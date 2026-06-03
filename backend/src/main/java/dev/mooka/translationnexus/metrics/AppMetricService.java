package dev.mooka.translationnexus.metrics;

import dev.mooka.translationnexus.exception.BusinessException;
import io.micrometer.core.instrument.MeterRegistry;

public class AppMetricService extends MetricService {

  public static final String TAG_NAME_CLASS = "class";
  public static final String TAG_NAME_CODE = "code";
  private static final String METRIC_BUSINESS_EXCEPTION = "BUSINESS_EXCEPTION";

  public AppMetricService(MeterRegistry registry, String appName) {
    super(registry, appName);
  }

  public void businessException(BusinessException exception) {
    incrementCounter(
            METRIC_BUSINESS_EXCEPTION,
            TAG_NAME_CODE, exception.getCode(),
            TAG_NAME_CLASS, exception.getClass().getSimpleName()
    );
  }
}
