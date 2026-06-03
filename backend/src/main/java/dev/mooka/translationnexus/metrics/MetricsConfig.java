package dev.mooka.translationnexus.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public AppMetricService metricService(
            MeterRegistry registry,
            @Value("${app.metrics.name}") String appName) {

        return new AppMetricService(registry, appName);
    }
}
