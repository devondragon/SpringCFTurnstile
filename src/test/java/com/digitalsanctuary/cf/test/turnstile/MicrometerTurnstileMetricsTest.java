package com.digitalsanctuary.cf.test.turnstile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;
import com.digitalsanctuary.cf.turnstile.metrics.MicrometerTurnstileMetrics;

/**
 * Unit tests for MicrometerTurnstileMetrics using an in-memory SimpleMeterRegistry.
 * Verifies that each method routes to the correct named Micrometer meter.
 */
class MicrometerTurnstileMetricsTest {

    private SimpleMeterRegistry registry;
    private MicrometerTurnstileMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new MicrometerTurnstileMetrics(registry);
    }

    @Test
    void recordValidation_incrementsRequestsCounter() {
        metrics.recordValidation();
        metrics.recordValidation();
        assertEquals(2.0, registry.counter("turnstile.validation.requests").count());
    }

    @Test
    void recordSuccess_incrementsSuccessCounter() {
        metrics.recordSuccess();
        assertEquals(1.0, registry.counter("turnstile.validation.success").count());
    }

    @Test
    void recordError_networkError_incrementsAggregateAndSubcounter() {
        metrics.recordError(ValidationResultType.NETWORK_ERROR);
        assertEquals(1.0, registry.counter("turnstile.validation.errors").count());
        assertEquals(1.0, registry.counter("turnstile.validation.errors.network").count());
        assertEquals(0.0, registry.counter("turnstile.validation.errors.config").count());
        assertEquals(0.0, registry.counter("turnstile.validation.errors.token").count());
        assertEquals(0.0, registry.counter("turnstile.validation.errors.input").count());
    }

    @Test
    void recordError_configurationError_incrementsAggregateAndSubcounter() {
        metrics.recordError(ValidationResultType.CONFIGURATION_ERROR);
        assertEquals(1.0, registry.counter("turnstile.validation.errors").count());
        assertEquals(0.0, registry.counter("turnstile.validation.errors.network").count());
        assertEquals(1.0, registry.counter("turnstile.validation.errors.config").count());
    }

    @Test
    void recordError_invalidToken_incrementsAggregateAndSubcounter() {
        metrics.recordError(ValidationResultType.INVALID_TOKEN);
        assertEquals(1.0, registry.counter("turnstile.validation.errors").count());
        assertEquals(1.0, registry.counter("turnstile.validation.errors.token").count());
    }

    @Test
    void recordError_inputError_incrementsAggregateAndSubcounter() {
        metrics.recordError(ValidationResultType.INPUT_ERROR);
        assertEquals(1.0, registry.counter("turnstile.validation.errors").count());
        assertEquals(1.0, registry.counter("turnstile.validation.errors.input").count());
    }

    @Test
    void recordResponseTime_recordsToTimer() {
        metrics.recordResponseTime(150L);
        metrics.recordResponseTime(250L);
        assertEquals(2L, registry.timer("turnstile.validation.response.time").count());
    }

    @Test
    void constructor_rejectsNullRegistry() {
        assertThrows(NullPointerException.class, () -> new MicrometerTurnstileMetrics(null));
    }

    @Test
    void multipleErrors_aggregateCountEqualsSum() {
        metrics.recordError(ValidationResultType.NETWORK_ERROR);
        metrics.recordError(ValidationResultType.INVALID_TOKEN);
        metrics.recordError(ValidationResultType.INPUT_ERROR);

        Counter aggregate = registry.counter("turnstile.validation.errors");
        double subTotal = registry.counter("turnstile.validation.errors.network").count()
                + registry.counter("turnstile.validation.errors.config").count()
                + registry.counter("turnstile.validation.errors.token").count()
                + registry.counter("turnstile.validation.errors.input").count();

        assertEquals(aggregate.count(), subTotal, "Aggregate error count must equal sum of sub-counters");
    }
}
