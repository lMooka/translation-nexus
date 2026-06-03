package dev.mooka.translationnexus.domain.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocaleModel {
    private String id;
    private String name;
    private String googleCode;
    private Integer sortOrder;
}
