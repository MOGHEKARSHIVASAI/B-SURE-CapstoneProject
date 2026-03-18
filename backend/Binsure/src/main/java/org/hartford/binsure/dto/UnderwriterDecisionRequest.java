package org.hartford.binsure.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hartford.binsure.enums.DecisionType;

import java.math.BigDecimal;

/**
 * Request body for POST /api/v1/underwriting/application/{appId}/decision/{uwId}
 */
@Data
public class UnderwriterDecisionRequest {

    @NotNull(message = "Decision is required")
    private DecisionType decision;

    @Min(value = 0, message = "Risk score must be between 0 and 100")
    @Max(value = 100, message = "Risk score must be between 0 and 100")
    private Integer riskScore;

    @DecimalMin(value = "-100.00", message = "Adjustment must be between -100 and 100")
    @DecimalMax(value = "100.00",  message = "Adjustment must be between -100 and 100")
    private BigDecimal premiumAdjustmentPct;

    private String comments;
}

