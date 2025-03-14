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


/**
 * Filters incoming HTTP requests to validate the Turnstile captcha token during login submissions.
 * <p>
 * This filter intercepts POST requests to the configured login submission path and validates the Turnstile captcha token provided in the request. If
 * the token is valid, the request is allowed to proceed through the filter chain. If the token is invalid, the user is redirected to a configured
 * error URL.
 * </p>
 *
 *
 * Configuration properties:
 * <ul>
 * <li><b>ds.cf.turnstile.login.submissionPath</b>: The path to intercept for login submissions (default: <code>/login</code>).</li>
 * <li><b>ds.cf.turnstile.login.redirectUrl</b>: The URL to redirect to when captcha validation fails (default:
 * <code>/login?error=captcha</code>).</li>
 * <li><b>ds.cf.turnstile.token.parameterName</b>: The name of the request parameter containing the Turnstile token (default:
 * <code>cf-turnstile-response</code>).</li>
 * </ul>
 *
 * <p>
 * Note: The client IP address is extracted using the {@link TurnstileValidationService#getClientIpAddress(HttpServletRequest)} method.
 * </p>
 */
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

    /**
     * Filters incoming HTTP requests to validate the Turnstile captcha token during login submissions.
     * <p>
     * This filter intercepts POST requests to the configured login submission path and validates the Turnstile captcha token provided in the request.
     * If the token is valid, the request is allowed to proceed through the filter chain. If the token is invalid, the user is redirected to a
     * configured error URL.
     * </p>
     *
     * @param request the {@link HttpServletRequest} object that contains the client request
     * @param response the {@link HttpServletResponse} object that contains the response the filter sends
     * @param filterChain the {@link FilterChain} to pass the request and response to the next filter
     * @throws ServletException if an error occurs during the filtering process
     * @throws IOException if an I/O error occurs during the filtering process
     */
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
