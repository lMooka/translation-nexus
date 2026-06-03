package dev.mooka.translationnexus.metrics;

import io.micrometer.core.instrument.*;

import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class MetricService {

    private final MeterRegistry registry;

    private final String appName;

    public MetricService(MeterRegistry registry,
                         String appName) {
        this.registry = registry;
        this.appName = appName;
    }

    private Tags defaultTags(String... extraTags) {
        return Tags.concat(Tags.of(extraTags)
        );
    }

    public void incrementCounter(String metricName, String... extraTags) {
        Counter.builder(getNameWithPrefix(metricName))
                .tags(defaultTags(extraTags))
                .register(registry)
                .increment();
    }

    public void incrementCounter(String metricName, double amount, String... extraTags) {
        Counter.builder(getNameWithPrefix(metricName))
                .tags(defaultTags(extraTags))
                .register(registry)
                .increment(amount);
    }

    public <T> T recordTime(String metricName,
                            Supplier<T> supplier,
                            String... extraTags) {

        Timer timer = Timer.builder(getNameWithPrefix(metricName))
                .tags(defaultTags(extraTags))
                .publishPercentileHistogram()
                .register(registry);

        return timer.record(supplier);
    }

    public void recordTime(String metricName,
                           Runnable runnable,
                           String... extraTags) {

        Timer timer = Timer.builder(getNameWithPrefix(metricName))
                .tags(defaultTags(extraTags))
                .publishPercentileHistogram()
                .register(registry);

        timer.record(runnable);
    }

    public <T> void registerGauge(String metricName,
                                  T obj,
                                  ToDoubleFunction<T> valueFunction,
                                  String... extraTags) {

        Gauge.builder(getNameWithPrefix(metricName), obj, valueFunction)
                .tags(defaultTags(extraTags))
                .register(registry);
    }

    public void recordDistribution(String metricName,
                                   double amount,
                                   String... extraTags) {

        DistributionSummary.builder(getNameWithPrefix(metricName))
                .tags(defaultTags(extraTags))
                .publishPercentileHistogram()
                .register(registry)
                .record(amount);
    }

    public LongTaskTimer.Sample startLongTask(String metricName,
                                              String... extraTags) {

        LongTaskTimer timer = LongTaskTimer.builder(getNameWithPrefix(metricName))
                .tags(defaultTags(extraTags))
                .register(registry);

        return timer.start();
    }

    public void stopLongTask(LongTaskTimer.Sample sample) {
        sample.stop();
    }

    public String getNameWithPrefix(String metricName) {
        return appName + "_" + metricName;
    }
}
