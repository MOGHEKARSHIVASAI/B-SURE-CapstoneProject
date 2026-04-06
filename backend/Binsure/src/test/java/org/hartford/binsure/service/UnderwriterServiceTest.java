package org.hartford.binsure.service;

import org.hartford.binsure.dto.UnderwriterDecisionRequest;
import org.hartford.binsure.entity.PolicyApplication;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.entity.UnderwriterDecision;
import org.hartford.binsure.entity.InsuranceProduct;
import org.hartford.binsure.entity.Business;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hartford.binsure.enums.DecisionType;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.InvalidStatusTransitionException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.exception.UnauthorizedActionException;
import org.hartford.binsure.repository.PolicyApplicationRepository;
import org.hartford.binsure.repository.UnderwriterDecisionRepository;
import org.hartford.binsure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UnderwriterService.
 * Tests underwriter decision submission and application review workflow.
 */
@ExtendWith(MockitoExtension.class)
class UnderwriterServiceTest {

    @Mock
    private UnderwriterDecisionRepository decisionRepository;

    @Mock
    private PolicyApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AiService aiService;

    @InjectMocks
    private UnderwriterService underwriterService;

    private PolicyApplication application;
    private User underwriter;
    private User customer;
    private Business business;
    private InsuranceProduct product;
    private UnderwriterDecisionRequest decisionRequest;

    @BeforeEach
    void setUp() {
        // Setup customer
        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@example.com");
        customer.setRole(UserRole.CUSTOMER);

        // Setup underwriter
        underwriter = new User();
        underwriter.setId(2L);
        underwriter.setEmail("underwriter@example.com");
        underwriter.setRole(UserRole.UNDERWRITER);

        // Setup business
        business = new Business();
        business.setId(1L);
        business.setUser(customer);

        // Setup product
        product = new InsuranceProduct();
        product.setId(1L);
        product.setBasePremiumRate(new BigDecimal("0.05"));

        // Setup application
        application = new PolicyApplication();
        application.setId(1L);
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setBusiness(business);
        application.setProduct(product);
        application.setAssignedUnderwriter(underwriter);
        application.setCoverageAmount(new BigDecimal("100000"));

        // Setup decision request
        decisionRequest = new UnderwriterDecisionRequest();
        decisionRequest.setDecision(DecisionType.APPROVED);
        decisionRequest.setRiskScore(75);
        decisionRequest.setPremiumAdjustmentPct(new BigDecimal("10"));
        decisionRequest.setComments("Approved with minor adjustments");
    }

    @Test
    void testSubmitDecision_ApprovalSuccess() {
        // Arrange
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findById(2L)).thenReturn(Optional.of(underwriter));
        when(decisionRepository.save(any(UnderwriterDecision.class)))
                .thenReturn(new UnderwriterDecision());
        when(applicationRepository.save(any(PolicyApplication.class)))
                .thenReturn(application);
        doNothing().when(notificationService).notifyCustomerApplicationDecision(any(), any(), anyLong(), anyString());

        // Act
        UnderwriterDecision result = underwriterService.submitDecision(1L, decisionRequest, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(DecisionType.APPROVED, result.getDecision());
        assertEquals(75, result.getRiskScore());
        verify(decisionRepository, times(1)).save(any(UnderwriterDecision.class));
    }

    @Test
    void testSubmitDecision_RejectionSuccess() {
        // Arrange
        decisionRequest.setDecision(DecisionType.REJECTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findById(2L)).thenReturn(Optional.of(underwriter));
        when(decisionRepository.save(any(UnderwriterDecision.class)))
                .thenReturn(new UnderwriterDecision());
        when(applicationRepository.save(any(PolicyApplication.class)))
                .thenReturn(application);
        doNothing().when(notificationService).notifyCustomerApplicationDecision(any(), any(), anyLong(), anyString());

        // Act
        UnderwriterDecision result = underwriterService.submitDecision(1L, decisionRequest, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(DecisionType.REJECTED, result.getDecision());
    }

    @Test
    void testSubmitDecision_ApplicationNotFound() {
        // Arrange
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> underwriterService.submitDecision(999L, decisionRequest, 2L));
    }

    @Test
    void testSubmitDecision_ApplicationNotUnderReview() {
        // Arrange
        application.setStatus(ApplicationStatus.DRAFT);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class,
                () -> underwriterService.submitDecision(1L, decisionRequest, 2L));
    }

    @Test
    void testSubmitDecision_UnauthorizedUnderwriter() {
        // Arrange
        User otherUnderwriter = new User();
        otherUnderwriter.setId(3L);
        application.setAssignedUnderwriter(otherUnderwriter);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findById(2L)).thenReturn(Optional.of(underwriter));

        // Act & Assert
        assertThrows(UnauthorizedActionException.class,
                () -> underwriterService.submitDecision(1L, decisionRequest, 2L));
    }

    @Test
    void testCustomerAcceptDecision_Success() {
        // Arrange
        application.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(applicationRepository.save(any(PolicyApplication.class))).thenReturn(application);
        doNothing().when(notificationService).notifyAdminCustomerAccepted(any(), anyLong());

        // Act
        underwriterService.customerAcceptDecision(1L, 1L);

        // Assert
        assertEquals(ApplicationStatus.CUSTOMER_ACCEPTED, application.getStatus());
        verify(applicationRepository, times(1)).save(any(PolicyApplication.class));
    }

    @Test
    void testCustomerAcceptDecision_NotApproved() {
        // Arrange
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class,
                () -> underwriterService.customerAcceptDecision(1L, 1L));
    }

    @Test
    void testCustomerAcceptDecision_Unauthorized() {
        // Arrange
        application.setStatus(ApplicationStatus.APPROVED);
        User otherCustomer = new User();
        otherCustomer.setId(3L);
        business.setUser(otherCustomer);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Act & Assert
        assertThrows(UnauthorizedActionException.class,
                () -> underwriterService.customerAcceptDecision(1L, 1L));
    }

    @Test
    void testGetUnderwritingQueue_Success() {
        // Arrange
        List<PolicyApplication> queue = Arrays.asList(application);
        when(applicationRepository.findByStatus(ApplicationStatus.UNDER_REVIEW))
                .thenReturn(queue);

        // Act
        List<PolicyApplication> results = underwriterService.getUnderwritingQueue(2L);

        // Assert
        assertEquals(1, results.size());
    }

    @Test
    void testCalculateRiskScore_Success() {
        // Arrange
        application.getCusiness().setNumEmployees(50);
        application.getProduct().setBasePremiumRate(new BigDecimal("0.05"));

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act
        int riskScore = underwriterService.calculateRiskScore(1L);

        // Assert
        assertTrue(riskScore >= 0 && riskScore <= 100);
    }
}

