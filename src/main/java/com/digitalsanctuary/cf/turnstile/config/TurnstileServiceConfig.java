package com.digitalsanctuary.cf.turnstile.config;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for setting up Turnstile related beans. This class configures the RestTemplate and RestClient used for Turnstile API
 * interactions, and initializes the TurnstileValidationService.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TurnstileServiceConfig {

    private final TurnstileConfigProperties properties;
    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    /**
     * Creates a TurnstileValidationService bean.
     *
     * @param restClient the preconfigured REST client for Turnstile calls
     * @return a configured TurnstileValidationService instance
     */
    @Bean
    public TurnstileValidationService turnstileValidationService(@Qualifier("turnstileRestClient") RestClient restClient) {
        Optional<MeterRegistry> optionalRegistry = Optional.ofNullable(meterRegistryProvider.getIfAvailable());
        return new TurnstileValidationService(restClient, properties, optionalRegistry);
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

        // Configure HttpClient with timeouts
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getConnectTimeout()))
                .build();

        // Create request factory with the configured HttpClient
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
