package org.hartford.binsure.service;

import org.hartford.binsure.dto.PolicyApplicationDTO;
import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.*;
import org.hartford.binsure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PolicyApplicationService {

    @Autowired
    private PolicyApplicationRepository applicationRepository;
    @Autowired
    private InsuranceProductRepository productRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;

    // Duplicate businessRepository removed

    public PolicyApplication submitApplication(PolicyApplicationDTO request, Long userId) {
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResourceNotFoundException("Business", "ID", request.getBusinessId()));
        if (!business.getUser().getId().equals(userId)) {
            throw new UnauthorizedActionException("This business does not belong to you.");
        }
        InsuranceProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", "ID", request.getProductId()));
        if (!product.isActive())
            throw new InvalidOperationException(
                    "Insurance product '" + product.getProductName() + "' is not available.");
        if (request.getCoverageStartDate() != null
                && request.getCoverageStartDate().isBefore(java.time.LocalDate.now()))
            throw new InvalidOperationException("Coverage start date cannot be in the past.");
        if (request.getCoverageStartDate() != null && request.getCoverageEndDate() != null
                && !request.getCoverageEndDate().isAfter(request.getCoverageStartDate()))
            throw new InvalidOperationException("Coverage end date must be after the start date.");

        PolicyApplication application = new PolicyApplication();
        application.setBusiness(business);
        application.setProduct(product);
        application.setCoverageAmount(request.getCoverageAmount());
        application.setCoverageStartDate(request.getCoverageStartDate());
        application.setCoverageEndDate(request.getCoverageEndDate());
        application.setRiskNotes(request.getRiskNotes());
        application.setStatus(ApplicationStatus.DRAFT);
        application.setCreatedAt(LocalDateTime.now());
        return applicationRepository.save(application);
    }

    public PolicyApplication getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID", applicationId));
    }

    public List<PolicyApplication> getApplicationsByCustomer(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        return applicationRepository.findByBusiness_User_Id(userId);
    }

    public List<PolicyApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<PolicyApplication> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<PolicyApplication> getApplicationsAssignedToUnderwriter(Long underwriterId) {
        userRepository.findById(underwriterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", underwriterId));
        return applicationRepository.findByAssignedUnderwriter_Id(underwriterId);
    }

    public List<PolicyApplication> getUnassignedApplications() {
        return applicationRepository.findByAssignedUnderwriterIsNull();
    }

    public PolicyApplication updateApplication(Long applicationId, PolicyApplicationDTO request) {
        PolicyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID", applicationId));
        if (application.getStatus() != ApplicationStatus.DRAFT)
            throw new InvalidStatusTransitionException("PolicyApplication",
                    application.getStatus().name(), "UPDATED", "Only DRAFT applications can be modified.");
        if (request.getCoverageAmount() != null)
            application.setCoverageAmount(request.getCoverageAmount());
        if (request.getCoverageStartDate() != null)
            application.setCoverageStartDate(request.getCoverageStartDate());
        if (request.getCoverageEndDate() != null)
            application.setCoverageEndDate(request.getCoverageEndDate());
        if (request.getRiskNotes() != null)
            application.setRiskNotes(request.getRiskNotes());
        return applicationRepository.save(application);
    }

    public PolicyApplication submitForReview(Long applicationId) {
        PolicyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID", applicationId));
        if (application.getStatus() != ApplicationStatus.DRAFT)
            throw new InvalidStatusTransitionException("PolicyApplication",
                    application.getStatus().name(), "SUBMITTED", "Only DRAFT applications can be submitted.");
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());
        application = applicationRepository.save(application);
        notificationService.notifyApplicationSubmitted(application.getBusiness().getUser(),
                application.getId());
        return application;
    }

    public PolicyApplication assignUnderwriter(Long applicationId, Long underwriterId) {
        PolicyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID", applicationId));
        if (application.getStatus() != ApplicationStatus.SUBMITTED)
            throw new InvalidStatusTransitionException("PolicyApplication",
                    application.getStatus().name(), "UNDER_REVIEW", "Only SUBMITTED applications can be assigned.");
        User underwriter = userRepository.findById(underwriterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", underwriterId));
        if (underwriter.getRole() != UserRole.UNDERWRITER)
            throw new InvalidOperationException("User ID " + underwriterId + " is not an UNDERWRITER.");
        application.setAssignedUnderwriter(underwriter);
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application = applicationRepository.save(application);
        notificationService.notifyUnderwriterAssigned(underwriter, null, application.getId());
        return application;
    }

    public void deleteApplication(Long applicationId) {
        PolicyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID", applicationId));
        if (application.getStatus() != ApplicationStatus.DRAFT)
            throw new InvalidStatusTransitionException("PolicyApplication",
                    application.getStatus().name(), "DELETED", "Only DRAFT applications can be deleted.");
        applicationRepository.delete(application);
    }
}
