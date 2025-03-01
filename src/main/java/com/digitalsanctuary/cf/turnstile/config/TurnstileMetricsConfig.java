package com.digitalsanctuary.cf.turnstile.config;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for Turnstile metrics and monitoring.
 * <p>
 * This class configures the metrics for Cloudflare Turnstile service. It sets up common
 * tags and filters for all metrics related to the Turnstile service. The metrics are only
 * configured if micrometer-core is on the classpath and metrics are enabled in the configuration.
 * </p>
 */
@Slf4j
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "ds.cf.turnstile.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TurnstileMetricsConfig {

    /**
     * Customizes the meter registry for Turnstile metrics.
     * <p>
     * Adds a common tag 'component:turnstile' to all Turnstile-related metrics
     * and configures a prefix for all Turnstile metrics.
     * </p>
     *
     * @return a MeterRegistryCustomizer to customize the MeterRegistry
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> turnstileMeterRegistryCustomizer() {
        log.info("Configuring Turnstile metrics");
        return registry -> {
            // Add a common tag to all turnstile metrics
            registry.config()
                    .meterFilter(MeterFilter.acceptNameStartsWith("turnstile"))
                    .meterFilter(MeterFilter.commonTags(Collections.singletonList(Tag.of("component", "turnstile"))));
        };
    }
}