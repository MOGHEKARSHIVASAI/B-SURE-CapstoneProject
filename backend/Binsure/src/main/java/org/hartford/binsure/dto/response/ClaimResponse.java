package org.hartford.binsure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.ClaimStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private String claimNumber;
    private LocalDate incidentDate;
    private LocalDate claimDate;
    private LocalDate settlementDate;
    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal settledAmount;
    private String incidentDescription;
    private String rejectionReason;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived fields
    private Long policyId;
    private String policyNumber;
    private Long businessId;
    private String companyName;
    private Long assignedOfficerId;
    private String assignedOfficerName;
    private String productName;
    private int documentCount;
}
