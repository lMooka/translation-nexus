package dev.mooka.translationnexus.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "versions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersion {

    @Id
    private String id;

    @Indexed(unique = true)
    private String version;

    private boolean active;

    private Instant createdAt;
}
