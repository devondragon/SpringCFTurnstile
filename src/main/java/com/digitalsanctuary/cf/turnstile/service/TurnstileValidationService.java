package com.digitalsanctuary.cf.turnstile.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import com.digitalsanctuary.cf.turnstile.exception.TurnstileConfigurationException;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileNetworkException;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileValidationException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating responses from Cloudflare's Turnstile API.
 * <p>
 * This service provides methods to validate Turnstile tokens with the Cloudflare API,
 * handling various error scenarios with appropriate exceptions and detailed validation
 * results.
 * </p>
 */
@Slf4j
@Service
public class TurnstileValidationService {
    private static final String UNKNOWN = "unknown";

    private final RestClient turnstileRestClient;
    private final TurnstileConfigProperties properties;

    /**
     * Constructor for TurnstileValidationService.
     *
     * @param turnstileRestClient the RestClient to use for making requests to the Turnstile API.
     * @param properties the TurnstileConfigProperties to use for configuration.
     */
    public TurnstileValidationService(@Qualifier("turnstileRestClient") RestClient turnstileRestClient, TurnstileConfigProperties properties) {
        this.turnstileRestClient = turnstileRestClient;
        this.properties = properties;
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
        
        // Validate required configuration
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            log.error("Turnstile secret key is not configured. Validation will fail.");
        }
        
        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            log.error("Turnstile URL is not configured. Validation will fail.");
        }
    }

    /**
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API.
     * This is a convenience method that doesn't require a remote IP.
     *
     * @param token the response token to be validated.
     * @return true if the response is valid and successful, false otherwise.
     */
    public boolean validateTurnstileResponse(String token) {
        return validateTurnstileResponse(token, null);
    }
    
    /**
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API.
     * This method returns a boolean result and handles all exceptions internally.
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
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API.
     * This is a convenience method that doesn't require a remote IP.
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
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API.
     * This method provides detailed validation results and throws specific exceptions for different error scenarios.
     *
     * @param token the response token to be validated.
     * @param remoteIp the remote IP address of the client (optional).
     * @return a ValidationResult object with detailed information about the validation outcome.
     * @throws TurnstileConfigurationException if the service is not properly configured
     * @throws TurnstileNetworkException if a network error occurs during validation
     * @throws TurnstileValidationException if the token is rejected by Cloudflare
     */
    public ValidationResult validateTurnstileResponseDetailed(String token, String remoteIp) {
        // Validate input parameters
        if (token == null) {
            log.warn("Turnstile validation failed: token cannot be null");
            return ValidationResult.inputError("Token cannot be null");
        }
        
        if (token.isEmpty() || token.isBlank()) {
            log.warn("Turnstile validation failed: token cannot be empty or blank");
            return ValidationResult.inputError("Token cannot be empty or blank");
        }
        
        // Basic format validation - Cloudflare tokens typically start with '0.' or '1.' followed by alphanumeric chars
        // and should be reasonably sized (typically 100+ chars)
        if (token.length() < 20) {
            log.warn("Turnstile validation failed: token appears to be too short to be valid (length: {})", token.length());
            return ValidationResult.inputError("Token is too short to be valid (length: " + token.length() + ")");
        }
        
        // Validate remoteIp if provided
        if (remoteIp != null && (remoteIp.isEmpty() || remoteIp.isBlank())) {
            log.warn("Turnstile validation: ignoring empty or blank remoteIp");
            remoteIp = null;
        }
        
        // Validate that we have the required configuration
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            String msg = "Turnstile secret key is not configured";
            log.error(msg);
            throw new TurnstileConfigurationException(msg);
        }
        
        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            String msg = "Turnstile URL is not configured";
            log.error(msg);
            throw new TurnstileConfigurationException(msg);
        }
        
        // Create a JSON request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("secret", properties.getSecret());
        requestBody.put("response", token);
        Optional.ofNullable(remoteIp).ifPresent(ip -> requestBody.put("remoteip", ip));

        // Make the request to Cloudflare's Turnstile API
        try {
            TurnstileResponse response = turnstileRestClient.post().uri(properties.getUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(requestBody)
                    .retrieve()
                    .body(TurnstileResponse.class);

            log.debug("Turnstile response: {}", response);
            
            if (response == null) {
                log.warn("Turnstile API returned null response");
                return ValidationResult.networkError("Cloudflare returned an empty response");
            }
            
            if (response.isSuccess()) {
                return ValidationResult.success();
            } else {
                log.warn("Turnstile validation failed with error codes: {}", response.getErrorCodes());
                throw new TurnstileValidationException("Token validation failed", response.getErrorCodes());
            }
            
        } catch (HttpClientErrorException e) {
            // 4xx response status codes (client errors)
            log.error("Client error during Turnstile validation: {}", e.getMessage(), e);
            throw new TurnstileNetworkException("Client error: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            // 5xx response status codes (server errors)
            log.error("Server error during Turnstile validation: {}", e.getMessage(), e);
            throw new TurnstileNetworkException("Server error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            // Network-related exceptions (timeouts, connection errors, etc.)
            log.error("Network error during Turnstile validation: {}", e.getMessage(), e);
            throw new TurnstileNetworkException("Network error: " + e.getMessage(), e);
        } catch (TurnstileValidationException e) {
            // Re-throw the TurnstileValidationException
            throw e;
        } catch (Exception e) {
            // Catch-all for any other unexpected exceptions
            log.error("Unexpected error during Turnstile validation: {}", e.getMessage(), e);
            throw new TurnstileNetworkException("Unexpected error: " + e.getMessage(), e);
        }
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
     * Gets the client IP address from the request.
     *
     * @param request the HttpServletRequest.
     * @return the client IP address.
     */
    public String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}

