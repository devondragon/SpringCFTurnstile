package com.digitalsanctuary.cf.test.turnstile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.digitalsanctuary.cf.test.TestApplication;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult;
import com.digitalsanctuary.cf.turnstile.dto.ValidationResult.ValidationResultType;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileConfigurationException;
import com.digitalsanctuary.cf.turnstile.exception.TurnstileValidationException;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test") // Use the test profile to load test-specific configurations
public class TurnstileValidationServiceTest {

    @Autowired
    private TurnstileValidationService turnstileValidationService;

    /**
     * Test the simple boolean validation method
     */
    @Test
    public void testValidateTurnstileResponse_Success() {
        // Create a token that will pass our basic validation (length >= 20)
        String validLengthToken = "0123456789012345678901234567890123456789";
        
        boolean response = turnstileValidationService.validateTurnstileResponse(validLengthToken, "127.0.0.1");
        assertTrue(response);
    }
    
    @Test
    public void testValidateTurnstileResponse_NullToken() {
        boolean response = turnstileValidationService.validateTurnstileResponse(null, "127.0.0.1");
        assertFalse(response); // Should fail with null token
    }
    
    @Test
    public void testValidateTurnstileResponse_EmptyToken() {
        boolean response = turnstileValidationService.validateTurnstileResponse("", "127.0.0.1");
        assertFalse(response); // Should fail with empty token
    }
    
    @Test
    public void testValidateTurnstileResponse_ShortToken() {
        boolean response = turnstileValidationService.validateTurnstileResponse("12345", "127.0.0.1");
        assertFalse(response); // Should fail with token that's too short
    }
    
    /**
     * Test the detailed validation method
     */
    @Test
    public void testValidateTurnstileResponseDetailed_Success() {
        // Create a token that will pass our basic validation (length >= 20)
        String validLengthToken = "0123456789012345678901234567890123456789";
        
        ValidationResult result = turnstileValidationService.validateTurnstileResponseDetailed(validLengthToken, "127.0.0.1");
        assertTrue(result.isSuccess());
        assertEquals(ValidationResultType.SUCCESS, result.getResultType());
    }
    
    @Test
    public void testValidateTurnstileResponseDetailed_NullToken() {
        ValidationResult result = turnstileValidationService.validateTurnstileResponseDetailed(null, "127.0.0.1");
        assertFalse(result.isSuccess());
        assertEquals(ValidationResultType.INPUT_ERROR, result.getResultType());
    }
    
    @Test
    public void testValidateTurnstileResponseDetailed_EmptyToken() {
        ValidationResult result = turnstileValidationService.validateTurnstileResponseDetailed("", "127.0.0.1");
        assertFalse(result.isSuccess());
        assertEquals(ValidationResultType.INPUT_ERROR, result.getResultType());
    }
    
    @Test
    public void testValidateTurnstileResponseDetailed_ShortToken() {
        ValidationResult result = turnstileValidationService.validateTurnstileResponseDetailed("12345", "127.0.0.1");
        assertFalse(result.isSuccess());
        assertEquals(ValidationResultType.INPUT_ERROR, result.getResultType());
    }
    
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
}
