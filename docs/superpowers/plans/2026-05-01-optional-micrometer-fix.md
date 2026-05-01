# Optional Micrometer Dependency Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix `NoClassDefFoundError: io/micrometer/core/instrument/MeterRegistry` when Micrometer is absent by isolating all Micrometer references behind classes that are only loaded when Micrometer is on the classpath.

**Architecture:** Introduce a `TurnstileMetrics` interface with a no-op default and a Micrometer-backed implementation. Classes that are always loaded (`TurnstileValidationService`, `TurnstileServiceConfig`, `TurnstileConfiguration`) are purged of all direct Micrometer type references. The Micrometer implementation lives in a class only loaded when `@ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")` is true.

**Tech Stack:** Java 17, Spring Boot 4.x auto-configuration, Micrometer (optional), Lombok, JUnit 5, Gradle

---

## File Map

**New files:**
- `src/main/java/com/digitalsanctuary/cf/turnstile/metrics/TurnstileMetrics.java` — interface; no Micrometer imports
- `src/main/java/com/digitalsanctuary/cf/turnstile/metrics/NoOpTurnstileMetrics.java` — active when Micrometer absent; no Micrometer imports
- `src/main/java/com/digitalsanctuary/cf/turnstile/metrics/MicrometerTurnstileMetrics.java` — active when Micrometer present; contains all Micrometer imports/references
- `src/test/java/com/digitalsanctuary/cf/test/turnstile/TurnstileWithoutActuatorTest.java` — Spring context test that excludes MeterRegistry to verify no-actuator loading

**Modified files:**
- `src/main/java/com/digitalsanctuary/cf/turnstile/service/TurnstileValidationService.java` — remove `Optional<MeterRegistry>`, `Counter`, `Timer` fields; take `TurnstileMetrics` instead
- `src/main/java/com/digitalsanctuary/cf/turnstile/config/TurnstileServiceConfig.java` — remove `ObjectProvider<MeterRegistry>`; add `@ConditionalOnMissingBean` no-op provider; wire `TurnstileMetrics` into service
- `src/main/java/com/digitalsanctuary/cf/turnstile/config/TurnstileMetricsConfig.java` — add bean that registers `MicrometerTurnstileMetrics` into Spring context
- `src/main/java/com/digitalsanctuary/cf/turnstile/TurnstileConfiguration.java` — switch `@ConditionalOnClass(MeterRegistry.class)` to string form; remove Micrometer import

---

## Task 1: Create `TurnstileMetrics` interface

**Files:**
- Create: `src/main/java/com/digitalsanctuary/cf/turnstile/metrics/TurnstileMetrics.java`

- [ ] **Step 1: Create the interface**

```java
package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;

/**
 * Abstraction for recording Turnstile validation metrics.
 * Implementations may be no-op (when Micrometer is absent) or Micrometer-backed.
 */
public interface TurnstileMetrics {
    void recordValidation();
    void recordSuccess();
    void recordError(ValidationResultType type);
    void recordResponseTime(long milliseconds);
}
```

- [ ] **Step 2: Compile to verify no issues**

```bash
./gradlew compileJava
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/digitalsanctuary/cf/turnstile/metrics/TurnstileMetrics.java
git commit -m "feat: add TurnstileMetrics interface for optional Micrometer support"
```

---

## Task 2: Create `NoOpTurnstileMetrics`

**Files:**
- Create: `src/main/java/com/digitalsanctuary/cf/turnstile/metrics/NoOpTurnstileMetrics.java`

- [ ] **Step 1: Create the no-op implementation**

```java
package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;

/**
 * No-op implementation of TurnstileMetrics used when Micrometer is not on the classpath.
 */
public class NoOpTurnstileMetrics implements TurnstileMetrics {

    @Override
    public void recordValidation() {}

    @Override
    public void recordSuccess() {}

    @Override
    public void recordError(ValidationResultType type) {}

    @Override
    public void recordResponseTime(long milliseconds) {}
}
```

- [ ] **Step 2: Compile to verify**

```bash
./gradlew compileJava
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/digitalsanctuary/cf/turnstile/metrics/NoOpTurnstileMetrics.java
git commit -m "feat: add NoOpTurnstileMetrics for when Micrometer is absent"
```

---

## Task 3: Create `MicrometerTurnstileMetrics`

