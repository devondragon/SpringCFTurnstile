package com.digitalsanctuary.cf.test.turnstile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.config.TurnstileStartupReporter;
import com.digitalsanctuary.cf.turnstile.filter.TurnstileCaptchaFilter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Verifies the startup reporting behaviour of {@link TurnstileStartupReporter}: the Cloudflare
 * test-credential WARN banner fires only for test credentials, and the login filter registration
 * state is always reported at INFO (issue #106).
 */
class TurnstileStartupReporterTest {

    private static final String CLOUDFLARE_TEST_SITEKEY = "1x00000000000000000000AA";
    private static final String CLOUDFLARE_TEST_SECRET = "1x0000000000000000000000000000000AA";

    private Logger reporterLogger;
    private Level originalLevel;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        reporterLogger = (Logger) LoggerFactory.getLogger(TurnstileStartupReporter.class);
        originalLevel = reporterLogger.getLevel();
        appender = new ListAppender<>();
        appender.start();
        reporterLogger.addAppender(appender);
        reporterLogger.setLevel(Level.INFO);
    }

    @AfterEach
    void detachAppender() {
        reporterLogger.setLevel(originalLevel);
        reporterLogger.detachAppender(appender);
        appender.stop();
    }

    private TurnstileConfigProperties properties(String sitekey, String secret) {
        TurnstileConfigProperties properties = new TurnstileConfigProperties();
        properties.setSitekey(sitekey);
        properties.setSecret(secret);
        properties.setUrl("https://challenges.cloudflare.com/turnstile/v0/siteverify");
        return properties;
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<TurnstileCaptchaFilter> filterProvider(TurnstileCaptchaFilter filter) {
        ObjectProvider<TurnstileCaptchaFilter> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(provider.getIfAvailable()).thenReturn(filter);
        return provider;
    }

    private List<String> messagesAt(Level level) {
        return appender.list.stream().filter(event -> event.getLevel() == level).map(ILoggingEvent::getFormattedMessage).toList();
    }

    @Test
    void warnsWhenConfiguredWithCloudflareTestSitekey() {
        new TurnstileStartupReporter(properties(CLOUDFLARE_TEST_SITEKEY, "a-real-looking-secret-value"), filterProvider(null))
                .reportStartupState();

        assertThat(messagesAt(Level.WARN)).anyMatch(message -> message.contains("TEST credentials"));
    }

    @Test
    void warnsWhenConfiguredWithCloudflareTestSecret() {
        new TurnstileStartupReporter(properties("0x4AAAAAAARealLookingSitekey", CLOUDFLARE_TEST_SECRET), filterProvider(null))
                .reportStartupState();

        assertThat(messagesAt(Level.WARN)).anyMatch(message -> message.contains("TEST credentials"));
    }

    @Test
    void doesNotWarnWhenConfiguredWithRealLookingCredentials() {
        new TurnstileStartupReporter(properties("0x4AAAAAAARealLookingSitekey", "0x4AAAAAAARealLookingSecretValue"), filterProvider(null))
                .reportStartupState();

        assertThat(messagesAt(Level.WARN)).noneMatch(message -> message.contains("TEST credentials"));
    }

    @Test
    void bannerDescribesBothAlwaysPassAndAlwaysFailBehaviour() {
        new TurnstileStartupReporter(properties(CLOUDFLARE_TEST_SITEKEY, CLOUDFLARE_TEST_SECRET), filterProvider(null)).reportStartupState();

        String banner = String.join("\n", messagesAt(Level.WARN));
        assertThat(banner).contains("always pass").contains("always fail");
    }

    @Test
    void reportsFilterDisabledWhenFilterBeanIsAbsent() {
        new TurnstileStartupReporter(properties("0x4AAAAAAARealLookingSitekey", "0x4AAAAAAARealLookingSecretValue"), filterProvider(null))
                .reportStartupState();

        assertThat(messagesAt(Level.INFO))
                .anyMatch(message -> message.contains("ds.cf.turnstile.login.enabled") && message.contains("DISABLED"));
    }

    @Test
    void reportsFilterEnabledWhenFilterBeanIsPresent() {
        TurnstileCaptchaFilter filter = Mockito.mock(TurnstileCaptchaFilter.class);

        new TurnstileStartupReporter(properties("0x4AAAAAAARealLookingSitekey", "0x4AAAAAAARealLookingSecretValue"), filterProvider(filter))
                .reportStartupState();

        assertThat(messagesAt(Level.INFO))
                .anyMatch(message -> message.contains("ds.cf.turnstile.login.enabled") && message.contains("ENABLED"));
    }

    @Test
    void logsErrorWhenSecretIsMissing() {
        new TurnstileStartupReporter(properties("0x4AAAAAAARealLookingSitekey", "  "), filterProvider(null)).reportStartupState();

        assertThat(messagesAt(Level.ERROR)).anyMatch(message -> message.contains("secret key is not configured"));
    }

    @Test
    void logsErrorWhenUrlIsMissing() {
        TurnstileConfigProperties properties = properties("0x4AAAAAAARealLookingSitekey", "0x4AAAAAAARealLookingSecretValue");
        properties.setUrl(null);

        new TurnstileStartupReporter(properties, filterProvider(null)).reportStartupState();

        assertThat(messagesAt(Level.ERROR)).anyMatch(message -> message.contains("URL is not configured"));
    }
}
