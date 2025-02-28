package com.digitalsanctuary.cf.turnstile.exception;

/**
 * Exception thrown when there is an issue with the Turnstile configuration.
 * <p>
 * This exception indicates problems with the configuration of the Turnstile service,
 * such as missing or invalid secret keys, URLs, or other required configuration properties.
 * </p>
 */
public class TurnstileConfigurationException extends TurnstileException {

    /**
     * Constructs a new Turnstile configuration exception with the specified detail message.
     * 
     * @param message the detail message
     */
    public TurnstileConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new Turnstile configuration exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TurnstileConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}