This class contains ALL Micrometer imports for the library. It must never be referenced as a type in any class that loads unconditionally.

**Files:**
- Create: `src/main/java/com/digitalsanctuary/cf/turnstile/metrics/MicrometerTurnstileMetrics.java`

- [ ] **Step 1: Create the Micrometer implementation**

```java
package com.digitalsanctuary.cf.turnstile.metrics;

import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Micrometer-backed implementation of TurnstileMetrics.
 * This class is only instantiated when Micrometer is present on the classpath.
 */
@Slf4j
public class MicrometerTurnstileMetrics implements TurnstileMetrics {

    private final Counter validationCounter;
    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter networkErrorCounter;
    private final Counter configErrorCounter;
    private final Counter validationErrorCounter;
    private final Counter inputErrorCounter;
    private final Timer responseTimer;

    public MicrometerTurnstileMetrics(MeterRegistry registry) {
        log.info("Initializing Turnstile metrics with MeterRegistry");
        validationCounter = Counter.builder("turnstile.validation.requests")
                .description("Total number of Turnstile validation requests").register(registry);
        successCounter = Counter.builder("turnstile.validation.success")
                .description("Number of successful Turnstile validations").register(registry);
        errorCounter = Counter.builder("turnstile.validation.errors")
                .description("Number of failed Turnstile validations").register(registry);
        networkErrorCounter = Counter.builder("turnstile.validation.errors.network")
                .description("Number of Turnstile validation network errors").register(registry);
        configErrorCounter = Counter.builder("turnstile.validation.errors.config")
                .description("Number of Turnstile validation configuration errors").register(registry);
        validationErrorCounter = Counter.builder("turnstile.validation.errors.token")
                .description("Number of Turnstile validation token errors").register(registry);
        inputErrorCounter = Counter.builder("turnstile.validation.errors.input")
                .description("Number of Turnstile validation input errors").register(registry);
        responseTimer = Timer.builder("turnstile.validation.response.time")
                .description("Response time for Turnstile validation requests").register(registry);
    }

    @Override
    public void recordValidation() {
        validationCounter.increment();
    }

    @Override
    public void recordSuccess() {
        successCounter.increment();
    }

    @Override
    public void recordError(ValidationResultType type) {
        errorCounter.increment();
        switch (type) {
            case NETWORK_ERROR -> networkErrorCounter.increment();
            case CONFIGURATION_ERROR -> configErrorCounter.increment();
            case INVALID_TOKEN -> validationErrorCounter.increment();
            case INPUT_ERROR -> inputErrorCounter.increment();
            default -> {}
        }
    }

    @Override
    public void recordResponseTime(long milliseconds) {
        responseTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}
```

- [ ] **Step 2: Compile to verify**

```bash
./gradlew compileJava
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/digitalsanctuary/cf/turnstile/metrics/MicrometerTurnstileMetrics.java
git commit -m "feat: add MicrometerTurnstileMetrics implementation"
```

---

## Task 4: Refactor `TurnstileValidationService` to use `TurnstileMetrics`

Remove all Micrometer imports and type references. Replace `Optional<MeterRegistry>` + individual counter/timer fields with a single `TurnstileMetrics metrics` field.

**Files:**
- Modify: `src/main/java/com/digitalsanctuary/cf/turnstile/service/TurnstileValidationService.java`

- [ ] **Step 1: Replace the class with the refactored version**

Replace the entire file content with:

