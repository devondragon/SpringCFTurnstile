package com.digitalsanctuary.cf.turnstile.exception;

/**
 * Base exception class for all Turnstile-related exceptions.
 * <p>
 * This exception serves as the parent class for all specific Turnstile exceptions.
 * It provides common functionality and allows for catching all Turnstile exceptions
 * with a single catch block if desired.
 * </p>
 */
public class TurnstileException extends RuntimeException {

    /**
     * Constructs a new Turnstile exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public TurnstileException(String message) {
        super(message);
    }

    /**
     * Constructs a new Turnstile exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TurnstileException(String message, Throwable cause) {
        super(message, cause);
    }
}