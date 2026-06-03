package dev.mooka.translationnexus.resource.dto;

import jakarta.validation.constraints.NotBlank;

public record TranslationUpdateDTO(@NotBlank String value) {
}