```java
package com.digitalsanctuary.cf.turnstile.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.dto.TurnstileResponse;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileConfigurationException;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileNetworkException;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileValidationException;
import com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating responses from Cloudflare's Turnstile API.
 * <p>
 * This service provides methods to validate Turnstile tokens with the Cloudflare API, handling various error scenarios with appropriate exceptions
 * and detailed validation results. It also collects metrics on validation attempts, success/failure rates, and response times when metrics are
 * enabled.
 * </p>
 */
@Slf4j
@Service
public class TurnstileValidationService {
    private static final String UNKNOWN = "unknown";
    private static final int MIN_TOKEN_LENGTH = 20;

    private final RestClient turnstileRestClient;
    private final TurnstileConfigProperties properties;
    private final TurnstileMetrics metrics;

    // Internal counters (always active, independent of Micrometer)
    private final LongAdder validationCount = new LongAdder();
    private final LongAdder successCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();
    private final LongAdder networkErrorCount = new LongAdder();
    private final LongAdder configErrorCount = new LongAdder();
    private final LongAdder validationErrorCount = new LongAdder();
    private final LongAdder inputErrorCount = new LongAdder();
    private final AtomicLong lastResponseTime = new AtomicLong();
    private final AtomicLong totalResponseTime = new AtomicLong();
    private final AtomicLong responseCount = new AtomicLong();

    /**
     * Constructor for TurnstileValidationService.
     *
     * @param turnstileRestClient the RestClient to use for making requests to the Turnstile API
     * @param properties the TurnstileConfigProperties to use for configuration
     * @param metrics the TurnstileMetrics implementation for recording metrics
     */
    public TurnstileValidationService(@Qualifier("turnstileRestClient") RestClient turnstileRestClient,
            TurnstileConfigProperties properties, TurnstileMetrics metrics) {
        this.turnstileRestClient = turnstileRestClient;
        this.properties = properties;
        this.metrics = metrics;
    }

    /**
     * Method called after the bean is initialized. Logs the startup information and validates the required configuration.
     *
     * @throws TurnstileConfigurationException if required configuration properties are missing
     */
    @PostConstruct
    public void onStartup() {
        log.info("TurnstileValidationService started");
        log.info("Turnstile URL: {}", properties.getUrl());
        log.info("Turnstile Sitekey: {}", properties.getSitekey());
        log.info("Turnstile Secret: {}", properties.getSecret() != null && !properties.getSecret().isBlank() ? "[CONFIGURED]" : "[NOT CONFIGURED]");
        log.info("Turnstile Metrics enabled: {}", properties.getMetrics().isEnabled());
        log.info("Turnstile Health Check enabled: {}", properties.getMetrics().isHealthCheckEnabled());

        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            log.error("Turnstile secret key is not configured. Validation will fail.");
        }
        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            log.error("Turnstile URL is not configured. Validation will fail.");
        }
    }

    /**
     * Validates the Turnstile response token. Convenience method without remote IP.
     *
     * @param token the response token to be validated.
     * @return true if the response is valid and successful, false otherwise.
     */
    public boolean validateTurnstileResponse(String token) {
        return validateTurnstileResponse(token, null);
    }

    /**
     * Validates the Turnstile response token. Returns boolean and handles exceptions internally.
     *
     * @param token the response token to be validated.
     * @param remoteIp the remote IP address of the client (optional).
     * @return true if the response is valid and successful, false otherwise.
     */
    public boolean validateTurnstileResponse(String token, String remoteIp) {
        try {
            ValidationResult result = validateTurnstileResponseDetailed(token, remoteIp);
            return result.isSuccess();
        } catch (Exception e) {
            log.error("Unexpected error during Turnstile validation: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates the Turnstile response token. Convenience method without remote IP.
     *
     * @param token the response token to be validated.
     * @return a ValidationResult object with detailed information about the validation outcome.
     * @throws TurnstileConfigurationException if the service is not properly configured
     * @throws TurnstileNetworkException if a network error occurs during validation
     * @throws TurnstileValidationException if the token is rejected by Cloudflare
     */
    public ValidationResult validateTurnstileResponseDetailed(String token) {
        return validateTurnstileResponseDetailed(token, null);
    }

    /**
     * Validates the Turnstile response token with detailed results and typed exceptions.
     *
     * @param token the response token to be validated.
     * @param remoteIp the remote IP address of the client (optional).
     * @return a ValidationResult object with detailed information about the validation outcome.
     * @throws TurnstileConfigurationException if the service is not properly configured
     * @throws TurnstileNetworkException if a network error occurs during validation
     * @throws TurnstileValidationException if the token is rejected by Cloudflare
     */
    public ValidationResult validateTurnstileResponseDetailed(String token, String remoteIp) {
        long startTime = System.currentTimeMillis();
        validationCount.increment();
        metrics.recordValidation();

        log.trace("Starting validation for token: {} with remoteIp: {}", token, remoteIp);

        if (token == null) {
            log.warn("Turnstile validation failed: token cannot be null");
            recordError(ValidationResultType.INPUT_ERROR);
            return ValidationResult.inputError("Token cannot be null");
        }

        if (token.isEmpty() || token.isBlank()) {
            log.warn("Turnstile validation failed: token cannot be empty or blank");
            recordError(ValidationResultType.INPUT_ERROR);
            return ValidationResult.inputError("Token cannot be empty or blank");
        }

        if (token.length() < MIN_TOKEN_LENGTH) {
            log.warn("Turnstile validation failed: token appears to be too short to be valid (length: {})", token.length());
            recordError(ValidationResultType.INPUT_ERROR);
            return ValidationResult.inputError("Token is too short to be valid (length: " + token.length() + ")");
        }

        String cleanRemoteIp = remoteIp;
        if (cleanRemoteIp != null && (cleanRemoteIp.isEmpty() || cleanRemoteIp.isBlank())) {
            log.warn("Turnstile validation: ignoring empty or blank remoteIp");
            cleanRemoteIp = null;
        }

        if (properties.getSecret() == null || properties.getSecret().isBlank()) {
            String msg = "Turnstile secret key is not configured";
            log.error(msg);
            recordError(ValidationResultType.CONFIGURATION_ERROR);
            throw new TurnstileConfigurationException(msg);
        }

        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            String msg = "Turnstile URL is not configured";
            log.error(msg);
            recordError(ValidationResultType.CONFIGURATION_ERROR);
            throw new TurnstileConfigurationException(msg);
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("secret", properties.getSecret());
        requestBody.put("response", token);
        Optional.ofNullable(cleanRemoteIp).ifPresent(ip -> requestBody.put("remoteip", ip));

        log.trace("Making request to Cloudflare Turnstile API at: {}", properties.getUrl());

        try {
            ValidationResult result = executeValidationRequest(requestBody);
            long elapsed = System.currentTimeMillis() - startTime;
            lastResponseTime.set(elapsed);
            totalResponseTime.addAndGet(elapsed);
            responseCount.incrementAndGet();
            metrics.recordResponseTime(elapsed);
            return result;
        } catch (HttpClientErrorException e) {
            log.error("Client error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Client error: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("Server error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Server error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            log.error("Network error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Network error: " + e.getMessage(), e);
        } catch (TurnstileValidationException e) {
            recordError(ValidationResultType.INVALID_TOKEN);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Turnstile validation: {}", e.getMessage(), e);
            recordError(ValidationResultType.NETWORK_ERROR);
            throw new TurnstileNetworkException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private ValidationResult executeValidationRequest(Map<String, String> requestBody) {
        TurnstileResponse response = turnstileRestClient.post().uri(properties.getUrl())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(requestBody).retrieve().body(TurnstileResponse.class);

        log.debug("Turnstile response: {}", response);

        if (response == null) {
            log.warn("Turnstile API returned null response");
            recordError(ValidationResultType.NETWORK_ERROR);
            return ValidationResult.networkError("Cloudflare returned an empty response");
        }

        if (response.isSuccess()) {
            log.debug("Turnstile validation successful");
            successCount.increment();
            metrics.recordSuccess();
            return ValidationResult.success();
        } else {
            log.warn("Turnstile validation failed with error codes: {}", response.getErrorCodes());
            recordError(ValidationResultType.INVALID_TOKEN);
            throw new TurnstileValidationException("Token validation failed", response.getErrorCodes());
        }
    }

    private void recordError(ValidationResultType resultType) {
        errorCount.increment();
        metrics.recordError(resultType);

        switch (resultType) {
            case NETWORK_ERROR -> networkErrorCount.increment();
            case CONFIGURATION_ERROR -> configErrorCount.increment();
            case INVALID_TOKEN -> validationErrorCount.increment();
            case INPUT_ERROR -> inputErrorCount.increment();
            default -> {}
        }
    }

    /** @return total number of validation attempts */
    public long getValidationCount() { return validationCount.sum(); }

    /** @return number of successful validations */
    public long getSuccessCount() { return successCount.sum(); }

    /** @return number of failed validations */
    public long getErrorCount() { return errorCount.sum(); }

    /** @return number of network errors */
    public long getNetworkErrorCount() { return networkErrorCount.sum(); }

    /** @return number of configuration errors */
    public long getConfigErrorCount() { return configErrorCount.sum(); }

    /** @return number of validation errors (invalid tokens) */
    public long getValidationErrorCount() { return validationErrorCount.sum(); }

    /** @return number of input validation errors */
    public long getInputErrorCount() { return inputErrorCount.sum(); }

    /** @return time of last response in milliseconds */
    public long getLastResponseTime() { return lastResponseTime.get(); }

    /** @return average response time in milliseconds, or 0 if no responses yet */
    public double getAverageResponseTime() {
        long count = responseCount.get();
        return count > 0 ? (double) totalResponseTime.get() / count : 0;
    }

    /** @return error rate as a percentage (0-100), or 0 if no attempts yet */
    public double getErrorRate() {
        long total = validationCount.sum();
        return total > 0 ? (double) errorCount.sum() * 100 / total : 0;
    }

    /**
     * @deprecated Use {@link #getTurnstileSitekey()} instead.
     */
    @Deprecated
    public String getTurnsiteSitekey() { return getTurnstileSitekey(); }

    /** @return the Turnstile Sitekey */
    public String getTurnstileSitekey() { return properties.getSitekey(); }

    /**
     * Gets the client IP address from the ServletRequest.
     *
     * @param request the ServletRequest.
     * @return the client IP address.
     */
    public String getClientIpAddress(ServletRequest request) {
        if (request instanceof HttpServletRequest httpRequest) {
            String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
            for (String header : headers) {
                String ipHeaderValue = httpRequest.getHeader(header);
                if (ipHeaderValue == null || ipHeaderValue.isBlank()) {
                    continue;
                }
                String candidate = ipHeaderValue.split(",", 2)[0].trim();
                if (!candidate.isEmpty() && !UNKNOWN.equalsIgnoreCase(candidate)) {
                    return candidate;
                }
            }
        }
        return request.getRemoteAddr();
    }
}
```

