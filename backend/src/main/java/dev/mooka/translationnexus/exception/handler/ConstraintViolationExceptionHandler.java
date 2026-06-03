package dev.mooka.translationnexus.exception.handler;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.netty.http.server.HttpServerRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;

import static dev.mooka.translationnexus.exception.BusinessException.count;

@RequiredArgsConstructor
@Provider
@Component
public class ConstraintViolationExceptionHandler
    implements ExceptionMapper<ConstraintViolationException> {

  private static final Integer HTTP_STATUS = 400;
  private static final String ERROR_CODE = "exception.request.constraint-violation";

  @Context
  UriInfo uriInfo;

  @Context
  HttpServerRequest request;

  final MeterRegistry registry;

  @Override
  public Response toResponse(ConstraintViolationException ex) {
    LinkedHashMap<String, Object> violations = new LinkedHashMap<>();

    ex.getConstraintViolations().forEach(c ->
        violations.put(c.getPropertyPath().toString(), c.getMessage()));

    count(registry, ERROR_CODE);

    return Response //
        .status(HTTP_STATUS) //
        .entity(ErrorResponse.builder() //
            .uniqueId(UUID.randomUUID().toString()) //
            .method(request.method().name()) //
            .timestamp(Instant.now().getEpochSecond()) //
            .path(uriInfo.getRequestUri().getPath()) //
            .status(HTTP_STATUS) //
            .code(ERROR_CODE) //
            .message("Falha na validação do objeto da requisição.") //
            .params(violations) //
            .build() //
        ).build();
  }
}
