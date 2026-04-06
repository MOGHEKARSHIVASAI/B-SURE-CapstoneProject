package org.hartford.binsure.mapper;

import org.hartford.binsure.dto.response.*;
import org.hartford.binsure.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    // Removed BusinessCustomerResponse toDto(BusinessCustomer entity)

    public PolicyApplicationResponse toDto(PolicyApplication entity) {
        if (entity == null)
            return null;
        return PolicyApplicationResponse.builder()
                .id(entity.getId())
                .coverageAmount(entity.getCoverageAmount())
                .coverageStartDate(entity.getCoverageStartDate())
                .coverageEndDate(entity.getCoverageEndDate())
                .status(entity.getStatus())
                .riskNotes(entity.getRiskNotes())
                .submittedAt(entity.getSubmittedAt())
                .reviewedAt(entity.getReviewedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .businessId(entity.getBusinessId())
                .companyName(entity.getCompanyName())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .assignedUnderwriterId(entity.getAssignedUnderwriterId())
                .assignedUnderwriterName(entity.getAssignedUnderwriterName())
                .annualPremium(entity.getAnnualPremium())
                .premiumAdjustmentPct(entity.getPremiumAdjustmentPct())
                .aiRiskScore(entity.getAiRiskScore())
                .aiUnderwritingAnalysis(entity.getAiUnderwritingAnalysis())
                .recommendedPremium(entity.getRecommendedPremium())
                .basePremium((entity.getProduct() != null && entity.getCoverageAmount() != null)
                        ? entity.getProduct().getBasePremiumRate().multiply(entity.getCoverageAmount())
                        : null)
                .documentCount(entity.getDocumentCount())
                .build();
    }

    public PolicyResponse toDto(Policy entity) {
        if (entity == null)
            return null;
        return PolicyResponse.builder()
                .id(entity.getId())
                .policyNumber(entity.getPolicyNumber())
                .coverageAmount(entity.getCoverageAmount())
                .annualPremium(entity.getAnnualPremium())
                .deductible(entity.getDeductible())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .policyDocumentUrl(entity.getPolicyDocumentUrl())
                .issuedAt(entity.getIssuedAt())
                .cancelledAt(entity.getCancelledAt())
                .cancellationReason(entity.getCancellationReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .applicationId(entity.getApplicationId())
                .businessId(entity.getBusinessId())
                .companyName(entity.getCompanyName())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .underwriterId(entity.getUnderwriterId())
                .underwriterName(entity.getUnderwriterName())
                .build();
    }

    public ClaimResponse toDto(Claim entity) {
        if (entity == null)
            return null;
        return ClaimResponse.builder()
                .id(entity.getId())
                .claimNumber(entity.getClaimNumber())
                .incidentDate(entity.getIncidentDate())
                .claimDate(entity.getClaimDate())
                .settlementDate(entity.getSettlementDate())
                .claimedAmount(entity.getClaimedAmount())
                .approvedAmount(entity.getApprovedAmount())
                .settledAmount(entity.getSettledAmount())
                .incidentDescription(entity.getIncidentDescription())
                .rejectionReason(entity.getRejectionReason())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .policyId(entity.getPolicyId())
                .policyNumber(entity.getPolicyNumber())
                .businessId(entity.getBusinessId())
                .companyName(entity.getCompanyName())
                .assignedOfficerId(entity.getAssignedOfficerId())
                .assignedOfficerName(entity.getAssignedOfficerName())
                .productName(entity.getProductName())
                .documentCount(entity.getDocumentCount())
                .riskScore(entity.getRiskScore())
                .riskAnalysis(entity.getRiskAnalysis())
                .build();
    }

    public PremiumPaymentResponse toDto(PremiumPayment entity) {
        if (entity == null)
            return null;
        return PremiumPaymentResponse.builder()
                .id(entity.getId())
                .paymentReference(entity.getPaymentReference())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .paidAt(entity.getPaidAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .applicationId(entity.getApplicationId())
                .businessId(entity.getBusinessId())
                .companyName(entity.getCompanyName())
                .build();
    }

    public InsuranceProductResponse toDto(InsuranceProduct entity) {
        if (entity == null)
            return null;
        return InsuranceProductResponse.builder()
                .id(entity.getId())
                .productName(entity.getProductName())
                .productCode(entity.getProductCode())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .basePremiumRate(entity.getBasePremiumRate())
                .minCoverageAmount(entity.getMinCoverageAmount())
                .maxCoverageAmount(entity.getMaxCoverageAmount())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdByName(entity.getCreatedBy() != null
                        ? entity.getCreatedBy().getFirstName() + " " + entity.getCreatedBy().getLastName()
                        : null)
                .build();
    }

    public UnderwriterDecisionResponse toDto(UnderwriterDecision entity) {
        if (entity == null)
            return null;
        return UnderwriterDecisionResponse.builder()
                .id(entity.getId())
                .decision(entity.getDecision())
                .riskScore(entity.getRiskScore())
                .premiumAdjustmentPct(entity.getPremiumAdjustmentPct())
                .comments(entity.getComments())
                .decidedAt(entity.getDecidedAt())
                .applicationId(entity.getApplication() != null ? entity.getApplication().getId() : null)
                .underwriterId(entity.getUnderwriter() != null ? entity.getUnderwriter().getId() : null)
                .underwriterName(entity.getUnderwriter() != null
                        ? entity.getUnderwriter().getFirstName() + " " + entity.getUnderwriter().getLastName()
                        : null)
                .build();
    }

    public BusinessResponse toDto(Business entity) {
        if (entity == null)
            return null;
        return BusinessResponse.builder()
                .id(entity.getId())
                .companyName(entity.getCompanyName())
                .companyRegNumber(entity.getCompanyRegNumber())
                .industryType(entity.getIndustryType())
                .annualRevenue(entity.getAnnualRevenue())
                .numEmployees(entity.getNumEmployees())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .taxId(entity.getTaxId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .email(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .firstName(entity.getUser() != null ? entity.getUser().getFirstName() : null)
                .lastName(entity.getUser() != null ? entity.getUser().getLastName() : null)
                .phone(entity.getUser() != null ? entity.getUser().getPhone() : null)
                .build();
    }
}
