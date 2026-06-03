package dev.mooka.translationnexus.resource.dto;

import jakarta.validation.constraints.NotBlank;

public record AppVersionCreateDTO(
        @NotBlank String version
) {}
