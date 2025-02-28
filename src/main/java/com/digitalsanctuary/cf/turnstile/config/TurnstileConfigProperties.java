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

}
