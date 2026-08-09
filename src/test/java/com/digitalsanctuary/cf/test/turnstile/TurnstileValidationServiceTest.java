package com.digitalsanctuary.cf.test.turnstile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.digitalsanctuary.cf.test.TestApplication;
import com.digitalsanctuary.cf.turnstile.config.TurnstileConfigProperties;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;
import com.digitalsanctuary.cf.turnstile.metrics.NoOpTurnstileMetrics;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;
import lombok.extern.slf4j.Slf4j;

/**
 * Unit tests for the TurnstileValidationService.
 * <p>
 * This class tests the basic and detailed validation methods of the TurnstileValidationService. It includes tests for valid and invalid tokens, as
 * well as convenience methods without IP.
 * </p>
 */
@Slf4j
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test") // Use the test profile to load test-specific configurations
public class TurnstileValidationServiceTest {

    /**
     * Localhost IP address used for testing.
     * <p>
     * This is used to simulate requests from localhost in the validation tests.
     * </p>
     */
    private static final String LOCALHOST_IP = "127.0.0.1";

    /**
     * The TurnstileValidationService to be tested.
     * <p>
     * This service is responsible for validating Turnstile tokens against Cloudflare's API.
     * </p>
     */
    @Autowired
    private TurnstileValidationService turnstileValidationService;

    /**
     * Test the simple boolean validation method.
     */
    @Test
    public void testValidateTurnstileResponseSuccess() {
        // Create a token that will pass our basic validation (length >= 20)
        String validLengthToken = "0123456789012345678901234567890123456789";

        boolean response = turnstileValidationService.validateTurnstileResponse(validLengthToken, LOCALHOST_IP);
        assertTrue(response);
    }

    /**
     * Test the simple boolean validation method with various invalid inputs.
     * <p>
     * This includes null, empty, and short tokens to ensure they fail validation as expected.
     * </p>
     */
    @Test
    public void testValidateTurnstileResponseNullToken() {
        boolean response = turnstileValidationService.validateTurnstileResponse(null, LOCALHOST_IP);
        assertFalse(response); // Should fail with null token
    }

    /**
     * Test the simple boolean validation method with an empty token.
     * <p>
     * This ensures that an empty token is correctly identified as invalid.
     * </p>
     */
    @Test
    public void testValidateTurnstileResponseEmptyToken() {
        boolean response = turnstileValidationService.validateTurnstileResponse("", LOCALHOST_IP);
        assertFalse(response); // Should fail with empty token
    }

    /**
     * Test the simple boolean validation method with a short token.
     * <p>
     * This ensures that tokens shorter than the required length are correctly identified as invalid.
     * </p>
     */
    @Test
    public void testValidateTurnstileResponseShortToken() {
        boolean response = turnstileValidationService.validateTurnstileResponse("12345", LOCALHOST_IP);
        assertFalse(response); // Should fail with token that's too short
    }

    /**
     * Test the detailed validation method.
     */
    @Test
    public void testValidateTurnstileResponseDetailedSuccess() {
        // Create a token that will pass our basic validation (length >= 20)
        String validLengthToken = "0123456789012345678901234567890123456789";

        ValidationResult result = turnstileValidationService.validateTurnstileResponseDetailed(validLengthToken, LOCALHOST_IP);
        assertTrue(result.isSuccess());
        assertEquals(ValidationResultType.SUCCESS, result.getResultType());
    }

    /**
     * Test the detailed validation method with various invalid inputs.
     * <p>
     * This includes null, empty, and short tokens to ensure they fail validation as expected.
     * </p>
     */
    @Test
    public void testValidateTurnstileResponseDetailedNullToken() {
        ValidationResult result = turnstileValidationService.validateTurnstileResponseDetailed(null, LOCALHOST_IP);
        assertFalse(result.isSuccess());
        assertEquals(ValidationResultType.INPUT_ERROR, result.getResultType());
    }

    /**
     * Test the detailed validation method with an empty token.
     * <p>
     * This ensures that an empty token is correctly identified as invalid.
     * </p>
     */
    @Test
    public void testValidateTurnstileResponseDetailedEmptyToken() {
        ValidationResult result = turnstileValidationService.validateTurnstileResponseDetailed("", LOCALHOST_IP);
        assertFalse(result.isSuccess());
        assertEquals(ValidationResultType.INPUT_ERROR, result.getResultType());
    }

