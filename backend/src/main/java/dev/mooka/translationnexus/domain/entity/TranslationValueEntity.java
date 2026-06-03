package dev.mooka.translationnexus.domain.entity;

import dev.mooka.translationnexus.domain.enums.TranslationStatusEnum;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationValueEntity {
    private String translatedValue;
    private TranslationStatusEnum status; // PENDING, APPROVED, REVIEW
    private String lastModifiedBy;
    private Instant updatedAt;
}
