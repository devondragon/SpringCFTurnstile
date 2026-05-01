package com.digitalsanctuary.cf.turnstile;

import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.config.TurnstileHealthIndicator;
import com.digitalsanctuary.cf.turnstile.config.TurnstileMetricsConfig;
import com.digitalsanctuary.cf.turnstile.config.TurnstileServiceConfig;
import com.digitalsanctuary.cf.turnstile.filter.TurnstileCaptchaFilter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Main auto-configuration class for the Spring Cloudflare Turnstile integration.
 * <p>
 * Imports core configuration unconditionally; metrics and health configurations are
 * conditional on the presence of their respective classes on the classpath.
 * </p>
 *
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileServiceConfig
 * @see com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileMetricsConfig
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileHealthIndicator
 */
@Slf4j
@Configuration
@AutoConfiguration
@Import({TurnstileServiceConfig.class, TurnstileConfigProperties.class, TurnstileCaptchaFilter.class})
public class TurnstileConfiguration {

    /**
     * Metrics configuration for Turnstile.
     * Only imported if Micrometer's MeterRegistry is available on the classpath.
     * Uses string form of @ConditionalOnClass to avoid encoding a bytecode reference
     * to MeterRegistry in this class, which would cause NoClassDefFoundError when
     * Micrometer is absent.
     */
    @Configuration
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    @Import(TurnstileMetricsConfig.class)
    static class TurnstileMetricsConfiguration {
    }

    /**
     * Health indicator configuration for Turnstile.
     * Only imported if Spring Actuator's {@code HealthIndicator} class is on the classpath
     * and the turnstile health indicator has not been disabled via
     * {@code management.health.turnstile.enabled=false}.
     */
    @Configuration
    @ConditionalOnEnabledHealthIndicator("turnstile")
    @ConditionalOnClass(name = "org.springframework.boot.health.contributor.HealthIndicator")
    @Import(TurnstileHealthIndicator.class)
    static class TurnstileHealthConfiguration {
    }

    /**
     * Logs confirmation that the Turnstile service has been loaded.
     */
    @PostConstruct
    public void onStartup() {
        log.info("DigitalSanctuary Spring Cloudflare Turnstile Service loaded");
    }
}
