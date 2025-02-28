package com.digitalsanctuary.cf.turnstile.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a Turnstile validation operation.
 * <p>
 * This class provides detailed information about the result of validating a Turnstile token,
 * including whether the validation was successful, any error codes returned by Cloudflare,
 * and additional context about the validation.
 * </p>
 */
public class ValidationResult {
    
    private final boolean success;
    private final List<String> errorCodes;
    private final String message;
    private final ValidationResultType resultType;
    
    /**
     * Private constructor for ValidationResult instances.
     * Use the static factory methods to create instances.
     * 
     * @param success whether the validation was successful
     * @param errorCodes the list of error codes if validation failed
     * @param message a descriptive message about the validation result
     * @param resultType the type of validation result
     */
    private ValidationResult(boolean success, List<String> errorCodes, String message, ValidationResultType resultType) {
        this.success = success;
        this.errorCodes = errorCodes != null ? Collections.unmodifiableList(errorCodes) : Collections.emptyList();
        this.message = message;
        this.resultType = resultType;
    }
    
    /**
     * Creates a successful validation result.
     * 
     * @return a ValidationResult indicating success
     */
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList(), "Validation successful", ValidationResultType.SUCCESS);
    }
    
    /**
     * Creates a validation result for an invalid token response from Cloudflare.
     * 
     * @param errorCodes the error codes returned by Cloudflare
     * @return a ValidationResult indicating an invalid token
     */
    public static ValidationResult invalidToken(List<String> errorCodes) {
        return new ValidationResult(false, errorCodes, "Token validation failed", ValidationResultType.INVALID_TOKEN);
    }
    
    /**
     * Creates a validation result for a network error.
     * 
     * @param errorMessage the error message describing the network issue
     * @return a ValidationResult indicating a network error
     */
    public static ValidationResult networkError(String errorMessage) {
        return new ValidationResult(false, Collections.emptyList(), 
                "Network error during validation: " + errorMessage, ValidationResultType.NETWORK_ERROR);
    }
    
    /**
     * Creates a validation result for a configuration error.
     * 
     * @param errorMessage the error message describing the configuration issue
     * @return a ValidationResult indicating a configuration error
     */
    public static ValidationResult configurationError(String errorMessage) {
        return new ValidationResult(false, Collections.emptyList(), 
                "Configuration error: " + errorMessage, ValidationResultType.CONFIGURATION_ERROR);
    }
    
    /**
     * Creates a validation result for an input validation error.
     * 
     * @param errorMessage the error message describing the input validation issue
     * @return a ValidationResult indicating an input validation error
     */
    public static ValidationResult inputError(String errorMessage) {
        return new ValidationResult(false, Collections.emptyList(), 
                "Input validation error: " + errorMessage, ValidationResultType.INPUT_ERROR);
    }
    
    /**
     * Returns whether the validation was successful.
     * 
     * @return true if validation was successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Returns the list of error codes if validation failed.
     * 
     * @return an unmodifiable list of error codes, or an empty list if no error codes are available
     */
    public List<String> getErrorCodes() {
        return errorCodes;
    }
    
    /**
     * Returns a descriptive message about the validation result.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Returns the type of validation result.
     * 
     * @return the validation result type
     */
    public ValidationResultType getResultType() {
        return resultType;
    }
    
    /**
     * Enum representing the different types of validation results.
     */
    public enum ValidationResultType {
        /** Validation was successful. */
        SUCCESS,
        
        /** Token was invalid according to Cloudflare. */
        INVALID_TOKEN,
        
        /** A network error occurred during validation. */
        NETWORK_ERROR,
        
        /** A configuration error was detected. */
        CONFIGURATION_ERROR,
        
        /** An input validation error occurred. */
        INPUT_ERROR
    }
}