package dev.mooka.translationnexus.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
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
public class TranslationEntity {

    @Id
    private String id;
    private String keyCode;

    @Indexed
    private String version;

    @Indexed
    private String category;

    @Indexed
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private String contextInfo;
    private String baseValue;
    @Builder.Default
    private Map<String, TranslationValueEntity> translations = new HashMap<>();
    @Builder.Default
    private List<HistoryEntryEntity> history = new ArrayList<>();
    @Builder.Default
    private Integer priority = 3;
    private Instant createdAt;
    private Instant updatedAt;
}
