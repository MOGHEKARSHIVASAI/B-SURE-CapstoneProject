package org.hartford.binsure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for:
 * - POST /api/v1/applications/customer/{customerId} (create DRAFT application)
 * - PUT /api/v1/applications/{id} (update DRAFT application)
 */
@Data
public class PolicyApplicationDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "0.01", message = "Coverage amount must be greater than 0")
    private BigDecimal coverageAmount;

    @NotNull(message = "Coverage start date is required")
    private LocalDate coverageStartDate;

    @NotNull(message = "Coverage end date is required")
    private LocalDate coverageEndDate;

    private String riskNotes;
}
