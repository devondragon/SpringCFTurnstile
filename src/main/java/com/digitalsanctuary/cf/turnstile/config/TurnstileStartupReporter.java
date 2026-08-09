package com.digitalsanctuary.cf.turnstile.config;

import org.springframework.beans.factory.ObjectProvider;
import com.digitalsanctuary.cf.turnstile.filter.TurnstileCaptchaFilter;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Reports the Turnstile configuration state once, at application startup.
 * <p>
 * This bean is registered unconditionally by the library's auto-configuration, so the checks below run even when a consuming application supplies its
 * own {@link TurnstileValidationService} bean. It logs missing required configuration at ERROR, warns when Cloudflare test credentials are in use, and
 * reports whether the login captcha filter is registered.
 * </p>
 */
@Slf4j
public class TurnstileStartupReporter {

    private final TurnstileConfigProperties properties;
    private final ObjectProvider<TurnstileCaptchaFilter> captchaFilterProvider;

    /**
     * Constructor for TurnstileStartupReporter.
     *
     * @param properties the Turnstile configuration properties to report on
     * @param captchaFilterProvider provider used to detect whether the login captcha filter bean is registered
     */
    public TurnstileStartupReporter(TurnstileConfigProperties properties, ObjectProvider<TurnstileCaptchaFilter> captchaFilterProvider) {
        this.properties = properties;
        this.captchaFilterProvider = captchaFilterProvider;
    }

    /**
     * Logs the Turnstile startup state: missing required configuration, Cloudflare test-credential usage, and login captcha filter registration state.
     */
    @PostConstruct
    public void reportStartupState() {
        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            log.error("Turnstile secret key is not configured. Validation will fail.");
        }
        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            log.error("Turnstile URL is not configured. Validation will fail.");
        }

        if (TurnstileValidationService.isTestCredentials(properties.getSitekey(), properties.getSecret())) {
            log.warn("========================================================");
            log.warn("Turnstile is configured with Cloudflare TEST credentials.");
            log.warn("Depending on the key, validation will either always pass (NO bot");
            log.warn("protection) or always fail (ALL users blocked).");
            log.warn("Do not use these credentials in production.");
            log.warn("========================================================");
        }

        log.info("Turnstile login captcha filter (ds.cf.turnstile.login.enabled): {}",
                captchaFilterProvider.getIfAvailable() != null ? "ENABLED" : "DISABLED");
    }
}
