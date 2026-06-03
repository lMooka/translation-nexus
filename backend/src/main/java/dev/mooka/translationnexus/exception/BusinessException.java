package dev.mooka.translationnexus.exception;

import dev.mooka.translationnexus.exception.handler.ErrorResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BusinessException extends Exception {

  @Serial
  private static final long serialVersionUID = -6244119180966344271L;

  private static final String LOG_MESSSAGE = "exception.code:[{}] - message: {}";

  public static final Integer DEFAULT_STATUS = 412;
  public static final Integer NOT_FOUND_STATUS = 404;

  public static final String MDC_TAG_UNIQUE_ID = "uniqueId";
  public static final String MDC_TAG_EXCEPTION_CODE = "exception-code";
  public static final String EXCEPTION_METRIC = "exception";
  public static final String EXCEPTION_METRIC_CODE_TAG = "code";

  private String uniqueId;
  private Integer status;
  private final String code;
  private transient LinkedHashMap<String, Object> params;
  private Boolean retryable;

  protected BusinessException(Integer status, String code, String message) {
    super(message);
    this.uniqueId = UUID.randomUUID().toString();
    this.status = status;
    this.code = code;
    this.params = new LinkedHashMap<>();
    this.retryable = Boolean.FALSE;

    MDC.put(MDC_TAG_UNIQUE_ID, uniqueId);
  }

  public ErrorResponse getResponseBody() {
    return ErrorResponse.builder() //
        .uniqueId(uniqueId).code(code) //
        .message(getMessage()) //
        .params(params) //
        .build();
  }

  public void param(String key, Object value) {
    this.params.put(key, value);
  }

  public void warn(Boolean withParams) {
    MDC.put(MDC_TAG_EXCEPTION_CODE, code);
    log.warn(LOG_MESSSAGE, getCode(), getFormatedMessage(withParams));
  }

  public void error() {
    MDC.put(MDC_TAG_EXCEPTION_CODE, code);
    log.error(LOG_MESSSAGE, getCode(), getFormatedMessage(Boolean.TRUE));
  }

  public void error(Throwable t) {
    MDC.put(MDC_TAG_EXCEPTION_CODE, code);
    log.error(LOG_MESSSAGE, getCode(), getFormatedMessage(Boolean.TRUE), t);
  }

  public static void count( //
                            @NonNull MeterRegistry registry, //
                            @NonNull String exceptionCode) //
  {
    registry.counter(EXCEPTION_METRIC, Tags.of(Tag.of(EXCEPTION_METRIC_CODE_TAG, exceptionCode)))
        .increment();
  }

  @Override
  public String getMessage() {
    String formated = super.getMessage();

    for (Entry<String, Object> entry : params.entrySet()) {
      String regex = "\\[(" + entry.getKey() + ")]";
      formated = formated.replaceAll(regex,
          nonNull(entry.getValue()) ? entry.getValue().toString() : "null");
    }

    return formated;
  }

  @Override
  public String getLocalizedMessage() {
    return getFormatedMessage(true);
  }

  public String getFormatedMessage(Boolean withParams) {
    String formated =
        getCode() + " - " + super.getLocalizedMessage() + " - uniqueId:[" + uniqueId + "]";

    for (Entry<String, Object> entry : params.entrySet()) {
      String regex = "\\[(" + entry.getKey() + ")]";
      formated = formated.replaceAll(regex,
          nonNull(entry.getValue()) ? entry.getValue().toString() : "null");
    }

    if (withParams) {
      StringBuilder builder = new StringBuilder(formated);
      builder.append(" - params:[");

      boolean first = Boolean.TRUE;
      for (Entry<String, Object> entry : params.entrySet()) {
        if (!first) {
          builder.append(",");
        } else {
          first = Boolean.FALSE;
        }
        builder.append(entry.getKey());
        builder.append("=");
        builder.append(entry.getValue());
      }

      builder.append("]");
      formated = builder.toString();
    }

    return formated;
  }
}
