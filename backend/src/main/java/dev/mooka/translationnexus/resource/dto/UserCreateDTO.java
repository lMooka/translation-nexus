package dev.mooka.translationnexus.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserCreateDTO(
    @NotBlank String username,
    @NotBlank String password,
    @NotEmpty List<String> roles,
    @NotNull List<String> allowedLocales
) {}
