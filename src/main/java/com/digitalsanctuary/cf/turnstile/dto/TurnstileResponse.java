package com.digitalsanctuary.cf.turnstile.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents the response from Cloudflare's Turnstile API. This class is used to deserialize the JSON response from the API.
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

