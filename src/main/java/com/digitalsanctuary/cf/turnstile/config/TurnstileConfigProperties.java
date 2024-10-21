package com.digitalsanctuary.cf.turnstile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * Configuration properties for Turnstile integration. This class holds the properties required to configure the Turnstile service, such as the secret
 * key, site key, and the URL for the Turnstile API.
 */
@Data
@Component
@PropertySource("classpath:config/turnstile.properties")
@ConfigurationProperties(prefix = "ds.cf.turnstile")
public class TurnstileConfigProperties {

    /**
     * The secret key used for Turnstile API authentication.
     */
    private String secret;

    /**
     * The site key used for Turnstile API authentication.
     */
    private String sitekey;

    /**
     * The URL for the Turnstile API.
     */
    private String url;

}
