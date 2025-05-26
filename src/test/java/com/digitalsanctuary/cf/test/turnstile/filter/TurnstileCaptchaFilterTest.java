package com.digitalsanctuary.cf.test.turnstile.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.digitalsanctuary.cf.test.TestApplication;
import com.digitalsanctuary.cf.turnstile.filter.TurnstileCaptchaFilter;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;

/**
 * Unit tests for the TurnstileCaptchaFilter.
 * <p>
 * This class tests the behavior of the TurnstileCaptchaFilter, including valid and invalid captcha responses, and non-login paths.
 * </p>
 */
@Slf4j
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class TurnstileCaptchaFilterTest {

    /**
     * Mocked TurnstileValidationService for testing.
     */
    @MockitoBean
    private TurnstileValidationService validationService;

    /**
     * This filter is responsible for validating Turnstile captcha responses in HTTP requests.
     */
    @Autowired
    private TurnstileCaptchaFilter captchaFilter;

    /**
     * Default constructor for TurnstileCaptchaFilterTest.
     * <p>
     * This constructor is used to initialize the test class.
     * </p>
     */
    @Test
    public void testValidCaptcha() throws ServletException, IOException {
        // Use specific parameters instead of any() matchers
        String expectedToken = "valid-token";

        // Make your mock more specific
        when(validationService.validateTurnstileResponse(eq(expectedToken), any())).thenReturn(true);

        // Add verification
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/login");
        request.setMethod("POST");
        request.setParameter("cf-turnstile-response", expectedToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // Execute the filter
        captchaFilter.doFilter(request, response, filterChain);

        // Verify the mock was actually called with the expected parameters
        verify(validationService).validateTurnstileResponse(eq(expectedToken), any());

        // Rest of your assertions
        assertNotNull(filterChain.getRequest(), "Filter chain should continue with valid token");
        assertNull(response.getRedirectedUrl(), "Should not redirect with valid token");
    }

    /**
     * Tests the behavior of the TurnstileCaptchaFilter when an invalid captcha response is provided.
     * <p>
     * This test simulates a scenario where the captcha validation fails, and checks that the filter redirects to the login page with an error
     * parameter.
     * </p>
     */
    @Test
    public void testInvalidCaptcha() throws ServletException, IOException {
        // given an invalid token returns false from the validation service
        when(validationService.validateTurnstileResponse(any(String.class), any(String.class))).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/login");
        request.setMethod("POST");
        request.setParameter("cf-turnstile-response", "invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        captchaFilter.doFilter(request, response, filterChain);

        // expect redirect to the login page with error param
        assertEquals("/login?error=captcha", response.getRedirectedUrl());
    }

    /**
     * Tests the behavior of the TurnstileCaptchaFilter when the servlet path does not match the login submission path.
     * <p>
     * This test ensures that the filter allows requests to proceed without redirection when they do not match the expected login path.
     * </p>
     */
    @Test
    public void testNonLoginPath() throws ServletException, IOException {
        // when the servlet path does not match login submission path, filter should pass through
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/other");
        request.setMethod("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        captchaFilter.doFilter(request, response, filterChain);

        // expect filter chain to proceed without redirection
        assertNotNull(filterChain.getRequest());
        assertNull(response.getRedirectedUrl());
    }
}
