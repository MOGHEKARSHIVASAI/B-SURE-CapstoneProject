package org.hartford.binsure.service;

import org.hartford.binsure.dto.PremiumPaymentRequest;
import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hartford.binsure.enums.PaymentStatus;
import org.hartford.binsure.enums.PolicyStatus;
import org.hartford.binsure.exception.InvalidStatusTransitionException;
import org.hartford.binsure.exception.PaymentException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PremiumPaymentService.
 * Tests payment processing and automatic policy creation flow.
 */
@ExtendWith(MockitoExtension.class)
class PremiumPaymentServiceTest {

    @Mock
    private PremiumPaymentRepository paymentRepository;

    @Mock
    private PolicyApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InsuranceProductRepository productRepository;

    @Mock
    private UnderwriterDecisionRepository decisionRepository;

    @InjectMocks
    private PremiumPaymentService premiumPaymentService;

    private User customer;
    private Business business;
    private PolicyApplication application;
    private PremiumPaymentRequest paymentRequest;
    private InsuranceProduct product;
    private User underwriter;

    @BeforeEach
    void setUp() {
        // Setup customer
        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@example.com");

        // Setup underwriter
        underwriter = new User();
        underwriter.setId(2L);
        underwriter.setEmail("underwriter@example.com");

        // Setup business
        business = new Business();
        business.setId(1L);
        business.setUser(customer);
        business.setCompanyName("Test Company");

        // Setup product
        product = new InsuranceProduct();
        product.setId(1L);
        product.setName("Test Product");
        product.setBasePremiumRate(new BigDecimal("0.05"));

        // Setup application
        application = new PolicyApplication();
        application.setId(1L);
        application.setBusiness(business);
        application.setProduct(product);
        application.setStatus(ApplicationStatus.CUSTOMER_ACCEPTED);
        application.setAssignedUnderwriter(underwriter);
        application.setCoverageAmount(new BigDecimal("100000"));
        application.setAnnualPremium(new BigDecimal("5000"));
        application.setCoverageStartDate(LocalDate.now());
        application.setCoverageEndDate(LocalDate.now().plusYears(1));

        // Setup payment request
        paymentRequest = new PremiumPaymentRequest();
        paymentRequest.setApplicationId(1L);
        paymentRequest.setAmount(new BigDecimal("5000"));
        paymentRequest.setRemarks("Payment for policy coverage");
    }

    @Test
    void testMakePayment_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(paymentRepository.existsByApplication_IdAndStatus(1L, PaymentStatus.PAID))
                .thenReturn(false);
        when(paymentRepository.save(any(PremiumPayment.class)))
                .thenReturn(new PremiumPayment());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(policyRepository.save(any(Policy.class))).thenReturn(new Policy());
        doNothing().when(notificationService).notifyAdminPaymentReceived(any(), anyLong(), anyString());
        doNothing().when(notificationService).notifyCustomerPolicyIssued(any(), any(), anyLong(), anyString());

        // Act
        PremiumPayment result = premiumPaymentService.makePayment(1L, paymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.PAID, result.getStatus());
        verify(userRepository, times(1)).findById(1L);
        verify(applicationRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(PremiumPayment.class));
    }

    @Test
    void testMakePayment_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> premiumPaymentService.makePayment(999L, paymentRequest));
    }

    @Test
    void testMakePayment_ApplicationNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> premiumPaymentService.makePayment(1L, paymentRequest));
    }

    @Test
    void testMakePayment_InvalidApplicationStatus() {
        // Arrange
        application.setStatus(ApplicationStatus.SUBMITTED); // Not CUSTOMER_ACCEPTED
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class,
                () -> premiumPaymentService.makePayment(1L, paymentRequest));
    }

    @Test
    void testMakePayment_AlreadyPaid() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(paymentRepository.existsByApplication_IdAndStatus(1L, PaymentStatus.PAID))
                .thenReturn(true);

        // Act & Assert
        assertThrows(PaymentException.class,
                () -> premiumPaymentService.makePayment(1L, paymentRequest));
    }

    @Test
    void testGetPaymentsByApplication_Success() {
        // Arrange
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        PremiumPayment payment = new PremiumPayment();
        payment.setId(1L);
        payment.setAmount(new BigDecimal("5000"));
        when(paymentRepository.findByApplication_Id(1L))
                .thenReturn(Arrays.asList(payment));

        // Act
        List<PremiumPayment> results = premiumPaymentService.getPaymentsByApplication(1L);

        // Assert
        assertEquals(1, results.size());
        verify(paymentRepository, times(1)).findByApplication_Id(1L);
    }

    @Test
    void testGetPaymentsByCustomer_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        PremiumPayment payment = new PremiumPayment();
        payment.setId(1L);
        when(paymentRepository.findByBusiness_User_Id(1L))
                .thenReturn(Arrays.asList(payment));

        // Act
        List<PremiumPayment> results = premiumPaymentService.getPaymentsByCustomer(1L);

        // Assert
        assertEquals(1, results.size());
        verify(paymentRepository, times(1)).findByBusiness_User_Id(1L);
    }

    @Test
    void testGetPaymentsByStatus_Success() {
        // Arrange
        PremiumPayment payment = new PremiumPayment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findByStatus(PaymentStatus.PAID))
                .thenReturn(Arrays.asList(payment));

        // Act
        List<PremiumPayment> results = premiumPaymentService.getPaymentsByStatus(PaymentStatus.PAID);

        // Assert
        assertEquals(1, results.size());
        assertEquals(PaymentStatus.PAID, results.get(0).getStatus());
        verify(paymentRepository, times(1)).findByStatus(PaymentStatus.PAID);
    }

    @Test
    void testGetAllPayments_Success() {
        // Arrange
        PremiumPayment payment = new PremiumPayment();
        payment.setId(1L);
        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment));

        // Act
        List<PremiumPayment> results = premiumPaymentService.getAllPayments();

        // Assert
        assertEquals(1, results.size());
        verify(paymentRepository, times(1)).findAll();
    }
}

