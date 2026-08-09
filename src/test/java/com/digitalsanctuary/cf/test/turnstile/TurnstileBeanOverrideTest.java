package com.digitalsanctuary.cf.test.turnstile;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.digitalsanctuary.cf.turnstile.TurnstileConfiguration;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;

/**
 * Verifies that a consumer-supplied TurnstileValidationService bean replaces the library's
 * default instead of causing a bean-definition conflict (issue #106).
 */
class TurnstileBeanOverrideTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner().withConfiguration(AutoConfigurations.of(TurnstileConfiguration.class));

    @Configuration
    static class CustomServiceConfiguration {
        @Bean
        TurnstileValidationService customTurnstileValidationService() {
            return Mockito.mock(TurnstileValidationService.class);
        }
    }

    @Test
    void consumerServiceBeanReplacesDefault() {
        contextRunner.withUserConfiguration(CustomServiceConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TurnstileValidationService.class);
            assertThat(context).hasBean("customTurnstileValidationService");
        });
    }

    @Test
    void defaultServiceBeanRegistersWhenNoOverride() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TurnstileValidationService.class);
            assertThat(context).hasBean("turnstileValidationService");
        });
    }

    @Configuration
    static class CustomRestClientConfiguration {
        static final RestClient INSTANCE = RestClient.builder().build();

        @Bean(name = "turnstileRestClient")
        RestClient turnstileRestClient() {
            return INSTANCE;
        }
    }

    @Test
    void consumerRestClientBeanNamedTurnstileRestClientReplacesDefault() {
        contextRunner.withUserConfiguration(CustomRestClientConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean("turnstileRestClient")).isSameAs(CustomRestClientConfiguration.INSTANCE);
        });
    }

    @Configuration
    static class DifferentlyNamedRestClientConfiguration {
        @Bean
        RestClient someOtherRestClient() {
            return RestClient.builder().build();
        }
    }

    @Test
    void consumerRestClientBeanWithDifferentNameDoesNotReplaceDefault() {
        contextRunner.withUserConfiguration(DifferentlyNamedRestClientConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasBean("turnstileRestClient");
            assertThat(context).hasBean("someOtherRestClient");
            assertThat(context).hasSingleBean(TurnstileValidationService.class);
        });
    }
}
