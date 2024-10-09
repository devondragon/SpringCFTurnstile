package com.digitalsanctuary.cf.turnstile;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
    private final RestTemplate restTemplate;

    @Value("${ds.cf.turnstile.secret}")
    private String turnstileSecret; // Cloudflare Turnstile secret key

    @Value("${ds.cf.turnstile.sitekey}")
    private String turnstileSitekey;

    @Value("${ds.cf.turnstile.url}")
    private String turnstileUrl;

    /**
     * Constructor for TurnstileValidationService.
     *
     * @param restTemplate the RestTemplate to be used for making HTTP requests.
     */
    public TurnstileValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Method called after the bean is initialized. Logs the startup information including the Turnstile URL and Sitekey.
     */
    @PostConstruct
    public void onStartup() {
        log.info("TurnstileValidationService started");
        log.info("Turnstile URL: {}", turnstileUrl);
        log.info("Turnstile Sitekey: {}", turnstileSitekey);
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
        requestBody.put("secret", turnstileSecret);
        requestBody.put("response", token);
        if (remoteIp != null && !remoteIp.isEmpty()) {
            requestBody.put("remoteip", remoteIp); // Optional
        }

        // Make the request to Cloudflare's Turnstile API
        try {
            ResponseEntity<TurnstileResponse> response =
                    restTemplate.exchange(turnstileUrl, HttpMethod.POST, new HttpEntity<>(requestBody, createHeaders()), TurnstileResponse.class);
            var body = response.getBody();
            log.debug("Turnstile response: {}", body);
            return body != null && body.isSuccess();
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
        return turnstileSitekey;
    }

    /**
     * Creates HTTP headers for the request.
     *
     * @return HttpHeaders with content type set to application/json.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // JSON content type
        return headers;
    }

    public String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

