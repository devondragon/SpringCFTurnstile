package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;

/**
 * Abstraction for recording Turnstile validation metrics.
 * <p>
 * Implementations may be no-op (when Micrometer is absent from the classpath or metrics are
 * disabled) or Micrometer-backed. Consumers may also supply a custom implementation as a Spring
 * bean to integrate with their own metrics infrastructure.
 * </p>
 * <p>
 * Expected call sequence per validation attempt:
 * {@link #recordValidation()} is always called first, followed by exactly one of
 * {@link #recordSuccess()} or {@link #recordError(ValidationResultType)}, and then
 * {@link #recordResponseTime(long)} for any attempt that reached the network (input and
 * configuration errors do not record a response time).
 * </p>
 */
public interface TurnstileMetrics {

    /**
     * Records a validation attempt. Called once per invocation of
     * {@code validateTurnstileResponseDetailed}, regardless of outcome.
     */
    void recordValidation();

    /**
     * Records a successful validation. Called only when Cloudflare returns a success response.
     */
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

    /**
     * Records the elapsed wall-clock time for a validation attempt that reached the network.
     * Not called for input or configuration errors that short-circuit before the HTTP request.
     *
     * @param milliseconds elapsed time in milliseconds from the start of the validation call
     *                     to its completion (success or failure)
     */
    void recordResponseTime(long milliseconds);
}
