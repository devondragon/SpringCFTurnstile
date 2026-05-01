package com.digitalsanctuary.cf.turnstile.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import com.digitalsanctuary.cf.turnstile.metrics.NoOpTurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for setting up Turnstile related beans.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TurnstileServiceConfig {

    private final TurnstileConfigProperties properties;

    /**
     * Provides a no-op {@link TurnstileMetrics} bean when no other implementation is registered.
     * This fallback is active when Micrometer is absent from the classpath, when metrics are
     * disabled via {@code ds.cf.turnstile.metrics.enabled=false}, or when no custom
     * {@code TurnstileMetrics} bean has been supplied by the consuming application.
     *
     * @return a no-op TurnstileMetrics instance
     */
    @Bean
    @ConditionalOnMissingBean(TurnstileMetrics.class)
    public TurnstileMetrics noOpTurnstileMetrics() {
        log.info("Micrometer not available — using no-op Turnstile metrics");
        return new NoOpTurnstileMetrics();
    }

    /**
     * Creates a TurnstileValidationService bean.
     *
     * @param restClient the preconfigured REST client for Turnstile calls
     * @param metrics the TurnstileMetrics implementation to use
     * @return a configured TurnstileValidationService instance
     */
    @Bean
    public TurnstileValidationService turnstileValidationService(
            @Qualifier("turnstileRestClient") RestClient restClient,
            TurnstileMetrics metrics) {
        return new TurnstileValidationService(restClient, properties, metrics);
    }

    /**
     * Creates a RestClient bean for Turnstile API interactions.
     *
     * @return a configured RestClient instance
     */
    @Bean(name = "turnstileRestClient")
    public RestClient turnstileRestClient() {
        log.info("Creating Turnstile REST client with endpoint: {}", properties.getUrl());
        log.info("Turnstile REST client timeouts - connect: {}s, read: {}s",
                properties.getConnectTimeout(), properties.getReadTimeout());

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeout()))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getReadTimeout()));

        return RestClient.builder()
                .baseUrl(properties.getUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
