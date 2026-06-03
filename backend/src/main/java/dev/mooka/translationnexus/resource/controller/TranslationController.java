package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.NoPermissionException;
import dev.mooka.translationnexus.domain.HistoryEntry;
import dev.mooka.translationnexus.domain.TranslationDocument;
import dev.mooka.translationnexus.security.Roles;
import dev.mooka.translationnexus.service.TranslationService;
import dev.mooka.translationnexus.resource.dto.ImportResultDTO;
import dev.mooka.translationnexus.resource.dto.TranslationKeyCreateDTO;
import dev.mooka.translationnexus.domain.Locale;
import dev.mooka.translationnexus.resource.dto.TranslationUpdateDTO;
import dev.mooka.translationnexus.resource.dto.TranslateRequestDTO;
import dev.mooka.translationnexus.resource.dto.TranslateResponseDTO;
import dev.mooka.translationnexus.service.GoogleTranslateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/translations")
@RequiredArgsConstructor
@Tag(name = "Translations", description = "Endpoints for managing translation keys, locales, approvals, history, and CSV imports")
public class TranslationController {

    private final TranslationService translationService;
    private final GoogleTranslateService googleTranslateService;

    /** Create a new translation key. MANAGER only (enforced in SecurityConfig). */
    @PostMapping("/keys")
    @Operation(summary = "Create a new translation key", description = "Allows a MANAGER to define a new base translation key and metadata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Key created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - requires MANAGER role"),
            @ApiResponse(responseCode = "409", description = "Key already exists for the given version")
    })
    public ResponseEntity<TranslationDocument> createKey(@Valid @RequestBody TranslationKeyCreateDTO dto) throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED).body(translationService.createKey(dto));
    }

    /** Paged list with optional filters: version, tag, category, search text. */
    @GetMapping
    @Operation(summary = "Get list of translations", description = "Retrieve a paged, filtered list of translation documents.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved translation list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - requires authentication")
    })
    public ResponseEntity<Page<TranslationDocument>> list(
            @Parameter(description = "Filter translations by version (e.g. 1.0)") @RequestParam(required = false) String version,
            @Parameter(description = "Filter translations by tag") @RequestParam(required = false) String tag,
            @Parameter(description = "Filter translations by category") @RequestParam(required = false) String category,
            @Parameter(description = "Search query matching keyCode or English base value") @RequestParam(required = false) String search,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of each page") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(translationService.findAll(version, tag, category, search, PageRequest.of(page, size)));
    }

    /**
     * Save / update a translation for a specific locale.
     * Any edit/save operation moves the translation to REVIEW status.
     */
    @PutMapping("/{id}/{locale}")
    @Operation(summary = "Submit or update a translation value", description = "Updates a translation value and sets its status to REVIEW.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Translation updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid translation value or placeholder mismatch"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Translation key document not found")
    })
    public ResponseEntity<TranslationDocument> update(
            @Parameter(description = "ID of the translation document") @PathVariable String id,
            @Parameter(description = "Locale identifier (e.g. pt, en)") @PathVariable String locale,
            @Valid @RequestBody TranslationUpdateDTO dto,
            Authentication authentication) throws BusinessException {

        String username = (String) authentication.getPrincipal();
        boolean isReviewer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Roles.ROLE_REVIEWER));

        boolean isAuthorizedForLocale = isReviewer || authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Roles.ROLE_TRANSLATOR + "_" + locale.toUpperCase()));

        if (!isAuthorizedForLocale) {
            throw new NoPermissionException();
        }

        return ResponseEntity.ok(translationService.updateTranslation(id, locale, dto, username, isReviewer));
    }

    /** All documents that have at least one locale in REVIEW. REVIEWER only. */
    @GetMapping("/pending")
    @Operation(summary = "Get pending translations", description = "Retrieve all translation documents containing at least one locale entry in REVIEW status. REVIEWER only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending translations"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires REVIEWER role")
    })
    public ResponseEntity<List<TranslationDocument>> pending() {
        return ResponseEntity.ok(translationService.findPending());
    }

    /** Approve a specific locale translation. REVIEWER only. */
    @PostMapping("/{id}/{locale}/approve")
    @Operation(summary = "Approve a translation value", description = "Approve a translation value that is pending approval. REVIEWER only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Translation approved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires REVIEWER role"),
            @ApiResponse(responseCode = "404", description = "Translation key or locale translation not found")
    })
    public ResponseEntity<TranslationDocument> approve(
            @Parameter(description = "ID of the translation document") @PathVariable String id,
            @Parameter(description = "Locale identifier (e.g. pt)") @PathVariable String locale,
            Authentication authentication) throws BusinessException {

        String username = (String) authentication.getPrincipal();
        return ResponseEntity.ok(translationService.approveTranslation(id, locale, username));
    }

    /** Full audit history for a translation key document. */
    @GetMapping("/{id}/history")
    @Operation(summary = "Get translation history", description = "Retrieve the full edit and approval history log for a translation key document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved history"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Translation key document not found")
    })
    public ResponseEntity<List<HistoryEntry>> history(
            @Parameter(description = "ID of the translation document") @PathVariable String id) {
        return ResponseEntity.ok(translationService.getHistory(id));
    }

    /**
     * Import a localization CSV file.
     * Upserts all rows into the translations collection using version "1.0".
     * REVIEWER only (enforced in SecurityConfig).
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import translations from CSV", description = "Bulk imports/upserts localization keys, categories, tags, and translation values from a CSV file. REVIEWER only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV file imported successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to parse the CSV file"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires REVIEWER role")
    })
    public ResponseEntity<ImportResultDTO> importCsv(
            @Parameter(description = "The CSV file containing columns: Review Status, Category, Internal Name, Field, English, Português, Weblate Key")
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws BusinessException {
        String username = (String) authentication.getPrincipal();
        return ResponseEntity.ok(translationService.importCsv(file, username));
    }

    @PutMapping("/{id}/{locale}/status")
    @Operation(summary = "Update translation status", description = "Allows a REVIEWER to change the status of a translation for a specific locale (e.g. to APPROVED, REVIEW, or PENDING).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Translation status updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires REVIEWER role"),
            @ApiResponse(responseCode = "404", description = "Translation key or locale translation not found")
    })
    public ResponseEntity<TranslationDocument> updateStatus(
            @PathVariable String id,
            @PathVariable String locale,
            @RequestParam String status,
            Authentication authentication) throws BusinessException {
        boolean isReviewer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Roles.ROLE_REVIEWER));
        if (!isReviewer) {
            throw new NoPermissionException();
        }
        String username = (String) authentication.getPrincipal();
        return ResponseEntity.ok(translationService.updateStatus(id, locale, status, username));
    }

    @GetMapping("/locales")
    @Operation(summary = "Get all available locales", description = "Retrieve list of all active translation locales.")
    public ResponseEntity<List<Locale>> listLocales() {
        return ResponseEntity.ok(translationService.getAllLocales());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a translation key. MANAGER only.")
    public ResponseEntity<Void> deleteKey(@PathVariable String id) throws BusinessException {
        translationService.deleteKey(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/translate")
    @Operation(summary = "Auto-translate text via Google Cloud Translation API", description = "Translates base text into the target language code.")
    public ResponseEntity<TranslateResponseDTO> translate(@Valid @RequestBody TranslateRequestDTO dto) throws BusinessException {
        String targetLang = dto.targetLanguage();
        java.util.Optional<Locale> localeOpt = translationService.getLocaleById(targetLang);
        if (localeOpt.isPresent() && localeOpt.get().getGoogleCode() != null && !localeOpt.get().getGoogleCode().isBlank()) {
            targetLang = localeOpt.get().getGoogleCode().trim();
        }
        String result = googleTranslateService.translate(dto.text(), targetLang);
        return ResponseEntity.ok(new TranslateResponseDTO(result));
    }
}
