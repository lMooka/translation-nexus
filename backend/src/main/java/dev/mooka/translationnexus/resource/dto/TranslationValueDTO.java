package dev.mooka.translationnexus.resource.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationValueDTO {
    private String translatedValue;
    private String status;
    private String lastModifiedBy;
    private Instant updatedAt;
}
