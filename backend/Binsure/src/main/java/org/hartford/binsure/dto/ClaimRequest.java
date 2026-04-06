package org.hartford.binsure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for POST /api/v1/claims/customer/{customerId} — file a new claim
 * Request body for PUT  /api/v1/claims/{id}                  — update a claim
 */
@Data
public class ClaimRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Incident date is required")
    private LocalDate incidentDate;

    @NotNull(message = "Claimed amount is required")
    @DecimalMin(value = "0.01", message = "Claimed amount must be greater than 0")
    private BigDecimal claimedAmount;

    @NotBlank(message = "Incident description is required")
    private String incidentDescription;
}

