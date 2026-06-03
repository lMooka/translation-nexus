package dev.mooka.translationnexus.resource.dto;

import java.util.List;

public record LoginResponse(
    String token,
    List<String> roles,
    String username,
    List<String> allowedLocales
) {}
