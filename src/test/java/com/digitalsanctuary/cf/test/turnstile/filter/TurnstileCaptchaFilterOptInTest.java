package com.digitalsanctuary.cf.test.turnstile.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import com.digitalsanctuary.cf.turnstile.TurnstileConfiguration;
import com.digitalsanctuary.cf.turnstile.filter.TurnstileCaptchaFilter;

/**
 * Verifies that the login captcha filter registers only when explicitly enabled via
 * {@code ds.cf.turnstile.login.enabled=true} (issue #106).
 */
class TurnstileCaptchaFilterOptInTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner().withConfiguration(AutoConfigurations.of(TurnstileConfiguration.class));

    @Test
    void filterIsNotRegisteredByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(TurnstileCaptchaFilter.class);
        });
    }

    @Test
    void filterIsRegisteredWhenLoginEnabled() {
        contextRunner.withPropertyValues("ds.cf.turnstile.login.enabled=true").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TurnstileCaptchaFilter.class);
        });
    }
}
