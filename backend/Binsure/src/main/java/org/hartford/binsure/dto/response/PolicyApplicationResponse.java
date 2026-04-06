package org.hartford.binsure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.ApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyApplicationResponse {
    private Long id;
    private BigDecimal coverageAmount;
    private LocalDate coverageStartDate;
    private LocalDate coverageEndDate;
    private ApplicationStatus status;
    private String riskNotes;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived fields
    private Long businessId;
    private String companyName;
    private Long productId;
    private String productName;
    private Long assignedUnderwriterId;
    private String assignedUnderwriterName;
    private BigDecimal annualPremium;
    private BigDecimal premiumAdjustmentPct;
    private Integer aiRiskScore;
    private String aiUnderwritingAnalysis;
    private BigDecimal recommendedPremium;
    private BigDecimal basePremium;
    private int documentCount;
}
