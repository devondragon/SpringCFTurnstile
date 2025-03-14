package com.digitalsanctuary.cf.turnstile.filter;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.digitalsanctuary.cf.turnstile.service.TurnstileValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TurnstileCaptchaFilter extends OncePerRequestFilter {

    private final TurnstileValidationService validationService;

    @Value("${ds.cf.turnstile.login.submissionPath:/login}")
    private String loginSubmissionPath;

    @Value("${ds.cf.turnstile.login.redirectUrl:/login?error=captcha}")
    private String loginRedirectUrl;

    @Value("${ds.cf.turnstile.token.parameterName:cf-turnstile-response}")
    private String turnstileTokenParameterName;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals(loginSubmissionPath) && "POST".equalsIgnoreCase(request.getMethod())) {
            String token = request.getParameter(turnstileTokenParameterName);
            boolean valid = validationService.validateTurnstileResponse(token, getClientIp(request));
            if (valid) {
                filterChain.doFilter(request, response);
            } else {
                log.warn("Turnstile captcha validation failed for request: {}", request.getServletPath());
                response.sendRedirect(loginRedirectUrl);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        // Delegate to the service method or use a similar logic
        return validationService.getClientIpAddress(request);
    }
}
