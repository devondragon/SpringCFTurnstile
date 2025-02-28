package com.digitalsanctuary.cf.turnstile.exception;

/**
 * Exception thrown when a network-related issue occurs during Turnstile validation.
 * <p>
 * This exception indicates problems like connection timeouts, DNS resolution failures,
 * or other network-related issues when communicating with the Cloudflare Turnstile API.
 * </p>
 */
public class TurnstileNetworkException extends TurnstileException {

    /**
     * Constructs a new Turnstile network exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public TurnstileNetworkException(String message) {
        super(message);
    }

    /**
     * Constructs a new Turnstile network exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TurnstileNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}