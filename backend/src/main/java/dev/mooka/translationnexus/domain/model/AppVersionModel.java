package dev.mooka.translationnexus.domain.model;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionModel {
    private String id;
    private String version;
    private boolean active;
    private Instant createdAt;
}
