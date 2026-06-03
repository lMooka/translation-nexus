package dev.mooka.translationnexus.resource.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEntryDTO {
    private String locale;
    private String modifiedBy;
    private String previousValue;
    private String newValue;
    private String action;
    private Instant timestamp;
}
