package dev.mooka.translationnexus.resource.dto;

import java.util.List;

public record ImportResultDTO(
        int totalRows,
        int importedCount,
        int skippedCount,
        List<String> skippedKeyCodes
) {}
