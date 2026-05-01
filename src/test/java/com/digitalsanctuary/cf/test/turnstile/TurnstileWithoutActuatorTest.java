package com.digitalsanctuary.cf.test.turnstile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import com.digitalsanctuary.cf.test.TestApplication;
import com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;

/**
 * Regression tests for GitHub issue #94.
 * Verifies the library starts without NoClassDefFoundError when Micrometer or Actuator is absent.
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
class TurnstileWithoutActuatorTest {

    @Autowired
    private TurnstileValidationService turnstileValidationService;

    @Autowired
    private TurnstileMetrics turnstileMetrics;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Verifies TurnstileValidationService is available when Micrometer auto-configs are excluded.
     */
    @Test
    void contextLoadsWithoutMicrometer() {
        assertNotNull(turnstileValidationService, "TurnstileValidationService should be available");
    }

    /**
     * Verifies a TurnstileMetrics bean is available when Micrometer auto-configs are excluded.
     * Regression test for issue #94: unconditionally-loaded classes with direct Micrometer type
     * references caused NoClassDefFoundError when Micrometer was absent. The core fix ensures
     * that a metrics bean (of any implementation) is always available without throwing errors.
     */
    @Test
    void metricsAvailableWhenMicrometerAutoConfigExcluded() {
        assertNotNull(turnstileMetrics,
            "A TurnstileMetrics bean should be available even with Micrometer auto-config excluded");
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
        assertNotNull(after, "Validation count should be trackable");
        assertTrue(after > before, "Validation count should have incremented");
    }
}
