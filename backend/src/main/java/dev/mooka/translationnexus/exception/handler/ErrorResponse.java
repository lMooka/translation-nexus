package dev.mooka.translationnexus.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = 3687419227405114999L;

  private String uniqueId;
  private String method;
  private String path;
  private Long timestamp;
  private Integer status;
  private String code;
  private String message;
  @Builder.Default
  private Boolean retryable = Boolean.FALSE;
  private transient LinkedHashMap<String, Object> params;

}
