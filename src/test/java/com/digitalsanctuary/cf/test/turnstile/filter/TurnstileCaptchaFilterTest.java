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

@Slf4j
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
public class TurnstileCaptchaFilterTest {

    // Use @MockitoBean to replace the real service with a mock in the Spring context
    @MockitoBean
    private TurnstileValidationService validationService;

    // Autowire the filter from the Spring context
    @Autowired
    private TurnstileCaptchaFilter captchaFilter;

    @Test
    public void testValidCaptcha() throws ServletException, IOException {
        // Use specific parameters instead of any() matchers
        String expectedToken = "valid-token";
        String expectedIp = null; // or the actual IP if your filter uses one

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
