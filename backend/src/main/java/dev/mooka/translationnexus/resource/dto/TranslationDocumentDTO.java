package dev.mooka.translationnexus.resource.dto;

import lombok.*;
import java.time.Instant;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationDocumentDTO {
    private String id;
    private String keyCode;
    private String version;
    private String category;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private String contextInfo;
    private String baseValue;
    @Builder.Default
    private Map<String, TranslationValueDTO> translations = new HashMap<>();
    @Builder.Default
    private List<HistoryEntryDTO> history = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
