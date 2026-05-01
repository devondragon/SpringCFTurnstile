package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Micrometer-backed implementation of {@link TurnstileMetrics}.
 * <p>
 * This class is only instantiated when Micrometer is present on the classpath, via
 * {@code TurnstileMetricsConfig} which is guarded by {@code @ConditionalOnClass(MeterRegistry.class)}.
 * </p>
 * <p>
 * All eight meters ({@code errorCounter} is the aggregate; the four type-specific counters are
 * sub-categories whose sum equals the aggregate) are eagerly registered at construction time so
 * they appear in monitoring dashboards before the first validation event occurs. A single instance
 * should be registered per application context to avoid duplicate meter registration errors.
 * </p>
 *
 * @see NoOpTurnstileMetrics
 */
@Slf4j
public class MicrometerTurnstileMetrics implements TurnstileMetrics {

    private final Counter validationCounter;
    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter networkErrorCounter;
    private final Counter configErrorCounter;
    private final Counter validationErrorCounter;
    private final Counter inputErrorCounter;
    private final Timer responseTimer;

    /**
     * Creates a {@code MicrometerTurnstileMetrics} instance and eagerly registers all Turnstile
     * meters with the provided registry. The registered meters are:
     * <ul>
     *   <li>{@code turnstile.validation.requests} — total attempts</li>
     *   <li>{@code turnstile.validation.success} — successful validations</li>
     *   <li>{@code turnstile.validation.errors} — all errors (aggregate)</li>
     *   <li>{@code turnstile.validation.errors.network} — network errors</li>
     *   <li>{@code turnstile.validation.errors.config} — configuration errors</li>
     *   <li>{@code turnstile.validation.errors.token} — invalid token errors</li>
     *   <li>{@code turnstile.validation.errors.input} — input validation errors</li>
     *   <li>{@code turnstile.validation.response.time} — response time timer</li>
     * </ul>
     *
     * @param registry the Micrometer {@link MeterRegistry} to register meters with; must not be null
     */
    public MicrometerTurnstileMetrics(MeterRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");
        log.info("Initializing Turnstile metrics with MeterRegistry");
        validationCounter = Counter.builder("turnstile.validation.requests")
                .description("Total number of Turnstile validation requests").register(registry);
        successCounter = Counter.builder("turnstile.validation.success")
                .description("Number of successful Turnstile validations").register(registry);
        errorCounter = Counter.builder("turnstile.validation.errors")
                .description("Number of failed Turnstile validations").register(registry);
        networkErrorCounter = Counter.builder("turnstile.validation.errors.network")
                .description("Number of Turnstile validation network errors").register(registry);
        configErrorCounter = Counter.builder("turnstile.validation.errors.config")
                .description("Number of Turnstile validation configuration errors").register(registry);
        validationErrorCounter = Counter.builder("turnstile.validation.errors.token")
                .description("Number of Turnstile validation token errors").register(registry);
        inputErrorCounter = Counter.builder("turnstile.validation.errors.input")
                .description("Number of Turnstile validation input errors").register(registry);
        responseTimer = Timer.builder("turnstile.validation.response.time")
                .description("Response time for Turnstile validation requests").register(registry);
    }

    @Override
    public void recordValidation() {
        validationCounter.increment();
    }

    @Override
    public void recordSuccess() {
        successCounter.increment();
    }

    @Override
    public void recordError(ValidationResultType type) {
        errorCounter.increment();
        switch (type) {
            case NETWORK_ERROR -> networkErrorCounter.increment();
            case CONFIGURATION_ERROR -> configErrorCounter.increment();
            case INVALID_TOKEN -> validationErrorCounter.increment();
            case INPUT_ERROR -> inputErrorCounter.increment();
            default -> { }
        }
    }

    @Override
    public void recordResponseTime(long milliseconds) {
        responseTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}
