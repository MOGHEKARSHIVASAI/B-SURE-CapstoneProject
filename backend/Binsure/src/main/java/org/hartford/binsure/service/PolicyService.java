package org.hartford.binsure.service;

import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hartford.binsure.enums.PolicyStatus;
import org.hartford.binsure.exception.*;
import org.hartford.binsure.repository.PolicyApplicationRepository;
import org.hartford.binsure.repository.PolicyRepository;
import org.hartford.binsure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private NotificationService notificationService;

    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * AUTO POLICY ISSUANCE FLOW:
     * ═══════════════════════════════════════════════════════════════════════════════
     *
     * 1. Customer applies for policy (DRAFT)
     * 2. Customer submits application (SUBMITTED)
     * 3. Admin assigns underwriter (UNDER_REVIEW)
     * 4. Underwriter reviews & approves (APPROVED)
     * 5. Customer accepts decision (CUSTOMER_ACCEPTED)
     * 6. Customer makes payment → POLICY AUTO-CREATED IN ACTIVE STATUS
     *
     * → This service ONLY manages existing policies (read, suspend, cancel,
     * reactivate)
     * → Policy creation is handled by
     * PremiumPaymentService.createPolicyFromApplication()
     * ═══════════════════════════════════════════════════════════════════════════════
     */

    public Policy getPolicyById(Long policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "ID", policyId));
    }

    public Policy getPolicyByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "number", policyNumber));
    }

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public List<Policy> getPoliciesByCustomer(Long userId) {
        return policyRepository.findByBusiness_User_Id(userId);
    }

    public List<Policy> getPoliciesByBusiness(Long businessId) {
        return policyRepository.findByBusiness_Id(businessId);
    }

    public List<Policy> getPoliciesByStatus(PolicyStatus status) {
        return policyRepository.findByStatus(status);
    }

    public List<Policy> getExpiringPolicies(int daysFromNow) {
        if (daysFromNow <= 0) {
            throw new InvalidOperationException("Days must be a positive number.");
        }
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysFromNow);
        return policyRepository.findExpiringPolicies(PolicyStatus.ACTIVE, today, endDate);
    }

    public Policy suspendPolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "ID", policyId));

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Policy",
                    policy.getStatus().name(), "SUSPENDED",
                    "Only ACTIVE policies can be suspended.");
        }
        policy.setStatus(PolicyStatus.SUSPENDED);
        return policyRepository.save(policy);
    }

    public Policy reactivatePolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "ID", policyId));

        if (policy.getStatus() != PolicyStatus.SUSPENDED) {
            throw new InvalidStatusTransitionException("Policy",
                    policy.getStatus().name(), "ACTIVE",
                    "Only SUSPENDED policies can be reactivated.");
        }
        policy.setStatus(PolicyStatus.ACTIVE);
        return policyRepository.save(policy);
    }

    public Policy cancelPolicy(Long policyId, String cancellationReason) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "ID", policyId));

        if (policy.getStatus() == PolicyStatus.CANCELLED) {
            throw new InvalidOperationException("Policy ID " + policyId + " is already cancelled.");
        }
        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            throw new InvalidOperationException("Cancellation reason must be provided.");
        }

        policy.setStatus(PolicyStatus.CANCELLED);
        policy.setCancelledAt(LocalDateTime.now());
        policy.setCancellationReason(cancellationReason.trim());
        return policyRepository.save(policy);
    }

    private BigDecimal calculatePremium(InsuranceProduct product, BigDecimal coverageAmount) {
        return product.getBasePremiumRate().multiply(coverageAmount);
    }
}
