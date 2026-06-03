package dev.mooka.translationnexus.resource.controller;

import dev.mooka.translationnexus.domain.entity.AppVersionEntity;
import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.service.VersionService;
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

    private final VersionService versionService;

    @GetMapping
    @Operation(summary = "Get list of all versions")
    public ResponseEntity<List<AppVersionEntity>> list() {
        return ResponseEntity.ok(versionService.getAllVersions());
    }

    @PostMapping
    @Operation(summary = "Create a new version", description = "Deactivates current active version and clones all translation documents to the new version. MANAGER only.")
    public ResponseEntity<AppVersionEntity> create(@Valid @RequestBody AppVersionCreateDTO dto) throws BusinessException {
        return ResponseEntity.status(HttpStatus.CREATED).body(versionService.createVersion(dto.version()));
    }
}
