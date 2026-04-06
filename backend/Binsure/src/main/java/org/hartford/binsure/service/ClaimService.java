package org.hartford.binsure.service;

import org.hartford.binsure.dto.ClaimRequest;
import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.ClaimStatus;
import org.hartford.binsure.exception.*;
import org.hartford.binsure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AiService aiService;

    public Claim fileNewClaim(ClaimRequest request, Long userId) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy", "ID", request.getPolicyId()));
        if (!policy.getBusiness().getUser().getId().equals(userId))
            throw new UnauthorizedActionException(
                    "Policy " + request.getPolicyId() + " does not belong to user " + userId);
        if (policy.getStatus().name().equals("CANCELLED") || policy.getStatus().name().equals("EXPIRED"))
            throw new InvalidOperationException("Claims cannot be filed against a " + policy.getStatus() + " policy.");
        if (request.getIncidentDate().isAfter(LocalDate.now()))
            throw new InvalidOperationException("Incident date cannot be in the future.");
        if (request.getIncidentDate().isBefore(policy.getStartDate()))
            throw new InvalidOperationException(
                    "Incident date cannot be before the policy start date (" + policy.getStartDate() + ").");
        if (request.getIncidentDate().isAfter(policy.getEndDate()))
            throw new InvalidOperationException(
                    "Incident date cannot be after the policy end date (" + policy.getEndDate() + ").");

        Claim claim = new Claim();
        claim.setClaimNumber("CLM-" + System.currentTimeMillis());
        claim.setPolicy(policy);
        claim.setBusiness(policy.getBusiness());
        claim.setIncidentDate(request.getIncidentDate());
        claim.setClaimDate(LocalDate.now());
        claim.setClaimedAmount(request.getClaimedAmount());
        claim.setIncidentDescription(request.getIncidentDescription());
        claim.setStatus(ClaimStatus.DRAFT);
        return claimRepository.save(claim);
    }

    public Claim submitClaim(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
        if (claim.getStatus() != ClaimStatus.DRAFT)
            throw new InvalidOperationException("Only DRAFT claims can be submitted.");

        claim.setStatus(ClaimStatus.SUBMITTED);
        
        // Trigger AI analysis
        aiService.analyzeClaim(claim);
        
        claim = claimRepository.save(claim);
        notificationService.notifyClaimSubmitted(claim.getBusiness().getUser(), claim.getId());
        return claim;
    }

    public Claim assignClaimsOfficer(Long claimId, Long officerId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", officerId));
        if (!officer.getRole().name().equals("CLAIMS_OFFICER"))
            throw new InvalidOperationException("User ID " + officerId + " is not a CLAIMS_OFFICER.");
        claim.setAssignedOfficer(officer);
        claim.setStatus(ClaimStatus.ASSIGNED);
        claim = claimRepository.save(claim);
        notificationService.notifyOfficerClaimAssigned(officer, null, claimId);
        return claim;
    }

    public Claim markUnderInvestigation(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
        if (claim.getStatus() != ClaimStatus.ASSIGNED && claim.getStatus() != ClaimStatus.SUBMITTED)
            throw new InvalidStatusTransitionException("Claim", claim.getStatus().name(), "UNDER_INVESTIGATION",
                    "Claim must be SUBMITTED or ASSIGNED first.");
        claim.setStatus(ClaimStatus.UNDER_INVESTIGATION);
        claim = claimRepository.save(claim);
        notificationService.notifyCustomerClaimInvestigation(
                claim.getBusiness().getUser(), claim.getAssignedOfficer(), claimId);
        return claim;
    }

    public Claim approveClaim(Long claimId, BigDecimal approvedAmount) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Approved amount must be greater than zero.");
        if (approvedAmount.compareTo(claim.getClaimedAmount()) > 0)
            throw new InvalidOperationException("Approved amount cannot exceed claimed amount.");
        claim.setApprovedAmount(approvedAmount);
        claim.setStatus(ClaimStatus.APPROVED);
        claim = claimRepository.save(claim);
        notificationService.notifyCustomerClaimApproved(
                claim.getBusiness().getUser(), claim.getAssignedOfficer(), claimId,
                approvedAmount.toString());
        return claim;
    }

    public Claim rejectClaim(Long claimId, String rejectionReason) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
        if (rejectionReason == null || rejectionReason.trim().isEmpty())
            throw new InvalidOperationException("Rejection reason must be provided.");
        claim.setRejectionReason(rejectionReason.trim());
        claim.setStatus(ClaimStatus.REJECTED);
        claim = claimRepository.save(claim);
        notificationService.notifyCustomerClaimRejected(
                claim.getBusiness().getUser(), claim.getAssignedOfficer(), claimId, rejectionReason);
        return claim;
    }

    public Claim settleClaim(Long claimId, BigDecimal settledAmount) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
        if (claim.getStatus() != ClaimStatus.APPROVED)
            throw new InvalidStatusTransitionException("Claim", claim.getStatus().name(), "SETTLED",
                    "Only APPROVED claims can be settled.");
        if (settledAmount == null || settledAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidOperationException("Settled amount must be greater than zero.");
        if (settledAmount.compareTo(claim.getApprovedAmount()) > 0)
            throw new InvalidOperationException(
                    "Settled amount cannot exceed approved amount (" + claim.getApprovedAmount() + ").");
        claim.setSettledAmount(settledAmount);
        claim.setSettlementDate(LocalDate.now());
        claim.setStatus(ClaimStatus.SETTLED);
        claim = claimRepository.save(claim);
        notificationService.notifyCustomerClaimSettled(
                claim.getBusiness().getUser(), claim.getAssignedOfficer(), claimId,
                settledAmount.toString());
        return claim;
    }

    public Claim appealClaim(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
        if (claim.getStatus() != ClaimStatus.REJECTED)
            throw new InvalidStatusTransitionException("Claim", claim.getStatus().name(), "APPEALED",
                    "Only REJECTED claims can be appealed.");
        claim.setStatus(ClaimStatus.APPEALED);
        claim = claimRepository.save(claim);
        notificationService.notifyAdminClaimAppealed(claim.getBusiness().getUser(), claimId);
        return claim;
    }

    public Claim updateClaim(Long claimId, ClaimRequest request) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
        if (claim.getStatus() != ClaimStatus.DRAFT)
            throw new InvalidStatusTransitionException("Claim", claim.getStatus().name(), "UPDATED",
                    "Only DRAFT claims can be updated.");
        if (request.getClaimedAmount() != null)
            claim.setClaimedAmount(request.getClaimedAmount());
        if (request.getIncidentDescription() != null)
            claim.setIncidentDescription(request.getIncidentDescription());
        if (request.getIncidentDate() != null)
            claim.setIncidentDate(request.getIncidentDate());
        return claimRepository.save(claim);
    }

    public void deleteClaim(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", id));
        if (claim.getStatus() != ClaimStatus.DRAFT)
            throw new InvalidOperationException("Only DRAFT claims can be deleted.");
        claimRepository.delete(claim);
    }

    public Claim getClaimById(Long claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "ID", claimId));
    }

    public Claim getClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "number", claimNumber));
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public List<Claim> getClaimsByCustomer(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        return claimRepository.findByBusiness_User_Id(userId);
    }

    public List<Claim> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByStatus(status);
    }

    public List<Claim> getClaimsAssignedToOfficer(Long officerId) {
        userRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", officerId));
        return claimRepository.findByAssignedOfficer_Id(officerId);
    }
}
