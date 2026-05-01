package com.digitalsanctuary.cf.turnstile.config;

import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.digitalsanctuary.cf.turnstile.metrics.MicrometerTurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for Turnstile metrics and monitoring.
 * Only loaded when Micrometer is on the classpath and metrics are enabled.
 */
@Slf4j
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "ds.cf.turnstile.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TurnstileMetricsConfig {

    /**
     * Registers the Micrometer-backed TurnstileMetrics bean.
     *
     * @param registry the MeterRegistry to use for metrics
     * @return a MicrometerTurnstileMetrics instance
     */
    @Bean
    public TurnstileMetrics micrometerTurnstileMetrics(MeterRegistry registry) {
        return new MicrometerTurnstileMetrics(registry);
    }

    /**
     * Customizes the meter registry with Turnstile-specific tags and filters.
     *
     * @return a MeterRegistryCustomizer for the MeterRegistry
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> turnstileMeterRegistryCustomizer() {
        log.info("Configuring Turnstile metrics");
        return registry -> registry.config()
                .meterFilter(MeterFilter.acceptNameStartsWith("turnstile"))
                .meterFilter(MeterFilter.commonTags(Collections.singletonList(Tag.of("component", "turnstile"))));
    }
}
