package com.digitalsanctuary.cf.test.turnstile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.digitalsanctuary.cf.test.TestApplication;
import com.digitalsanctuary.cf.turnstile.metrics.NoOpTurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;

/**
 * Metrics wiring sanity checks that verify auto-configuration exclusions work correctly.
 *
 * <p>Note: Micrometer is still on the test classpath (via spring-boot-starter-actuator), so these
 * tests cannot detect a bytecode-level {@code NoClassDefFoundError} regression. They verify that
 * Spring auto-configuration exclusions are respected and that the correct metrics implementation
 * is active given the classpath conditions.
 */
@SpringBootTest(
    classes = TestApplication.class,
    properties = {
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration," +
            "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
    }
)
@ActiveProfiles("test")
class TurnstileMetricsWiringTest {

    @Autowired
    private TurnstileValidationService turnstileValidationService;

    @Autowired
    private TurnstileMetrics turnstileMetrics;

    /**
     * Verifies that a TurnstileMetrics bean is available when metrics auto-configs are excluded.
     * Because Micrometer is still on the test classpath, MicrometerTurnstileMetrics remains active.
     */
    @Test
    void metricsAvailableWhenMicrometerAutoConfigExcluded() {
        assertNotNull(turnstileMetrics,
            "A TurnstileMetrics bean should be available even with Micrometer auto-config excluded");
        assertFalse(turnstileMetrics instanceof NoOpTurnstileMetrics,
            "MicrometerTurnstileMetrics should be active when Micrometer is on the classpath");
    }

    /**
     * Verifies validation still works (exercises the metrics recording code path via NoOp).
     */
    @Test
    void validationWorksWithoutMicrometer() {
        boolean result = turnstileValidationService.validateTurnstileResponse("short");
        assertFalse(result, "Short token should fail validation with false return (not throw)");
    }

    /**
     * Verifies internal counters still work without Micrometer (they use LongAdder/AtomicLong).
     */
    @Test
    void internalCountersWorkWithoutMicrometer() {
        long before = turnstileValidationService.getValidationCount();
        turnstileValidationService.validateTurnstileResponse("short");
        long after = turnstileValidationService.getValidationCount();
        assertTrue(after > before, "Validation count should have incremented");
    }
}
