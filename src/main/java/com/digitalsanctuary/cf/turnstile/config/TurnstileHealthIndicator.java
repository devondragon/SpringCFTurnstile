package com.digitalsanctuary.cf.turnstile.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Health indicator for the Cloudflare Turnstile service.
 * <p>
 * This component provides health check information for the Cloudflare Turnstile service.
 * It checks if the service is properly configured and if the service has not exceeded
 * the configured error threshold. The health indicator can be disabled through configuration.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ds.cf.turnstile.metrics", name = "health-check-enabled", havingValue = "true", matchIfMissing = true)
public class TurnstileHealthIndicator implements HealthIndicator {

    private final TurnstileValidationService validationService;
    private final TurnstileConfigProperties properties;

    @Override
    public Health health() {
        try {
            // Check if the service is properly configured
            if (properties.getSecret() == null || properties.getSecret().isBlank()) {
                return Health.down().withDetail("reason", "Turnstile secret key is not configured").build();
            }

            if (properties.getUrl() == null || properties.getUrl().isBlank()) {
                return Health.down().withDetail("reason", "Turnstile URL is not configured").build();
            }

            // Check if the error rate is below the threshold
            double errorRate = validationService.getErrorRate();
            int errorThreshold = properties.getMetrics().getErrorThreshold();

            Health.Builder builder = Health.up()
                    .withDetail("url", properties.getUrl())
                    .withDetail("validationCount", validationService.getValidationCount())
                    .withDetail("successCount", validationService.getSuccessCount())
                    .withDetail("errorCount", validationService.getErrorCount())
                    .withDetail("errorRate", String.format("%.2f%%", errorRate))
                    .withDetail("responseTimeAvg", String.format("%.2fms", validationService.getAverageResponseTime()));

            // If error rate exceeds threshold, report as DOWN
            if (errorRate > errorThreshold) {
                return builder.down()
                        .withDetail("reason", "Error rate exceeded threshold: " + errorRate + "% > " + errorThreshold + "%")
                        .build();
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Error checking Turnstile service health", e);
            return Health.down(e).withDetail("reason", "Error checking service health: " + e.getMessage()).build();
        }
    }
}