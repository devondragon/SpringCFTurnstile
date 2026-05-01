package com.digitalsanctuary.cf.turnstile.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.dto.TurnstileResponse;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileConfigurationException;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileNetworkException;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileValidationException;
import com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating responses from Cloudflare's Turnstile API.
 * <p>
 * This service provides methods to validate Turnstile tokens with the Cloudflare API, handling various error scenarios with appropriate exceptions
 * and detailed validation results. It maintains internal counters for validation attempts, success/failure rates, and response times, and delegates
 * to a {@link com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics} implementation (Micrometer-backed or no-op) for external metric recording.
 * </p>
 */
@Slf4j
public class TurnstileValidationService {
    private static final String UNKNOWN = "unknown";
    private static final int MIN_TOKEN_LENGTH = 20;

    private final RestClient turnstileRestClient;
    private final TurnstileConfigProperties properties;
    private final TurnstileMetrics metrics;

    // Internal counters (always active, independent of Micrometer)
    private final LongAdder validationCount = new LongAdder();
    private final LongAdder successCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();
    private final LongAdder networkErrorCount = new LongAdder();
    private final LongAdder configErrorCount = new LongAdder();
    private final LongAdder validationErrorCount = new LongAdder();
    private final LongAdder inputErrorCount = new LongAdder();
    private final AtomicLong lastResponseTime = new AtomicLong();
    private final AtomicLong totalResponseTime = new AtomicLong();
    private final AtomicLong responseCount = new AtomicLong();

    /**
     * Constructor for TurnstileValidationService.
     *
     * @param turnstileRestClient the RestClient to use for making requests to the Turnstile API
     * @param properties the TurnstileConfigProperties to use for configuration
     * @param metrics the TurnstileMetrics implementation for recording metrics
     */
    public TurnstileValidationService(@Qualifier("turnstileRestClient") RestClient turnstileRestClient,
            TurnstileConfigProperties properties, TurnstileMetrics metrics) {
        this.turnstileRestClient = turnstileRestClient;
        this.properties = properties;
        this.metrics = metrics;
    }

