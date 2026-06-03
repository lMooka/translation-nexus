package dev.mooka.translationnexus.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathMapping {
    private String pattern;
    private String filename;
}
