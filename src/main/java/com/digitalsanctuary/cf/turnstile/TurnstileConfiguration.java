package com.digitalsanctuary.cf.turnstile;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.digitalsanctuary.cf.turnstile.config.TurnstileCacheConfig;
import com.digitalsanctuary.cf.turnstile.config.TurnstileCacheProperties;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.config.TurnstileServiceConfig;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Main auto-configuration class for the Spring Cloudflare Turnstile integration.
 * <p>
 * This class serves as the entry point for Spring Boot's auto-configuration mechanism to
 * automatically set up Cloudflare Turnstile integration when the library is included in a project.
 * It imports the necessary configuration components such as property management and service configuration.
 * </p>
 * <p>
 * To use this auto-configuration, include this library in your Spring Boot project and configure
 * the required properties in your application.yml or application.properties file:
 * </p>
 * <pre>
 * ds:
 *   cf:
 *     turnstile:
 *       sitekey: your-turnstile-site-key
 *       secret: your-turnstile-secret-key
 *       url: https://challenges.cloudflare.com/turnstile/v0/siteverify
 *       cache:
 *         enabled: true
 *         ttlSeconds: 300
 *         maxSize: 1000
 *         cacheSuccessOnly: true
 * </pre>
 * <p>
 * The {@link #onStartup()} method is annotated with {@link jakarta.annotation.PostConstruct} and is executed 
 * after the bean initialization to log a confirmation message that the Cloudflare Turnstile Service has been loaded.
 * </p>
 * 
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileServiceConfig
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileCacheProperties
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileCacheConfig
 * @see com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService
 */
@Slf4j
@Configuration
@AutoConfiguration
@Import({
    TurnstileServiceConfig.class, 
    TurnstileConfigProperties.class,
    TurnstileCacheProperties.class,
    TurnstileCacheConfig.class
})
public class TurnstileConfiguration {

    /**
     * Method executed after the bean initialization.
     * <p>
     * This method logs a message indicating that the DigitalSanctuary Cloudflare Turnstile Service has been loaded.
     * </p>
     */
    @PostConstruct
    public void onStartup() {
        log.info("DigitalSanctuary Spring Cloudflare Turnstile Service loaded");
    }
}
