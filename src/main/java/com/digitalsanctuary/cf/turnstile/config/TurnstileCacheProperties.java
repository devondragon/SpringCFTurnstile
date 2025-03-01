package com.digitalsanctuary.cf.turnstile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration properties for Turnstile caching.
 * <p>
 * This class defines the configuration properties for the caching functionality of the Turnstile validation service.
 * </p>
 * <p>
 * By default, these properties are expected to be defined with the prefix {@code ds.cf.turnstile.cache}:
 * </p>
 * 
 * <pre>
 * ds:
 *   cf:
 *     turnstile:
 *       cache:
 *         enabled: true
 *         ttlSeconds: 300
 *         maxSize: 1000
 * </pre>
 */
@Data
@Component("turnstileCacheProperties")
@ConfigurationProperties(prefix = "ds.cf.turnstile.cache")
public class TurnstileCacheProperties {

    /**
     * Whether caching is enabled for Turnstile validation responses. Default is true.
     */
    private boolean enabled = true;

    /**
     * Time-to-live for cache entries in seconds. Default is 300 seconds (5 minutes).
     */
    private int ttlSeconds = 300;

    /**
     * Maximum number of entries in the cache. Default is 1000.
     */
    private int maxSize = 1000;

    /**
     * Whether to cache successful validations only. If true, only successful validations will be cached, which prevents cache poisoning. Default is
     * true.
     */
    private boolean cacheSuccessOnly = true;
}
