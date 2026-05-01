package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;

/**
 * Abstraction for recording Turnstile validation metrics.
 * Implementations may be no-op (when Micrometer is absent) or Micrometer-backed.
 */
public interface TurnstileMetrics {
    void recordValidation();
    void recordSuccess();
    void recordError(ValidationResultType type);
    void recordResponseTime(long milliseconds);
}
