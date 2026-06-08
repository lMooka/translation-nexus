package dev.mooka.translationnexus.resource.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserUpdateDTO(
    @NotEmpty List<String> roles,
    @NotNull List<String> allowedLocales,
    String password
) {}
