package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;

/**
 * No-op implementation of TurnstileMetrics used when Micrometer is not on the classpath.
 */
public class NoOpTurnstileMetrics implements TurnstileMetrics {

    @Override
    public void recordValidation() { // no-op
    }

    @Override
    public void recordSuccess() { // no-op
    }

    @Override
    public void recordError(ValidationResultType type) { // no-op
    }

    @Override
    public void recordResponseTime(long milliseconds) { // no-op
    }
}