- [ ] **Step 2: Compile to verify**

```bash
./gradlew compileJava
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run existing tests to verify no regressions**

```bash
./gradlew test
```
Expected: All tests pass (they will fail until Task 5 wires the `TurnstileMetrics` bean — the constructor signature changed, so Spring context won't load yet). If they fail with `NoSuchBeanDefinitionException: TurnstileMetrics`, that's expected — continue to Task 5.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/digitalsanctuary/cf/turnstile/service/TurnstileValidationService.java
git commit -m "refactor: remove Micrometer imports from TurnstileValidationService, use TurnstileMetrics interface"
```

---

## Task 5: Register `TurnstileMetrics` beans in configuration

Wire the correct `TurnstileMetrics` bean into Spring context: `NoOpTurnstileMetrics` when Micrometer is absent (via `@ConditionalOnMissingBean`), `MicrometerTurnstileMetrics` when Micrometer is present (registered in `TurnstileMetricsConfig`).

**Files:**
- Modify: `src/main/java/com/digitalsanctuary/cf/turnstile/config/TurnstileServiceConfig.java`
- Modify: `src/main/java/com/digitalsanctuary/cf/turnstile/config/TurnstileMetricsConfig.java`

- [ ] **Step 1: Replace `TurnstileServiceConfig` — remove `ObjectProvider<MeterRegistry>`, add no-op bean, fix service constructor call**

