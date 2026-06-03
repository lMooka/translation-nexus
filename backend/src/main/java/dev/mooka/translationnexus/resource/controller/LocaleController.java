package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.domain.Locale;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locales")
@RequiredArgsConstructor
@Tag(name = "Locales", description = "Endpoints for managing translation locales/languages")
public class LocaleController {

    private final TranslationService translationService;

    @GetMapping
    @Operation(summary = "Get list of all active locales")
    public ResponseEntity<List<Locale>> list() {
        return ResponseEntity.ok(translationService.getAllLocales());
    }

    @PostMapping
    @Operation(summary = "Create a new locale. MANAGER only.")
    public ResponseEntity<Locale> create(@RequestBody Locale locale) throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED).body(translationService.createLocale(locale));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing locale. MANAGER only.")
    public ResponseEntity<Locale> update(@PathVariable String id, @RequestBody Locale locale) throws BusinessException {
        return ResponseEntity.ok(translationService.updateLocale(id, locale));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a locale. MANAGER only.")
    public ResponseEntity<Void> delete(@PathVariable String id) throws BusinessException {
        translationService.deleteLocale(id);
        return ResponseEntity.noContent().build();
    }
}
