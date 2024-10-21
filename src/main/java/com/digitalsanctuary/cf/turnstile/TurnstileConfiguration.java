package com.digitalsanctuary.cf.turnstile;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.config.TurnstileServiceConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * A configuration class for the Cloudflare Turnstile Client Service.
 * <p>
 * This class is responsible for configuring the necessary components and dependencies required by the client.
 * </p>
 * <p>
 * The {@link #onStartup()} method is annotated with {@link jakarta.annotation.PostConstruct} and is executed after the bean initialization. It logs a
 * message indicating that the DigitalSanctuary Spring AI Client has been loaded.
 * </p>
 */
@Slf4j
@Configuration
@AutoConfiguration
@Import({TurnstileServiceConfig.class, TurnstileConfigProperties.class})
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
