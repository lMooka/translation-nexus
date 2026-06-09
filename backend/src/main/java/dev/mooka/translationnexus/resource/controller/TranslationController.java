package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.NoPermissionException;
import dev.mooka.translationnexus.domain.entity.TranslationEntity;
import dev.mooka.translationnexus.domain.entity.LocaleEntity;
import dev.mooka.translationnexus.domain.enums.TranslationStatusEnum;
import dev.mooka.translationnexus.security.Roles;
import dev.mooka.translationnexus.service.TranslationService;
import dev.mooka.translationnexus.service.LocaleService;
import dev.mooka.translationnexus.resource.dto.*;
import dev.mooka.translationnexus.service.MapperService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/translations")
@RequiredArgsConstructor
@Tag(name = "Translations", description = "Endpoints for managing translation keys, locales, approvals, history, and CSV imports")
public class TranslationController {

    private final TranslationService translationService;
    private final GoogleTranslateService googleTranslateService;
    private final MapperService mapperService;
    private final LocaleService localeService;

    /** Create a new translation key. MANAGER only (enforced in SecurityConfig). */
    @PostMapping("/keys")
    @Operation(summary = "Create a new translation key", description = "Allows a MANAGER to define a new base translation key and metadata.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Key created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - requires MANAGER role"),
            @ApiResponse(responseCode = "409", description = "Key already exists for the given version")
    })
    public ResponseEntity<TranslationDocumentDTO> createKey(@Valid @RequestBody TranslationKeyCreateDTO dto) throws BusinessException {
        TranslationEntity created = translationService.createKey(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapperService.toDTO(created));
    }

    /** Paged list with optional filters: version, tag, category, search text. */
    @GetMapping
    @Operation(summary = "Get list of translations", description = "Retrieve a paged, filtered list of translation documents.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved translation list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - requires authentication")
    })
    public ResponseEntity<Page<TranslationDocumentDTO>> list(
            @Parameter(description = "Filter translations by version (e.g. 1.0)") @RequestParam(required = false) String version,
            @Parameter(description = "Filter translations by tag") @RequestParam(required = false) List<String> tag,
            @Parameter(description = "Filter translations by category") @RequestParam(required = false) String category,
            @Parameter(description = "Search query matching keyCode or English base value") @RequestParam(required = false) String search,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of each page") @RequestParam(defaultValue = "20") int size) {
        Page<TranslationDocumentDTO> pageDto = translationService.findAll(version, tag, category, search, 
                PageRequest.of(page, size, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("priority"),
                        org.springframework.data.domain.Sort.Order.asc("keyCode")
                )))
                .map(mapperService::toDTO);
        return ResponseEntity.ok(pageDto);
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
    public ResponseEntity<TranslationDocumentDTO> update(
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

        TranslationEntity updated = translationService.updateTranslation(id, locale, dto, username, isReviewer);
        return ResponseEntity.ok(mapperService.toDTO(updated));
    }

    /** All documents that have at least one locale in REVIEW. REVIEWER only. */
    @GetMapping("/pending")
    @Operation(summary = "Get pending translations", description = "Retrieve all translation documents containing at least one locale entry in REVIEW status. REVIEWER only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending translations"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires REVIEWER role")
    })
    public ResponseEntity<List<TranslationDocumentDTO>> pending() {
        List<TranslationDocumentDTO> pendingList = translationService.findPending().stream()
                .map(mapperService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendingList);
    }

    /** Approve a specific locale translation. REVIEWER only. */
    @PostMapping("/{id}/{locale}/approve")
    @Operation(summary = "Approve a translation value", description = "Approve a translation value that is pending approval. REVIEWER only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Translation approved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires REVIEWER role"),
            @ApiResponse(responseCode = "404", description = "Translation key or locale translation not found")
    })
    public ResponseEntity<TranslationDocumentDTO> approve(
            @Parameter(description = "ID of the translation document") @PathVariable String id,
            @Parameter(description = "Locale identifier (e.g. pt)") @PathVariable String locale,
            Authentication authentication) throws BusinessException {

        String username = (String) authentication.getPrincipal();
        TranslationEntity approved = translationService.approveTranslation(id, locale, username);
        return ResponseEntity.ok(mapperService.toDTO(approved));
    }

    /** Full audit history for a translation key document. */
    @GetMapping("/{id}/history")
    @Operation(summary = "Get translation history", description = "Retrieve the full edit and approval history log for a translation key document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved history"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Translation key document not found")
    })
    public ResponseEntity<List<HistoryEntryDTO>> history(
            @Parameter(description = "ID of the translation document") @PathVariable String id) {
        List<HistoryEntryDTO> historyList = translationService.getHistory(id).stream()
                .map(mapperService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historyList);
    }

    @PutMapping("/{id}/{locale}/status")
    @Operation(summary = "Update translation status", description = "Allows a REVIEWER to change the status of a translation for a specific locale (e.g. to APPROVED, REVIEW, or PENDING).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Translation status updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires REVIEWER role"),
            @ApiResponse(responseCode = "404", description = "Translation key or locale translation not found")
    })
    public ResponseEntity<TranslationDocumentDTO> updateStatus(
            @PathVariable String id,
            @PathVariable String locale,
            @RequestParam TranslationStatusEnum status,
            Authentication authentication) throws BusinessException {
        boolean isReviewer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Roles.ROLE_REVIEWER));
        if (!isReviewer) {
            throw new NoPermissionException();
        }
        String username = (String) authentication.getPrincipal();
        TranslationEntity updated = translationService.updateStatus(id, locale, status, username);
        return ResponseEntity.ok(mapperService.toDTO(updated));
    }

    @GetMapping("/locales")
    @Operation(summary = "Get all available locales", description = "Retrieve list of all active translation locales.")
    public ResponseEntity<List<LocaleDTO>> listLocales() {
        List<LocaleDTO> localesList = localeService.getAllLocales().stream()
                .map(mapperService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(localesList);
    }

    @PutMapping("/{id}/priority")
    @Operation(summary = "Update translation priority. MANAGER only.", description = "Allows a MANAGER to change the priority of a translation document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Translation priority updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires MANAGER role"),
            @ApiResponse(responseCode = "404", description = "Translation key not found")
    })
    public ResponseEntity<TranslationDocumentDTO> updatePriority(
            @PathVariable String id,
            @RequestParam int priority) throws BusinessException {
        TranslationEntity updated = translationService.updatePriority(id, priority);
        return ResponseEntity.ok(mapperService.toDTO(updated));
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
        java.util.Optional<LocaleEntity> localeOpt = localeService.getLocaleById(targetLang);
        if (localeOpt.isPresent() && localeOpt.get().getGoogleCode() != null && !localeOpt.get().getGoogleCode().isBlank()) {
            targetLang = localeOpt.get().getGoogleCode().trim();
        }
        String result = googleTranslateService.translate(dto.text(), targetLang);
        return ResponseEntity.ok(new TranslateResponseDTO(result));
    }
}
