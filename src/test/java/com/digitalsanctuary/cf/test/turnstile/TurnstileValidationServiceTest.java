package com.digitalsanctuary.cf.test.turnstile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.digitalsanctuary.cf.test.TestApplication;
import com.digitalsanctuary.cf.turnstile.TurnstileValidationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test") // Use the test profile to load test-specific configurations
public class TurnstileValidationServiceTest {

    @Autowired
    private TurnstileValidationService turnstileValidationService;

    @Test
    public void testValidateTurnstileResponse_Success() {

        boolean response = turnstileValidationService.validateTurnstileResponse("testToken", "127.0.0.1");
        assertTrue(response);
    }
}