    /**
     * Test the detailed validation method with a short token.
     * <p>
     * This ensures that tokens shorter than the required length are correctly identified as invalid.
     * </p>
     */
    @Test
    public void testValidateTurnstileResponseDetailedShortToken() {
        ValidationResult result = turnstileValidationService.validateTurnstileResponseDetailed("12345", LOCALHOST_IP);
        assertFalse(result.isSuccess());
        assertEquals(ValidationResultType.INPUT_ERROR, result.getResultType());
    }

    /**
     * Test the convenience method without IP address.
     * <p>
     * This tests the validation methods that do not require an IP address, ensuring they still function correctly with valid tokens.
     * </p>
     */
    @Test
    public void testConvenienceMethodWithoutIp() {
        // Create a token that will pass our basic validation (length >= 20)
        String validLengthToken = "0123456789012345678901234567890123456789";

        // Test both the boolean and detailed methods without IP
        boolean booleanResult = turnstileValidationService.validateTurnstileResponse(validLengthToken);
        ValidationResult detailedResult = turnstileValidationService.validateTurnstileResponseDetailed(validLengthToken);

        assertTrue(booleanResult);
        assertTrue(detailedResult.isSuccess());
    }

    /**
     * Builds a standalone TurnstileValidationService backed by a fresh TurnstileConfigProperties instance with the given sitekey and secret. Used
     * to test {@code isUsingTestCredentials()} without mutating the shared autowired service/properties.
     */
    private TurnstileValidationService buildServiceWithCredentials(String sitekey, String secret) {
        TurnstileConfigProperties testProperties = new TurnstileConfigProperties();
        testProperties.setSitekey(sitekey);
        testProperties.setSecret(secret);
        return new TurnstileValidationService(null, testProperties, new NoOpTurnstileMetrics());
    }

    /**
     * Verifies that every documented Cloudflare test sitekey is detected as a test credential.
     *
     * @see <a href="https://developers.cloudflare.com/turnstile/troubleshooting/testing/">Cloudflare Turnstile testing docs</a>
     */
    @Test
    void isUsingTestCredentialsTrueForCloudflareTestSitekeys() {
        List<String> cloudflareTestSitekeys = List.of("1x00000000000000000000AA", "2x00000000000000000000AB", "1x00000000000000000000BB",
                "2x00000000000000000000BB", "3x00000000000000000000FF");

        for (String testSitekey : cloudflareTestSitekeys) {
            TurnstileValidationService service = buildServiceWithCredentials(testSitekey, "0x4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            assertTrue(service.isUsingTestCredentials(), "Expected sitekey '" + testSitekey + "' to be detected as a Cloudflare test credential");
        }
    }

    /**
     * Verifies that every documented Cloudflare test secret is detected as a test credential.
     *
     * @see <a href="https://developers.cloudflare.com/turnstile/troubleshooting/testing/">Cloudflare Turnstile testing docs</a>
     */
    @Test
    void isUsingTestCredentialsTrueForCloudflareTestSecrets() {
        List<String> cloudflareTestSecrets = List.of("1x0000000000000000000000000000000AA", "2x0000000000000000000000000000000AA",
                "3x0000000000000000000000000000000AA");

        for (String testSecret : cloudflareTestSecrets) {
            TurnstileValidationService service = buildServiceWithCredentials("0x4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", testSecret);
            assertTrue(service.isUsingTestCredentials(), "Expected secret '" + testSecret + "' to be detected as a Cloudflare test credential");
        }
    }

    /**
     * Verifies that real-looking (non-test) credentials are not flagged as test credentials.
     */
    @Test
    void isUsingTestCredentialsFalseForRealCredentials() {
        TurnstileValidationService service =
                buildServiceWithCredentials("0x4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "0x4BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        assertFalse(service.isUsingTestCredentials());
    }

    /**
     * Verifies that a null (unconfigured) sitekey and secret are not flagged as test credentials.
     */
    @Test
    void isUsingTestCredentialsFalseWhenUnconfigured() {
        TurnstileValidationService service = buildServiceWithCredentials(null, null);
        assertFalse(service.isUsingTestCredentials());
    }
}
