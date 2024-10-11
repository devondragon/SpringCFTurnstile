package com.digitalsanctuary.cf.turnstile;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * A configuration class for the Spring AI Client.
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
public class TurnstileConfiguration {

    @Bean
    public RestTemplate turnstileRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * Method executed after the bean initialization.
     * <p>
     * This method logs a message indicating that the DigitalSanctuary Spring AI Client has been loaded.
     * </p>
     */
    @PostConstruct
    public void onStartup() {
        log.info("DigitalSanctuary Spring Cloudflare Turnstile Service loaded");
    }
}
