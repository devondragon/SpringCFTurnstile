package com.digitalsanctuary.cf.turnstile.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.dto.TurnstileResponse;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating responses from Cloudflare's Turnstile API. This service uses a RestTemplate to send validation requests and process
 * responses.
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
     * Method called after the bean is initialized. Logs the startup information including the Turnstile URL and Sitekey.
     */
    @PostConstruct
    public void onStartup() {
        log.info("TurnstileValidationService started");
        log.info("Turnstile URL: {}", properties.getUrl());
        log.info("Turnstile Sitekey: {}", properties.getSitekey());
    }

    /**
     * Validates the Turnstile response token by making a request to Cloudflare's Turnstile API.
     *
     * @param token the response token to be validated.
     * @param remoteIp the remote IP address of the client (optional).
     * @return true if the response is valid and successful, false otherwise.
     */
    public boolean validateTurnstileResponse(String token, String remoteIp) {
        // Create a JSON request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("secret", properties.getSecret());
        requestBody.put("response", token);
        Optional.ofNullable(remoteIp).ifPresent(ip -> requestBody.put("remoteip", ip));

        // Make the request to Cloudflare's Turnstile API
        try {
            TurnstileResponse response = turnstileRestClient.post().uri(properties.getUrl())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(requestBody).retrieve().body(TurnstileResponse.class);

            log.debug("Turnstile response: {}", response);
            return response != null && response.isSuccess();
        } catch (Exception e) {
            log.error("Error validating Turnstile response: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets the Turnstile Sitekey.
     *
     * @return the Turnstile Sitekey.
     */
    public String getTurnsiteSitekey() {
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

