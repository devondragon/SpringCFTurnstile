package com.digitalsanctuary.cf.turnstile.exception;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when Cloudflare's Turnstile API explicitly rejects a token.
 * <p>
 * This exception indicates that the Cloudflare Turnstile API processed the request
 * successfully but rejected the token as invalid. The error codes returned by
 * Cloudflare are included in this exception.
 * </p>
 */
public class TurnstileValidationException extends TurnstileException {

    private final List<String> errorCodes;
    
    /**
     * Constructs a new Turnstile validation exception with the specified detail message
     * and error codes.
     * 
     * @param message the detail message
     * @param errorCodes the list of error codes returned by Cloudflare
     */
    public TurnstileValidationException(String message, List<String> errorCodes) {
        super(message);
        this.errorCodes = errorCodes != null ? Collections.unmodifiableList(errorCodes) : Collections.emptyList();
    }
    
    /**
     * Gets the list of error codes returned by Cloudflare's Turnstile API.
     * 
     * @return an unmodifiable list of error codes
     */
    public List<String> getErrorCodes() {
        return errorCodes;
    }
}