package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;

/**
 * Abstraction for recording Turnstile validation metrics.
 * Implementations may be no-op (when Micrometer is absent) or Micrometer-backed.
 */
public interface TurnstileMetrics {
    void recordValidation();
    void recordSuccess();

    /**
     * Records an error metric for a failed validation result.
     *
     * @param type the type of validation error that occurred. Expected values are:
     *             {@link ValidationResultType#NETWORK_ERROR},
     *             {@link ValidationResultType#CONFIGURATION_ERROR},
     *             {@link ValidationResultType#INVALID_TOKEN},
     *             {@link ValidationResultType#INPUT_ERROR}.
     *             {@link ValidationResultType#SUCCESS} is never passed to this method.
     */
    void recordError(ValidationResultType type);

    void recordResponseTime(long milliseconds);
}
