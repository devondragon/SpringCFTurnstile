package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;

/**
 * No-op implementation of TurnstileMetrics used when Micrometer is not on the classpath.
 */
public class NoOpTurnstileMetrics implements TurnstileMetrics {

    @Override
    public void recordValidation() {}

    @Override
    public void recordSuccess() {}

    @Override
    public void recordError(ValidationResultType type) {}

    @Override
    public void recordResponseTime(long milliseconds) {}
}
