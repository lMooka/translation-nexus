package dev.mooka.translationnexus.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "locales")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Locale {
    @Id
    private String id; // e.g. "pt", "es", "fr", "de", "ja"
    private String name; // e.g. "Portuguese", "Spanish", etc.
    private String googleCode; // e.g. "pt" or "pt-PT" representing the Google Translate API target language code
    private Integer sortOrder;

    public Locale(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
