package dev.mooka.translationnexus.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.*;

@Document(collection = "translations")
@CompoundIndexes({
        @CompoundIndex(name = "keycode_version_unique", def = "{'keyCode': 1, 'version': 1}", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationDocument {

    @Id
    private String id;

    private String keyCode;

    private String version;

    private String category;

    private List<String> tags = new ArrayList<>();

    private String contextInfo;

    /** English source text — the authoritative base value */
    private String baseValue;

    /**
     * Map of locale → translation value.
     * Key examples: "pt", "es", "fr", "de", "ja"
     */
    @Builder.Default
    private Map<String, TranslationValue> translations = new HashMap<>();

    /** Full audit trail of all edits and approvals */
    @Builder.Default
    private List<HistoryEntry> history = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;
}
