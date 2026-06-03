package dev.mooka.translationnexus.resource.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TranslationKeyCreateDTO(
        @NotBlank String keyCode,
        String version,
        @NotBlank String category,
        List<String> tags,
        String contextInfo,
        @NotBlank String baseValue
) {}
