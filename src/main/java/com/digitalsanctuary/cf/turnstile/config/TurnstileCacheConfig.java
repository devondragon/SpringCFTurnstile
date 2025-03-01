package com.digitalsanctuary.cf.turnstile.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for Turnstile caching.
 * <p>
 * This class configures the cache manager and related components for the Turnstile validation service.
 * It is conditionally enabled based on the {@code ds.cf.turnstile.cache.enabled} property.
 * </p>
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ds.cf.turnstile.cache.enabled", havingValue = "true", matchIfMissing = true)
public class TurnstileCacheConfig {

    /**
     * The name of the cache used for Turnstile validation results.
     */
    public static final String TURNSTILE_VALIDATION_CACHE = "turnstileValidationCache";
    
    private final TurnstileCacheProperties cacheProperties;

    /**
     * Logs the cache configuration on startup.
     */
    @PostConstruct
    public void onStartup() {
        log.info("Turnstile caching is enabled");
        log.info("Cache TTL: {} seconds", cacheProperties.getTtlSeconds());
        log.info("Cache max size: {} entries", cacheProperties.getMaxSize());
        log.info("Cache successful validations only: {}", cacheProperties.isCacheSuccessOnly());
    }
    
    /**
     * Creates and configures the cache manager.
     * 
     * @return the configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(TURNSTILE_VALIDATION_CACHE);
        
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(cacheProperties.getTtlSeconds(), TimeUnit.SECONDS)
                .maximumSize(cacheProperties.getMaxSize())
                .recordStats();
        
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}