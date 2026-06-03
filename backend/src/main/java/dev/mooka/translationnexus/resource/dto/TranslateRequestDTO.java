package dev.mooka.translationnexus.resource.dto;

import jakarta.validation.constraints.NotBlank;

public record TranslateRequestDTO(
        @NotBlank String text,
        @NotBlank String targetLanguage
) {}
