package dev.mooka.translationnexus.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @Builder.Default
    private List<PathMappingEntity> pathMappings = new ArrayList<>();

    @Builder.Default
    private Integer priority = 3;

    private Instant createdAt;

    private Instant updatedAt;
}
