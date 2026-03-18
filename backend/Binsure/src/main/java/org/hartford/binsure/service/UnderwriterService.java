package org.hartford.binsure.service;

import org.hartford.binsure.dto.UnderwriterDecisionRequest;
import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hartford.binsure.enums.DecisionType;
import org.hartford.binsure.exception.*;
import org.hartford.binsure.repository.PolicyApplicationRepository;
import org.hartford.binsure.repository.UnderwriterDecisionRepository;
import org.hartford.binsure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UnderwriterService {

        @Autowired
        private UnderwriterDecisionRepository decisionRepository;
        @Autowired
        private PolicyApplicationRepository applicationRepository;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private NotificationService notificationService;

        public UnderwriterDecision submitDecision(Long applicationId,
                        UnderwriterDecisionRequest request,
                        Long underwriterId) {
                PolicyApplication application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID",
                                                applicationId));
                if (application.getStatus() != ApplicationStatus.UNDER_REVIEW)
                        throw new InvalidStatusTransitionException("PolicyApplication",
                                        application.getStatus().name(), "DECISION_SUBMITTED",
                                        "Application must be UNDER_REVIEW to receive a decision.");
                User underwriter = userRepository.findById(underwriterId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", underwriterId));
                if (application.getAssignedUnderwriter() != null
                                && !application.getAssignedUnderwriter().getId().equals(underwriterId))
                        throw new UnauthorizedActionException(
                                        "Underwriter ID " + underwriterId + " is not assigned to application ID "
                                                        + applicationId);

                UnderwriterDecision decision = new UnderwriterDecision();
                decision.setApplication(application);
                decision.setUnderwriter(underwriter);
                decision.setDecision(request.getDecision());
                decision.setRiskScore(request.getRiskScore());
                decision.setPremiumAdjustmentPct(request.getPremiumAdjustmentPct());
                decision.setComments(request.getComments());
                decision.setDecidedAt(LocalDateTime.now());
                decision = decisionRepository.save(decision);

                if (request.getDecision() == DecisionType.APPROVED) {
                        application.setStatus(ApplicationStatus.APPROVED);
                        // Calculate final premium during approval
                        BigDecimal basePremium = application.getProduct().getBasePremiumRate()
                                        .multiply(application.getCoverageAmount());
                        BigDecimal multiplier = BigDecimal.ONE.add(request.getPremiumAdjustmentPct()
                                        .divide(BigDecimal.valueOf(100)));
                        application.setAnnualPremium(basePremium.multiply(multiplier));
                        application.setPremiumAdjustmentPct(request.getPremiumAdjustmentPct());
                } else if (request.getDecision() == DecisionType.REJECTED) {
                        application.setStatus(ApplicationStatus.REJECTED);
                }
                application.setReviewedAt(LocalDateTime.now());
                applicationRepository.save(application);

                notificationService.notifyCustomerApplicationDecision(
                                application.getBusiness().getUser(), underwriter,
                                applicationId, request.getDecision().name());
                return decision;
        }

        public void customerAcceptDecision(Long applicationId, Long customerUserId) {
                PolicyApplication application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID",
                                                applicationId));
                if (application.getStatus() != ApplicationStatus.APPROVED)
                        throw new InvalidStatusTransitionException("PolicyApplication",
                                        application.getStatus().name(), "CUSTOMER_ACCEPTED",
                                        "Application must be APPROVED before the customer can accept.");
                User customerUser = userRepository.findById(customerUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", customerUserId));
                if (!application.getBusiness().getUser().getId().equals(customerUserId))
                        throw new UnauthorizedActionException(
                                        "You are not authorized to accept application ID " + applicationId);
                application.setStatus(ApplicationStatus.CUSTOMER_ACCEPTED);
                applicationRepository.save(application);
                notificationService.notifyAdminCustomerAccepted(customerUser, applicationId);
        }

        public void customerRejectDecision(Long applicationId, Long customerUserId) {
                PolicyApplication application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID",
                                                applicationId));
                if (application.getStatus() != ApplicationStatus.APPROVED)
                        throw new InvalidStatusTransitionException("PolicyApplication",
                                        application.getStatus().name(), "CUSTOMER_REJECTED",
                                        "Application must be APPROVED before the customer can reject.");
                User customerUser = userRepository.findById(customerUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", customerUserId));
                if (!application.getBusiness().getUser().getId().equals(customerUserId))
                        throw new UnauthorizedActionException(
                                        "You are not authorized to reject application ID " + applicationId);
                application.setStatus(ApplicationStatus.CUSTOMER_REJECTED);
                applicationRepository.save(application);
                notificationService.notifyAdminCustomerRejected(customerUser, applicationId);
        }

        public UnderwriterDecision getDecisionById(Long decisionId) {
                return decisionRepository.findById(decisionId)
                                .orElseThrow(() -> new ResourceNotFoundException("UnderwriterDecision", "ID",
                                                decisionId));
        }

        public List<UnderwriterDecision> getDecisionsByApplication(Long applicationId) {
                applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID",
                                                applicationId));
                return decisionRepository.findByApplication_Id(applicationId);
        }

        public UnderwriterDecision getLatestDecisionForApplication(Long applicationId) {
                return decisionRepository.findFirstByApplication_IdOrderByDecidedAtDesc(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException("UnderwriterDecision", "applicationId",
                                                applicationId));
        }

        public List<UnderwriterDecision> getAllDecisions() {
                return decisionRepository.findAll();
        }

        public List<PolicyApplication> getUnderwritingQueue(Long underwriterId) {
                userRepository.findById(underwriterId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", underwriterId));
                return applicationRepository.findByAssignedUnderwriter_Id(underwriterId).stream()
                                .filter(app -> app.getStatus() == ApplicationStatus.UNDER_REVIEW)
                                .toList();
        }

        public Integer calculateRiskScore(Long applicationId) {
                PolicyApplication application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID",
                                                applicationId));
                int riskScore = 50;
                if (application.getCoverageAmount().intValue() > 1000000)
                        riskScore += 10;
                if (application.getBusiness().getNumEmployees() != null
                                && application.getBusiness().getNumEmployees() > 500)
                        riskScore -= 5;
                String industryType = application.getBusiness().getIndustryType();
                if (industryType != null && industryType.toLowerCase().contains("tech"))
                        riskScore += 15;
                return Math.max(0, Math.min(100, riskScore));
        }
}