Replace the entire file content with:

```java
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
     * Provides a no-op TurnstileMetrics bean when no other implementation is registered.
     * This is the fallback when Micrometer is not on the classpath.
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
```

- [ ] **Step 2: Add `MicrometerTurnstileMetrics` bean to `TurnstileMetricsConfig`**

Replace the entire file content with:

```java
package com.digitalsanctuary.cf.turnstile.config;

import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.digitalsanctuary.cf.turnstile.metrics.MicrometerTurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for Turnstile metrics and monitoring.
 * Only loaded when Micrometer is on the classpath and metrics are enabled.
 */
@Slf4j
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "ds.cf.turnstile.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TurnstileMetricsConfig {

    /**
     * Registers the Micrometer-backed TurnstileMetrics bean.
     *
     * @param registry the MeterRegistry to use for metrics
     * @return a MicrometerTurnstileMetrics instance
     */
    @Bean
    public TurnstileMetrics micrometerTurnstileMetrics(MeterRegistry registry) {
        return new MicrometerTurnstileMetrics(registry);
    }

    /**
     * Customizes the meter registry with Turnstile-specific tags and filters.
     *
     * @return a MeterRegistryCustomizer for the MeterRegistry
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> turnstileMeterRegistryCustomizer() {
        log.info("Configuring Turnstile metrics");
        return registry -> registry.config()
                .meterFilter(MeterFilter.acceptNameStartsWith("turnstile"))
                .meterFilter(MeterFilter.commonTags(Collections.singletonList(Tag.of("component", "turnstile"))));
    }
}
```

