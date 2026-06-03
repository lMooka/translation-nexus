package dev.mooka.translationnexus.resource.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocaleDTO {
    private String id;
    private String name;
    private String googleCode;
    private Integer sortOrder;
}
