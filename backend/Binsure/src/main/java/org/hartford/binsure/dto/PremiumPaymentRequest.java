package org.hartford.binsure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for POST /api/v1/payments/customer/{customerId}
 */
@Data
public class PremiumPaymentRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String remarks;
}

