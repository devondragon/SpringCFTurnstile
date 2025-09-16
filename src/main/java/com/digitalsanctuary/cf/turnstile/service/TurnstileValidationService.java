package com.digitalsanctuary.cf.turnstile.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating responses from Cloudflare's Turnstile API.
 * <p>
 * This service provides methods to validate Turnstile tokens with the Cloudflare API, handling various error scenarios with appropriate exceptions
 * and detailed validation results. It also collects metrics on validation attempts, success/failure rates, and response times when metrics are
 * enabled.
 * </p>
 */
@Slf4j
@Service
public class TurnstileValidationService {
    private static final String UNKNOWN = "unknown";

    private final RestClient turnstileRestClient;
    private final TurnstileConfigProperties properties;
    private final Optional<MeterRegistry> meterRegistry;

    // Metrics (used regardless of whether Micrometer is available)
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

    // Micrometer metrics
    private Counter validationCounter;
    private Counter successCounter;
    private Counter errorCounter;
    private Counter networkErrorCounter;
    private Counter configErrorCounter;
    private Counter validationErrorCounter;
    private Counter inputErrorCounter;
    private Timer responseTimer;

    /**
     * Constructor for TurnstileValidationService with metrics support.
     *
     * @param turnstileRestClient the RestClient to use for making requests to the Turnstile API
     * @param properties the TurnstileConfigProperties to use for configuration
     * @param meterRegistry the optional MeterRegistry for recording metrics
     */
    public TurnstileValidationService(@Qualifier("turnstileRestClient") RestClient turnstileRestClient, TurnstileConfigProperties properties,
            Optional<MeterRegistry> meterRegistry) {
        this.turnstileRestClient = turnstileRestClient;
        this.properties = properties;
        this.meterRegistry = meterRegistry;

        // Initialize metrics if Micrometer is available
        initMetrics();
    }

    /**
     * Constructor for TurnstileValidationService without metrics support.
     *
     * @param turnstileRestClient the RestClient to use for making requests to the Turnstile API
     * @param properties the TurnstileConfigProperties to use for configuration
     */
    public TurnstileValidationService(@Qualifier("turnstileRestClient") RestClient turnstileRestClient, TurnstileConfigProperties properties) {
        this(turnstileRestClient, properties, Optional.empty());
    }