- [ ] **Step 3: Compile to verify**

```bash
./gradlew compileJava
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Run existing tests**

```bash
./gradlew test
```
Expected: All existing tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/digitalsanctuary/cf/turnstile/config/TurnstileServiceConfig.java \
        src/main/java/com/digitalsanctuary/cf/turnstile/config/TurnstileMetricsConfig.java
git commit -m "feat: wire TurnstileMetrics beans — Micrometer-backed or no-op based on classpath"
```

---

## Task 6: Fix `TurnstileConfiguration` — remove direct Micrometer import, use string-form `@ConditionalOnClass`

The outer auto-configuration class references `MeterRegistry` as a class literal in an annotation, which encodes it into the bytecode. Switch to the safe string form.

**Files:**
- Modify: `src/main/java/com/digitalsanctuary/cf/turnstile/TurnstileConfiguration.java`

- [ ] **Step 1: Update `TurnstileConfiguration`**

Replace the entire file content with:

```java
package com.digitalsanctuary.cf.turnstile;

import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.config.TurnstileHealthIndicator;
import com.digitalsanctuary.cf.turnstile.config.TurnstileMetricsConfig;
import com.digitalsanctuary.cf.turnstile.config.TurnstileServiceConfig;
import com.digitalsanctuary.cf.turnstile.filter.TurnstileCaptchaFilter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Main auto-configuration class for the Spring Cloudflare Turnstile integration.
 * <p>
 * Imports core configuration unconditionally; metrics and health configurations are
 * conditional on the presence of their respective classes on the classpath.
 * </p>
 *
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileServiceConfig
 * @see com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileMetricsConfig
 * @see com.digitalsanctuary.cf.turnstile.config.TurnstileHealthIndicator
 */
@Slf4j
@Configuration
@AutoConfiguration
@Import({TurnstileServiceConfig.class, TurnstileConfigProperties.class, TurnstileCaptchaFilter.class})
public class TurnstileConfiguration {

    /**
     * Metrics configuration for Turnstile.
     * Only imported if Micrometer's MeterRegistry is available on the classpath.
     * Uses string form of @ConditionalOnClass to avoid encoding a bytecode reference
     * to MeterRegistry in this class, which would cause NoClassDefFoundError when
     * Micrometer is absent.
     */
    @Configuration
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    @Import(TurnstileMetricsConfig.class)
    static class TurnstileMetricsConfiguration {
    }

    /**
     * Health indicator configuration for Turnstile.
     * Only imported if Spring Actuator's HealthIndicator is available on the classpath.
     */
    @Configuration
    @ConditionalOnEnabledHealthIndicator("turnstile")
    @ConditionalOnClass(name = "org.springframework.boot.health.contributor.HealthIndicator")
    @Import(TurnstileHealthIndicator.class)
    static class TurnstileHealthConfiguration {
    }

    /**
     * Logs confirmation that the Turnstile service has been loaded.
     */
    @PostConstruct
    public void onStartup() {
        log.info("DigitalSanctuary Spring Cloudflare Turnstile Service loaded");
    }
}
```

- [ ] **Step 2: Compile to verify**

```bash
./gradlew compileJava
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run all tests**

```bash
./gradlew test
```
Expected: All tests pass.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/digitalsanctuary/cf/turnstile/TurnstileConfiguration.java
git commit -m "fix: use string-form @ConditionalOnClass in TurnstileConfiguration to prevent NoClassDefFoundError when Micrometer is absent"
```

---

## Task 7: Add test verifying the library loads without Actuator

Write a Spring context test that excludes `MeterRegistry` from the context, simulating a consumer app without Actuator. This is the regression test for issue #94.

**Files:**
- Create: `src/test/java/com/digitalsanctuary/cf/test/turnstile/TurnstileWithoutActuatorTest.java`

- [ ] **Step 1: Create the test**

