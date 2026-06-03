package dev.mooka.translationnexus.domain.model;

import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryModel {
    private String id;
    private String name;
    @Builder.Default
    private List<PathMappingModel> pathMappings = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
