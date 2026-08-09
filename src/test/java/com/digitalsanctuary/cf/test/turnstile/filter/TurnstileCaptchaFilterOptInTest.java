package com.digitalsanctuary.cf.test.turnstile.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import com.digitalsanctuary.cf.turnstile.TurnstileConfiguration;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.filter.TurnstileCaptchaFilter;

/**
 * Verifies that the login captcha filter registers only when explicitly enabled via
 * {@code ds.cf.turnstile.login.enabled=true}, and that its configuration binds from both kebab-case
 * and camelCase property names (issue #106).
 */
class TurnstileCaptchaFilterOptInTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class, TurnstileConfiguration.class));

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

    @Test
    void filterConfigurationDefaultsApplyWhenUnset() {
        contextRunner.withPropertyValues("ds.cf.turnstile.login.enabled=true").run(context -> {
            assertThat(context).hasNotFailed();
            TurnstileConfigProperties properties = context.getBean(TurnstileConfigProperties.class);
            assertThat(properties.getLogin().getSubmissionPath()).isEqualTo("/login");
            assertThat(properties.getLogin().getRedirectUrl()).isEqualTo("/login?error=captcha");
            assertThat(properties.getToken().getParameterName()).isEqualTo("cf-turnstile-response");
        });
    }

    @Test
    void filterConfigurationBindsFromKebabCaseProperties() {
        contextRunner
                .withPropertyValues("ds.cf.turnstile.login.enabled=true", "ds.cf.turnstile.login.submission-path=/custom-login",
                        "ds.cf.turnstile.login.redirect-url=/custom-login?error=captcha",
                        "ds.cf.turnstile.token.parameter-name=custom-token")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(TurnstileCaptchaFilter.class);
                    TurnstileConfigProperties properties = context.getBean(TurnstileConfigProperties.class);
                    assertThat(properties.getLogin().getSubmissionPath()).isEqualTo("/custom-login");
                    assertThat(properties.getLogin().getRedirectUrl()).isEqualTo("/custom-login?error=captcha");
                    assertThat(properties.getToken().getParameterName()).isEqualTo("custom-token");
                });
    }

    @Test
    void filterConfigurationBindsFromCamelCaseProperties() {
        contextRunner
                .withPropertyValues("ds.cf.turnstile.login.enabled=true", "ds.cf.turnstile.login.submissionPath=/camel-login",
                        "ds.cf.turnstile.login.redirectUrl=/camel-login?error=captcha",
                        "ds.cf.turnstile.token.parameterName=camelToken")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(TurnstileCaptchaFilter.class);
                    TurnstileConfigProperties properties = context.getBean(TurnstileConfigProperties.class);
                    assertThat(properties.getLogin().getSubmissionPath()).isEqualTo("/camel-login");
                    assertThat(properties.getLogin().getRedirectUrl()).isEqualTo("/camel-login?error=captcha");
                    assertThat(properties.getToken().getParameterName()).isEqualTo("camelToken");
                });
    }
}