    /**
     * Initializes metrics if Micrometer is available.
     */
    private void initMetrics() {
        meterRegistry.ifPresent(registry -> {
            log.info("Initializing Turnstile metrics with MeterRegistry");

            // Initialize counters
            validationCounter =
                    Counter.builder("turnstile.validation.requests").description("Total number of Turnstile validation requests").register(registry);

            successCounter =
                    Counter.builder("turnstile.validation.success").description("Number of successful Turnstile validations").register(registry);

            errorCounter = Counter.builder("turnstile.validation.errors").description("Number of failed Turnstile validations").register(registry);

            networkErrorCounter = Counter.builder("turnstile.validation.errors.network").description("Number of Turnstile validation network errors")
                    .register(registry);

            configErrorCounter = Counter.builder("turnstile.validation.errors.config")
                    .description("Number of Turnstile validation configuration errors").register(registry);

            validationErrorCounter = Counter.builder("turnstile.validation.errors.token").description("Number of Turnstile validation token errors")
                    .register(registry);

            inputErrorCounter = Counter.builder("turnstile.validation.errors.input").description("Number of Turnstile validation input errors")
                    .register(registry);

            // Initialize timer
            responseTimer = Timer.builder("turnstile.validation.response.time").description("Response time for Turnstile validation requests")
                    .register(registry);
        });
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

        // Validate required configuration
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            log.error("Turnstile secret key is not configured. Validation will fail.");
        }

        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            log.error("Turnstile URL is not configured. Validation will fail.");
        }
    }

    /**
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API. This is a convenience method that doesn't require a
     * remote IP.
     *
     * @param token the response token to be validated.
     * @return true if the response is valid and successful, false otherwise.
     */
    public boolean validateTurnstileResponse(String token) {
        return validateTurnstileResponse(token, null);
    }

    /**
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API. This method returns a boolean result and handles all
     * exceptions internally.
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
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API. This is a convenience method that doesn't require a
     * remote IP.
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
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API. This method provides detailed validation results and
     * throws specific exceptions for different error scenarios.
     *
     * @param token the response token to be validated.
     * @param remoteIp the remote IP address of the client (optional).
     * @return a ValidationResult object with detailed information about the validation outcome.
     * @throws TurnstileConfigurationException if the service is not properly configured
     * @throws TurnstileNetworkException if a network error occurs during validation
     * @throws TurnstileValidationException if the token is rejected by Cloudflare
     */
    public ValidationResult validateTurnstileResponseDetailed(String token, String remoteIp) {
        // Start tracking metrics for this validation attempt
        long startTime = System.currentTimeMillis();
        validationCount.increment();
        if (validationCounter != null) {
            validationCounter.increment();
        }

        log.trace("Starting validation for token: {} with remoteIp: {}", token, remoteIp);

        // Validate input parameters
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

        // Basic format validation - Cloudflare tokens typically start with '0.' or '1.' followed by alphanumeric chars
        // and should be reasonably sized (typically 100+ chars)
        if (token.length() < 20) {
            log.warn("Turnstile validation failed: token appears to be too short to be valid (length: {})", token.length());
            recordError(ValidationResultType.INPUT_ERROR);
            return ValidationResult.inputError("Token is too short to be valid (length: " + token.length() + ")");
        }

        // Validate remoteIp if provided
        String cleanRemoteIp = remoteIp;
        if (cleanRemoteIp != null && (cleanRemoteIp.isEmpty() || cleanRemoteIp.isBlank())) {
            log.warn("Turnstile validation: ignoring empty or blank remoteIp");
            cleanRemoteIp = null;
        }

        // Validate that we have the required configuration
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

        // Create a JSON request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("secret", properties.getSecret());
        requestBody.put("response", token);
        Optional.ofNullable(cleanRemoteIp).ifPresent(ip -> requestBody.put("remoteip", ip));

        log.trace("Making request to Cloudflare Turnstile API at: {}", properties.getUrl());

        // Make the request to Cloudflare's Turnstile API
        try {
            // Use timer if available
            if (responseTimer != null) {
                return responseTimer.record(() -> {
                    return executeValidationRequest(requestBody, startTime);
                });
            } else {
                return executeValidationRequest(requestBody, startTime);
            }
        } catch (HttpClientErrorException e) {
            // 4xx response status codes (client errors)
            log.error("Client error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Client error: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            // 5xx response status codes (server errors)
            log.error("Server error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Server error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            // Network-related exceptions (timeouts, connection errors, etc.)
            log.error("Network error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Network error: " + e.getMessage(), e);
        } catch (TurnstileValidationException e) {
            // Re-throw the TurnstileValidationException
            recordError(ValidationResultType.INVALID_TOKEN);
            throw e;
        } catch (Exception e) {
            // Catch-all for any other unexpected exceptions
            log.error("Unexpected error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the actual validation request to Cloudflare Turnstile API.
     *
     * @param requestBody the request body to send
     * @param startTime the start time of the validation for metrics tracking
     * @return the validation result
     */
    private ValidationResult executeValidationRequest(Map<String, String> requestBody, long startTime) {
        TurnstileResponse response = turnstileRestClient.post().uri(properties.getUrl())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(requestBody).retrieve().body(TurnstileResponse.class);

        // Record response time
        long responseTime = System.currentTimeMillis() - startTime;
        lastResponseTime.set(responseTime);
        totalResponseTime.addAndGet(responseTime);
        responseCount.incrementAndGet();

        log.debug("Turnstile response: {} (took {}ms)", response, responseTime);

        if (response == null) {
            log.warn("Turnstile API returned null response");
            recordError(ValidationResultType.NETWORK_ERROR);
            return ValidationResult.networkError("Cloudflare returned an empty response");
        }

        if (response.isSuccess()) {
            log.debug("Turnstile validation successful");
            successCount.increment();
            if (successCounter != null) {
                successCounter.increment();
            }
            return ValidationResult.success();
        } else {
            log.warn("Turnstile validation failed with error codes: {}", response.getErrorCodes());
            recordError(ValidationResultType.INVALID_TOKEN);
            throw new TurnstileValidationException("Token validation failed", response.getErrorCodes());
        }
    }

    /**
     * Records an error in the metrics based on the validation result type.
     *
     * @param resultType the type of validation result
     */
    private void recordError(ValidationResultType resultType) {
        errorCount.increment();
        if (errorCounter != null) {
            errorCounter.increment();
        }

        switch (resultType) {
            case NETWORK_ERROR:
                networkErrorCount.increment();
                if (networkErrorCounter != null) {
                    networkErrorCounter.increment();
                }
                break;
            case CONFIGURATION_ERROR:
                configErrorCount.increment();
                if (configErrorCounter != null) {
                    configErrorCounter.increment();
                }
                break;
            case INVALID_TOKEN:
                validationErrorCount.increment();
                if (validationErrorCounter != null) {
                    validationErrorCounter.increment();
                }
                break;
            case INPUT_ERROR:
                inputErrorCount.increment();
                if (inputErrorCounter != null) {
                    inputErrorCounter.increment();
                }
                break;
            default:
                // No specific counter for SUCCESS or other types
                break;
        }
    }

    /**
     * Gets the total number of validation attempts.
     *
     * @return the total number of validation attempts
     */
    public long getValidationCount() {
        return validationCount.sum();
    }

    /**
     * Gets the number of successful validations.
     *
     * @return the number of successful validations
     */
    public long getSuccessCount() {
        return successCount.sum();
    }

    /**
     * Gets the number of failed validations.
     *
     * @return the number of failed validations
     */
    public long getErrorCount() {
        return errorCount.sum();
    }

    /**
     * Gets the number of network errors.
     *
     * @return the number of network errors
     */
    public long getNetworkErrorCount() {
        return networkErrorCount.sum();
    }

    /**
     * Gets the number of configuration errors.
     *
     * @return the number of configuration errors
     */
    public long getConfigErrorCount() {
        return configErrorCount.sum();
    }

    /**
     * Gets the number of validation errors (invalid tokens).
     *
     * @return the number of validation errors
     */
    public long getValidationErrorCount() {
        return validationErrorCount.sum();
    }

    /**
     * Gets the number of input validation errors.
     *
     * @return the number of input validation errors
     */
    public long getInputErrorCount() {
        return inputErrorCount.sum();
    }

    /**
     * Gets the time of the last response in milliseconds.
     *
     * @return the time of the last response in milliseconds
     */
    public long getLastResponseTime() {
        return lastResponseTime.get();
    }

    /**
     * Gets the average response time in milliseconds.
     *
     * @return the average response time in milliseconds, or 0 if no responses have been received
     */
    public double getAverageResponseTime() {
        long count = responseCount.get();
        return count > 0 ? (double) totalResponseTime.get() / count : 0;
    }

    /**
     * Gets the error rate as a percentage of total validation attempts.
     *
     * @return the error rate as a percentage (0-100), or 0 if no validation attempts have been made
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
     * @return the Turnstile Sitekey.
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
