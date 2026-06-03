package dev.mooka.translationnexus.domain.model;

import dev.mooka.translationnexus.domain.enums.HistoryActionEnum;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEntryModel {
    private String locale;
    private String modifiedBy;
    private String previousValue;
    private String newValue;
    private HistoryActionEnum action;
    private Instant timestamp;
}
