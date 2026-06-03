package dev.mooka.translationnexus.domain;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEntry {

    private String locale;

    private String modifiedBy;

    private String previousValue;

    private String newValue;

    /** EDIT or APPROVE */
    private String action;

    private Instant timestamp;
}
