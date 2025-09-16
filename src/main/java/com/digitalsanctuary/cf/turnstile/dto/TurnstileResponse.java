package com.digitalsanctuary.cf.turnstile.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing the response from Cloudflare's Turnstile API.
 * <p>
 * This class maps to the JSON response returned by Cloudflare's Turnstile verification endpoint. It contains information about whether the
 * verification was successful, when the challenge was completed, what hostname it was completed on, and any error codes if the verification failed.
 * </p>
 * <p>
 * Example successful JSON response:
 * </p>
 * 
 * <pre>
 * {
 *   "success": true,
 *   "challenge_ts": "2023-01-01T12:00:00Z",
 *   "hostname": "example.com",
 *   "error-codes": []
 * }
 * </pre>
 * <p>
 * Example failed JSON response:
 * </p>
 * 
 * <pre>
 * {
 *   "success": false,
 *   "error-codes": ["invalid-input-response", "timeout-or-duplicate"]
 * }
 * </pre>
 *
 * @see <a href="https://developers.cloudflare.com/turnstile/get-started/server-side-validation/">Cloudflare Turnstile Server-Side Validation</a>
 */
@Data
public class TurnstileResponse {

    /**
     * Indicates whether the validation was successful.
     */
    private boolean success;

    /**
     * Timestamp of the challenge.
     */
    @JsonProperty("challenge_ts")
    private String challengeTs;

    /**
     * Hostname of the site where the challenge was solved.
     */
    private String hostname;

    /**
     * List of error codes returned by the API, if any.
     */
    @JsonProperty("error-codes")
    private List<String> errorCodes;
}
