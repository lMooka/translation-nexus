package dev.mooka.translationnexus.domain.model;

import dev.mooka.translationnexus.domain.enums.TranslationStatusEnum;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationValueModel {
    private String translatedValue;
    private TranslationStatusEnum status;
    private String lastModifiedBy;
    private Instant updatedAt;
}