    /**
     * Method called after the bean is initialized. Logs the startup information and validates the required configuration.
     *
     * @throws TurnstileConfigurationException if required configuration properties are missing
     */
    @PostConstruct
    public void onStartup() {
        log.info("TurnstileValidationService started");
        log.info("Turnstile URL: {}", properties.getUrl());
        log.info("Turnstile Sitekey: {}", properties.getSitekey());
        log.info("Turnstile Secret: {}", properties.getSecret() != null && !properties.getSecret().isBlank() ? "[CONFIGURED]" : "[NOT CONFIGURED]");
        log.info("Turnstile Metrics enabled: {}", properties.getMetrics().isEnabled());
        log.info("Turnstile Health Check enabled: {}", properties.getMetrics().isHealthCheckEnabled());

        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            log.error("Turnstile secret key is not configured. Validation will fail.");
        }
        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            log.error("Turnstile URL is not configured. Validation will fail.");
        }
    }

    /**
     * Validates the Turnstile response token. Convenience method without remote IP.
     *
     * @param token the response token to be validated.
     * @return true if the response is valid and successful, false otherwise.
     */
    public boolean validateTurnstileResponse(String token) {
        return validateTurnstileResponse(token, null);
    }

    /**
     * Validates the Turnstile response token. Returns boolean and handles exceptions internally.
     *
     * @param token the response token to be validated.
     * @param remoteIp the remote IP address of the client (optional).
     * @return true if the response is valid and successful, false otherwise.
     */
    public boolean validateTurnstileResponse(String token, String remoteIp) {
        try {
            ValidationResult result = validateTurnstileResponseDetailed(token, remoteIp);
            return result.isSuccess();
        } catch (Exception e) {
            log.error("Unexpected error during Turnstile validation: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates the Turnstile response token. Convenience method without remote IP.
     *
     * @param token the response token to be validated.
     * @return a ValidationResult object with detailed information about the validation outcome.
     * @throws TurnstileConfigurationException if the service is not properly configured
     * @throws TurnstileNetworkException if a network error occurs during validation
     * @throws TurnstileValidationException if the token is rejected by Cloudflare
     */
    public ValidationResult validateTurnstileResponseDetailed(String token) {
        return validateTurnstileResponseDetailed(token, null);
    }

    /**
     * Validates the Turnstile response token with detailed results and typed exceptions.
     *
     * @param token the response token to be validated.
     * @param remoteIp the remote IP address of the client (optional).
     * @return a ValidationResult object with detailed information about the validation outcome.
     * @throws TurnstileConfigurationException if the service is not properly configured
     * @throws TurnstileNetworkException if a network error occurs during validation
     * @throws TurnstileValidationException if the token is rejected by Cloudflare
     */
    public ValidationResult validateTurnstileResponseDetailed(String token, String remoteIp) {
        long startTime = System.currentTimeMillis();
        validationCount.increment();
        metrics.recordValidation();

        log.trace("Starting validation for token: {} with remoteIp: {}", token, remoteIp);

        if (token == null) {
            log.warn("Turnstile validation failed: token cannot be null");
            recordError(ValidationResultType.INPUT_ERROR);
            return ValidationResult.inputError("Token cannot be null");
        }

        if (token.isEmpty() || token.isBlank()) {
            log.warn("Turnstile validation failed: token cannot be empty or blank");
            recordError(ValidationResultType.INPUT_ERROR);
            return ValidationResult.inputError("Token cannot be empty or blank");
        }

        if (token.length() < MIN_TOKEN_LENGTH) {
            log.warn("Turnstile validation failed: token appears to be too short to be valid (length: {})", token.length());
            recordError(ValidationResultType.INPUT_ERROR);
            return ValidationResult.inputError("Token is too short to be valid (length: " + token.length() + ")");
        }

        String cleanRemoteIp = remoteIp;
        if (cleanRemoteIp != null && (cleanRemoteIp.isEmpty() || cleanRemoteIp.isBlank())) {
            log.warn("Turnstile validation: ignoring empty or blank remoteIp");
            cleanRemoteIp = null;
        }

        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            String msg = "Turnstile secret key is not configured";
            log.error(msg);
            recordError(ValidationResultType.CONFIGURATION_ERROR);
            throw new TurnstileConfigurationException(msg);
        }

        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            String msg = "Turnstile URL is not configured";
            log.error(msg);
            recordError(ValidationResultType.CONFIGURATION_ERROR);
            throw new TurnstileConfigurationException(msg);
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("secret", properties.getSecret());
        requestBody.put("response", token);
        Optional.ofNullable(cleanRemoteIp).ifPresent(ip -> requestBody.put("remoteip", ip));

        log.trace("Making request to Cloudflare Turnstile API at: {}", properties.getUrl());

        try {
            return executeValidationRequest(requestBody);
        } catch (HttpClientErrorException e) {
            log.error("Client error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Client error: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("Server error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Server error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Network error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Network error: " + e.getMessage(), e);
        } catch (TurnstileValidationException e) {
            log.debug("Turnstile token rejected by Cloudflare: {}", e.getMessage());
            recordError(ValidationResultType.INVALID_TOKEN);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected {} during Turnstile validation: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Unexpected error: " + e.getMessage(), e);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            lastResponseTime.set(elapsed);
            totalResponseTime.addAndGet(elapsed);
            responseCount.incrementAndGet();
            try {
                metrics.recordResponseTime(elapsed);
            } catch (Exception metricsEx) {
                log.warn("Failed to record response time metric; validation result is unaffected: {}", metricsEx.getMessage(), metricsEx);
            }
        }
    }

    private ValidationResult executeValidationRequest(Map<String, String> requestBody) {
        TurnstileResponse response = turnstileRestClient.post().uri(properties.getUrl())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(requestBody).retrieve().body(TurnstileResponse.class);

        log.debug("Turnstile response: {}", response);

        if (response == null) {
            log.warn("Turnstile API returned null response");
            recordError(ValidationResultType.NETWORK_ERROR);
            return ValidationResult.networkError("Cloudflare returned an empty response");
        }

        if (response.isSuccess()) {
            log.debug("Turnstile validation successful");
            successCount.increment();
            metrics.recordSuccess();
            return ValidationResult.success();
        } else {
            log.warn("Turnstile validation failed with error codes: {}", response.getErrorCodes());
            throw new TurnstileValidationException("Token validation failed", response.getErrorCodes());
        }
    }

    private void recordError(ValidationResultType resultType) {
        errorCount.increment();
        metrics.recordError(resultType);

        switch (resultType) {
            case NETWORK_ERROR -> networkErrorCount.increment();
            case CONFIGURATION_ERROR -> configErrorCount.increment();
            case INVALID_TOKEN -> validationErrorCount.increment();
            case INPUT_ERROR -> inputErrorCount.increment();
            default -> { }
        }
    }

    /**
     * Gets the total number of validation attempts.
     *
     * @return total number of validation attempts
     */
    public long getValidationCount() {
        return validationCount.sum();
    }

    /**
     * Gets the number of successful validations.
     *
     * @return number of successful validations
     */
    public long getSuccessCount() {
        return successCount.sum();
    }

    /**
     * Gets the number of failed validations.
     *
     * @return number of failed validations
     */
    public long getErrorCount() {
        return errorCount.sum();
    }

    /**
     * Gets the number of network errors.
     *
     * @return number of network errors
     */
    public long getNetworkErrorCount() {
        return networkErrorCount.sum();
    }

    /**
     * Gets the number of configuration errors.
     *
     * @return number of configuration errors
     */
    public long getConfigErrorCount() {
        return configErrorCount.sum();
    }

    /**
     * Gets the number of validation errors (invalid tokens).
     *
     * @return number of validation errors
     */
    public long getValidationErrorCount() {
        return validationErrorCount.sum();
    }

    /**
     * Gets the number of input validation errors.
     *
     * @return number of input validation errors
     */
    public long getInputErrorCount() {
        return inputErrorCount.sum();
    }

    /**
     * Gets the time of the last response in milliseconds.
     *
     * @return time of last response in milliseconds
     */
    public long getLastResponseTime() {
        return lastResponseTime.get();
    }

    /**
     * Gets the average response time in milliseconds.
     *
     * @return average response time in milliseconds, or 0 if no responses yet
     */
    public double getAverageResponseTime() {
        long count = responseCount.get();
        return count > 0 ? (double) totalResponseTime.get() / count : 0;
    }

    /**
     * Gets the error rate as a percentage of total validation attempts.
     *
     * @return error rate as a percentage (0-100), or 0 if no attempts yet
     */
    public double getErrorRate() {
        long total = validationCount.sum();
        return total > 0 ? (double) errorCount.sum() * 100 / total : 0;
    }

    /**
     * Gets the Turnstile Sitekey.
     *
     * @return the Turnstile Sitekey.
     * @deprecated Use {@link #getTurnstileSitekey()} instead. Will be removed in a future version.
     */
    @Deprecated
    public String getTurnsiteSitekey() {
        return getTurnstileSitekey();
    }

    /**
     * Gets the Turnstile Sitekey.
     *
     * @return the Turnstile Sitekey
     */
    public String getTurnstileSitekey() {
        return properties.getSitekey();
    }

    /**
     * Gets the client IP address from the ServletRequest.
     *
     * @param request the ServletRequest.
     * @return the client IP address.
     */
    public String getClientIpAddress(ServletRequest request) {
        if (request instanceof HttpServletRequest httpRequest) {
            String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
            for (String header : headers) {
                String ipHeaderValue = httpRequest.getHeader(header);
                if (ipHeaderValue == null || ipHeaderValue.isBlank()) {
                    continue;
                }
                String candidate = ipHeaderValue.split(",", 2)[0].trim();
                if (!candidate.isEmpty() && !UNKNOWN.equalsIgnoreCase(candidate)) {
                    return candidate;
                }
            }
        }
        return request.getRemoteAddr();
    }
}
