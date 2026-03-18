package org.hartford.binsure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private BigDecimal coverageAmount;
    private BigDecimal annualPremium;
    private BigDecimal deductible;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyStatus status;
    private String policyDocumentUrl;
    private LocalDateTime issuedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived fields
    private Long applicationId;
    private Long businessId;
    private String companyName;
    private Long productId;
    private String productName;
    private Long underwriterId;
    private String underwriterName;
}
