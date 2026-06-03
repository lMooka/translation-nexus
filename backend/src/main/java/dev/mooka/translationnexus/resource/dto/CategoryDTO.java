package dev.mooka.translationnexus.resource.dto;

import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private String id;
    private String name;
    @Builder.Default
    private List<PathMappingDTO> pathMappings = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