```java
package com.digitalsanctuary.cf.test.turnstile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.digitalsanctuary.cf.test.TestApplication;
import com.digitalsanctuary.cf.turnstile.metrics.NoOpTurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.metrics.TurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;

/**
 * Verifies that the library starts successfully without Micrometer/Actuator on the classpath.
 * Simulates this by excluding all Micrometer-related auto-configurations.
 * Regression test for GitHub issue #94.
 */
@SpringBootTest(
    classes = TestApplication.class,
    properties = {
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration," +
            "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
    }
)
@ActiveProfiles("test")
class TurnstileWithoutActuatorTest {

    @Autowired
    private TurnstileValidationService turnstileValidationService;

    @Autowired
    private TurnstileMetrics turnstileMetrics;

    @Test
    void contextLoadsWithoutMicrometer() {
        assertNotNull(turnstileValidationService, "TurnstileValidationService should be available");
    }

    @Test
    void noOpMetricsUsedWhenMicrometerExcluded() {
        assertInstanceOf(NoOpTurnstileMetrics.class, turnstileMetrics,
            "Should use NoOpTurnstileMetrics when Micrometer auto-config is excluded");
    }

    @Test
    void validationWorksWithoutMicrometer() {
        // Short token triggers INPUT_ERROR path — exercises metrics recording via NoOp
        boolean result = turnstileValidationService.validateTurnstileResponse("short");
        assertNotNull(result); // just verifying no exception from metrics recording
    }
}
```

- [ ] **Step 2: Run the new test**

```bash
./gradlew test --tests "com.digitalsanctuary.cf.test.turnstile.TurnstileWithoutActuatorTest"
```
Expected: All 3 tests pass.

- [ ] **Step 3: Run the full test suite**

```bash
./gradlew test
```
Expected: All tests pass.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/digitalsanctuary/cf/test/turnstile/TurnstileWithoutActuatorTest.java
git commit -m "test: add regression test for issue #94 — library loads without Actuator"
```

---

## Task 8: Final verification

- [ ] **Step 1: Clean build**

```bash
./gradlew clean build
```
Expected: `BUILD SUCCESSFUL`, all tests pass.

- [ ] **Step 2: Verify no stray Micrometer imports in always-loaded classes**

```bash
grep -r "import io.micrometer" \
  src/main/java/com/digitalsanctuary/cf/turnstile/TurnstileConfiguration.java \
  src/main/java/com/digitalsanctuary/cf/turnstile/config/TurnstileServiceConfig.java \
  src/main/java/com/digitalsanctuary/cf/turnstile/service/TurnstileValidationService.java
```
Expected: No output (zero matches).

- [ ] **Step 3: Verify Micrometer imports are only in conditional classes**

```bash
grep -r "import io.micrometer" src/main/java/ --include="*.java" -l
```
Expected: Only `TurnstileMetricsConfig.java` and `MicrometerTurnstileMetrics.java` appear.

- [ ] **Step 4: Publish to local Maven and verify POM**

```bash
./gradlew publishToMavenLocal
```
Expected: `BUILD SUCCESSFUL`. Verify `spring-boot-starter-actuator` is listed as `optional` or absent from the published POM's `<dependencies>` (it was `compileOnly` so it should not appear in the POM).

- [ ] **Step 5: Commit final verification results**

No code changes; if all checks pass, proceed to create a PR.

---

## Self-Review

**Spec coverage:**
- ✅ `NoClassDefFoundError` when Micrometer absent — fixed by removing type references from always-loaded classes
- ✅ Micrometer metrics still work when Actuator is present — `MicrometerTurnstileMetrics` registered via `TurnstileMetricsConfig`
- ✅ Health indicator still works (not touched — already uses string-form `@ConditionalOnClass`)
- ✅ `@ConditionalOnClass` uses safe string form in `TurnstileConfiguration`
- ✅ Regression test added

**Type consistency check:**
- `TurnstileMetrics` interface methods: `recordValidation()`, `recordSuccess()`, `recordError(ValidationResultType)`, `recordResponseTime(long)` — consistent across `NoOpTurnstileMetrics`, `MicrometerTurnstileMetrics`, and all call sites in `TurnstileValidationService`
- `TurnstileServiceConfig.turnstileValidationService(RestClient, TurnstileMetrics)` — matches `TurnstileValidationService` constructor signature

**Placeholder scan:** None found — all steps contain exact code.
