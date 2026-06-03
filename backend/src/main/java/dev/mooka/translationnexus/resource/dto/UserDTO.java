package dev.mooka.translationnexus.resource.dto;

import java.util.List;

public record UserDTO(
    String id,
    String username,
    List<String> roles,
    List<String> allowedLocales
) {}
