package com.digitalsanctuary.cf.turnstile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Configuration properties for Cloudflare Turnstile integration.
 * <p>
 * This class defines the configuration properties needed for Cloudflare Turnstile integration,
 * including the secret key, site key, and API URL. These properties are automatically bound 
 * from the application configuration (application.yml or application.properties) using Spring Boot's
 * configuration properties binding.
 * </p>
 * <p>
 * By default, these properties are expected to be defined with the prefix {@code ds.cf.turnstile}:
 * </p>
 * <pre>
 * ds:
 *   cf:
 *     turnstile:
 *       sitekey: your-turnstile-site-key
 *       secret: your-turnstile-secret-key
 *       url: https://challenges.cloudflare.com/turnstile/v0/siteverify
 *       metrics:
 *         enabled: true
 *         health-check-enabled: true
 *         error-threshold: 10
 * </pre>
 * <p>
 * To obtain your Turnstile site key and secret, you need to create a Turnstile widget in your
 * Cloudflare account dashboard.
 * </p>
 * 
 * @see <a href="https://developers.cloudflare.com/turnstile/">Cloudflare Turnstile Documentation</a>
 */
@Data
@Component
@PropertySource("classpath:config/turnstile.properties")
@ConfigurationProperties(prefix = "ds.cf.turnstile")
public class TurnstileConfigProperties {

    /**
     * The secret key used for Turnstile API authentication.
     */
    private String secret;

    /**
     * The site key used for Turnstile API authentication.
     */
    private String sitekey;

    /**
     * The URL for the Turnstile API.
     */
    private String url;

    /**
     * Connection timeout in seconds. Defaults to 5 seconds.
     */
    private int connectTimeout = 5;

    /**
     * Read timeout in seconds. Defaults to 10 seconds.
     */
    private int readTimeout = 10;

    /**
     * Configuration for metrics and monitoring.
     */
    private Metrics metrics = new Metrics();

    /**
     * Nested class for metrics configuration properties.
     */
    @Data
    public static class Metrics {

        /**
         * Whether metrics collection is enabled. Defaults to true.
         */
        private boolean enabled = true;

        /**
         * Whether health check endpoint is enabled. Defaults to true.
         */
        private boolean healthCheckEnabled = true;

        /**
         * The threshold percentage of errors that triggers health status degradation.
         * A value of 10 means that if 10% or more of validation requests fail, the health status is DOWN.
         * Defaults to 10.
         */
        private int errorThreshold = 10;
    }
}
