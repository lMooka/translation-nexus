package dev.mooka.translationnexus.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathMappingModel {
    private String pattern;
    private String filename;
}
