package com.digitalsanctuary.cf.test.turnstile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.config.TurnstileHealthIndicator;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;

/**
 * Unit tests for {@link TurnstileHealthIndicator}, including the {@code usingTestCredentials} detail
 * that surfaces Cloudflare test-credential usage through the actuator health endpoint (issue #106).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TurnstileHealthIndicatorTest {

    @Mock
    private TurnstileValidationService validationService;

    private TurnstileConfigProperties properties;
    private TurnstileHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        properties = new TurnstileConfigProperties();
        properties.setSecret("0x4AAAAAAARealLookingSecretValue");
        properties.setUrl("https://challenges.cloudflare.com/turnstile/v0/siteverify");
        healthIndicator = new TurnstileHealthIndicator(validationService, properties);
    }

    @Test
    void reportsUsingTestCredentialsTrueWhenServiceDetectsTestCredentials() {
        when(validationService.isUsingTestCredentials()).thenReturn(true);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("usingTestCredentials", true);
    }

    @Test
    void reportsUsingTestCredentialsFalseWhenServiceDetectsRealCredentials() {
        when(validationService.isUsingTestCredentials()).thenReturn(false);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("usingTestCredentials", false);
    }

    @Test
    void reportsDownWithoutTestCredentialDetailWhenSecretIsMissing() {
        properties.setSecret("  ");

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "Turnstile secret key is not configured");
    }

    @Test
    void reportsDownWhenErrorRateExceedsThreshold() {
        when(validationService.getErrorRate()).thenReturn(50.0);
        when(validationService.isUsingTestCredentials()).thenReturn(false);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("usingTestCredentials", false);
    }
}
