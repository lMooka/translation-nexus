package dev.mooka.translationnexus.resource.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathMappingDTO {
    private String pattern;
    private String filename;
}
