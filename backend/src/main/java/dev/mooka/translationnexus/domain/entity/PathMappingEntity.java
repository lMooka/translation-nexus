package dev.mooka.translationnexus.domain.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathMappingEntity {
    private String pattern;
    private String filename;
}
