package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.domain.AppVersion;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.service.TranslationService;
import dev.mooka.translationnexus.resource.dto.AppVersionCreateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
@Tag(name = "Versions", description = "Endpoints for managing application translation versions")
public class VersionController {

    private final TranslationService translationService;

    @GetMapping
    @Operation(summary = "Get list of all versions")
    public ResponseEntity<List<AppVersion>> list() {
        return ResponseEntity.ok(translationService.getAllVersions());
    }

    @PostMapping
    @Operation(summary = "Create a new version", description = "Deactivates current active version and clones all translation documents to the new version. MANAGER only.")
    public ResponseEntity<AppVersion> create(@Valid @RequestBody AppVersionCreateDTO dto) throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED).body(translationService.createVersion(dto.version()));
    }
}
