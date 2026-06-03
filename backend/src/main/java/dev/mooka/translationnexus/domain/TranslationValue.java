package dev.mooka.translationnexus.domain;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationValue {

    private String translatedValue;

    /** PENDING_APPROVAL or APPROVED */
    private String status;

    private String lastModifiedBy;

    private Instant updatedAt;
}
