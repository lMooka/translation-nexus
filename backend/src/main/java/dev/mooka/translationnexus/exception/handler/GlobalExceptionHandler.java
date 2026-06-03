package dev.mooka.translationnexus.exception.handler;

import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.metrics.AppMetricService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private static final Integer CONSTRAINT_VIOLATION_HTTP_STATUS = 400;
  private static final String CONSTRAINT_VIOLATION_ERROR_CODE = "exception.request.constraint-violation";

  private static final Integer RUNTIME_EXCEPTION_HTTP_STATUS = 412;
  private static final String RUNTIME_EXCEPTION_ERROR_CODE = "exception.unhandled-exception";
  public static final String NO_MESSAGE_EXCEPTION = "no-message-exception";
  public static final String UNKNOWN = "UNKNOWN";
  //@formatter:on

  final AppMetricService metricService;

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException( //
      BusinessException ex, //
      WebRequest webRequest) //
  {
    String httpMethod = getHttpMethod(webRequest), path = getURI(webRequest);

    ErrorResponse errorResponse = ErrorResponse.builder() //
        .uniqueId(ex.getUniqueId())
        .method(httpMethod) //
        .timestamp(Instant.now().getEpochSecond()) //
        .path(path) //
        .status(ex.getStatus()) //
        .code(ex.getCode()) //
        .message(ex.getFormatedMessage(Boolean.TRUE)) //
        .retryable(ex.getRetryable()) //
        .params(ex.getParams()) //
        .build();

    metricService.businessException(ex);
    log.error(ex.getFormatedMessage(Boolean.TRUE), ex);
    return ResponseEntity.status(ex.getStatus()).body(errorResponse);
  }

  private String getHttpMethod(WebRequest webRequest) {
    String httpMethod = UNKNOWN;

    if (webRequest instanceof ServletWebRequest) {
      HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
      httpMethod = request.getMethod();
    }
    return httpMethod;
  }

  private String getURI(WebRequest webRequest) {
    String uri = UNKNOWN;

    if (webRequest instanceof ServletWebRequest) {
      HttpServletRequest request = ((ServletWebRequest) webRequest).getRequest();
      uri = request.getRequestURI();
    }
    return uri;
  }

}
