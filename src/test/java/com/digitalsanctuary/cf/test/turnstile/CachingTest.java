package com.digitalsanctuary.cf.test.turnstile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.digitalsanctuary.cf.test.TestApplication;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;

/**
 * Test class dedicated to testing just the caching functionality. Uses a minimal configuration to avoid conflicts.
 */

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class CachingTest {

    @Autowired
    private TurnstileValidationService validationService;

    @Test
    public void testCachingAndBypassMethods() {
        // Create a unique test token to ensure clean slate
        String testToken = "0123456789012345678901234567890123456789_" + System.currentTimeMillis();

        // Test caching method
        ValidationResult result1 = validationService.validateTurnstileResponseDetailed(testToken);
        assertTrue(result1.isSuccess(), "First validation should succeed");

        // Test no-cache method
        ValidationResult result2 = validationService.validateTurnstileResponseNoCache(testToken);
        assertTrue(result2.isSuccess(), "No-cache validation should succeed");

        // Test cached method again (should hit cache)
        ValidationResult result3 = validationService.validateTurnstileResponseDetailed(testToken);
        assertTrue(result3.isSuccess(), "Second cached validation should succeed");
    }

}
