package org.hartford.binsure.service;

import org.hartford.binsure.dto.PremiumPaymentRequest;
import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hartford.binsure.enums.PaymentStatus;
import org.hartford.binsure.exception.*;
import org.hartford.binsure.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import org.hartford.binsure.enums.PolicyStatus;

@Service
@Transactional
public class PremiumPaymentService {

    @Autowired
    private PremiumPaymentRepository paymentRepository;
    @Autowired
    private PolicyApplicationRepository applicationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private InsuranceProductRepository productRepository;
    @Autowired
    private UnderwriterDecisionRepository decisionRepository;

    public PremiumPayment makePayment(Long userId, PremiumPaymentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        PolicyApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("PolicyApplication", "ID", request.getApplicationId()));
        if (application.getStatus() != ApplicationStatus.CUSTOMER_ACCEPTED)
            throw new InvalidStatusTransitionException("PolicyApplication",
                    application.getStatus().name(), "PAYMENT",
                    "Payment can only be made for CUSTOMER_ACCEPTED applications.");
        if (paymentRepository.existsByApplication_IdAndStatus(application.getId(), PaymentStatus.PAID))
            throw new PaymentException("A payment has already been made for application ID: " + application.getId());

        PremiumPayment payment = new PremiumPayment();
        payment.setPaymentReference("PAY-" + System.currentTimeMillis());
        payment.setApplication(application);
        payment.setBusiness(application.getBusiness());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setRemarks(request.getRemarks());
        payment = paymentRepository.save(payment);

        // AUTO-CREATE POLICY after successful payment
        Policy policy = createPolicyFromApplication(application);

        notificationService.notifyAdminPaymentReceived(user, application.getId(),
                request.getAmount().toString());
        notificationService.notifyCustomerPolicyIssued(user, application.getAssignedUnderwriter(),
                application.getId(), policy.getPolicyNumber());

        return payment;
    }

    public List<PremiumPayment> getPaymentsByApplication(Long applicationId) {
        applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("PolicyApplication", "ID", applicationId));
        return paymentRepository.findByApplication_Id(applicationId);
    }

    public List<PremiumPayment> getPaymentsByCustomer(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        return paymentRepository.findByBusiness_User_Id(userId);
    }

    public List<PremiumPayment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<PremiumPayment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    /**
     * AUTO-CREATE POLICY from application after successful payment.
     * Called automatically when customer makes payment.
     * Policy is created in ACTIVE status.
     */
    private Policy createPolicyFromApplication(PolicyApplication application) {
        InsuranceProduct product = productRepository.findById(application.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", "ID",
                        application.getProduct().getId()));

        User underwriter = application.getAssignedUnderwriter();

        if (underwriter == null) {
            throw new InvalidOperationException("Cannot issue policy: No underwriter assigned to this application.");
        }

        // Generate unique policy number
        String policyNumber = "POL-" + System.currentTimeMillis();

        // Create policy
        Policy policy = new Policy();
        policy.setPolicyNumber(policyNumber);
        policy.setApplication(application);
        policy.setBusiness(application.getBusiness());
        policy.setProduct(product);
        policy.setUnderwriter(underwriter);
        policy.setCoverageAmount(application.getCoverageAmount());
        policy.setAnnualPremium(application.getAnnualPremium());
        policy.setDeductible(BigDecimal.ZERO);
        policy.setStartDate(application.getCoverageStartDate());
        policy.setEndDate(application.getCoverageEndDate());
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setIssuedAt(LocalDateTime.now());

        policy = policyRepository.save(policy);

        // Update application status
        application.setStatus(ApplicationStatus.POLICY_ISSUED);
        applicationRepository.save(application);

        return policy;
    }
}
