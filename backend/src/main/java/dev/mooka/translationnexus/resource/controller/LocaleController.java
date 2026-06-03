package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.domain.entity.LocaleEntity;
import dev.mooka.translationnexus.resource.dto.LocaleDTO;
import dev.mooka.translationnexus.service.MapperService;
import dev.mooka.translationnexus.service.LocaleService;
import dev.mooka.translationnexus.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locales")
@RequiredArgsConstructor
@Tag(name = "Locales", description = "Endpoints for managing translation locales/languages")
public class LocaleController {

    private final LocaleService localeService;
    private final MapperService mapperService;

    @GetMapping
    @Operation(summary = "Get list of all active locales")
    public ResponseEntity<List<LocaleDTO>> list() {
        List<LocaleDTO> dtos = localeService.getAllLocales().stream()
                .map(mapperService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Operation(summary = "Create a new locale. MANAGER only.")
    public ResponseEntity<LocaleDTO> create(@RequestBody LocaleDTO localeDTO) throws BusinessException {
        LocaleEntity locale = mapperService.toEntity(localeDTO);
        LocaleEntity created = localeService.createLocale(locale);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapperService.toDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing locale. MANAGER only.")
    public ResponseEntity<LocaleDTO> update(@PathVariable String id, @RequestBody LocaleDTO localeDTO) throws BusinessException {
        LocaleEntity locale = mapperService.toEntity(localeDTO);
        LocaleEntity updated = localeService.updateLocale(id, locale);
        return ResponseEntity.ok(mapperService.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a locale. MANAGER only.")
    public ResponseEntity<Void> delete(@PathVariable String id) throws BusinessException {
        localeService.deleteLocale(id);
        return ResponseEntity.noContent().build();
    }
}
