package org.hartford.binsure.service;

import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.PolicyStatus;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.exception.InvalidStatusTransitionException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PolicyService.
 * Tests policy lifecycle: suspension, reactivation, cancellation, and retrieval.
 */
@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PolicyService policyService;

    private Policy policy;
    private User user;
    private Business business;
    private InsuranceProduct product;

    @BeforeEach
    void setUp() {
        // Setup user
        user = new User();
        user.setId(1L);
        user.setEmail("customer@example.com");

        // Setup business
        business = new Business();
        business.setId(1L);
        business.setUser(user);
        business.setCompanyName("Test Company");

        // Setup product
        product = new InsuranceProduct();
        product.setId(1L);
        product.setName("Test Product");
        product.setBasePremiumRate(new BigDecimal("0.05"));

        // Setup policy
        policy = new Policy();
        policy.setId(1L);
        policy.setPolicyNumber("POL-123456");
        policy.setBusiness(business);
        policy.setProduct(product);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(1));
        policy.setAnnualPremium(new BigDecimal("10000"));
        policy.setCoverageAmount(new BigDecimal("100000"));
    }

    @Test
    void testGetPolicyById_Success() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // Act
        Policy result = policyService.getPolicyById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("POL-123456", result.getPolicyNumber());
        verify(policyRepository, times(1)).findById(1L);
    }

    @Test
    void testGetPolicyById_NotFound() {
        // Arrange
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> policyService.getPolicyById(999L));
    }

    @Test
    void testGetPolicyByNumber_Success() {
        // Arrange
        when(policyRepository.findByPolicyNumber("POL-123456")).thenReturn(Optional.of(policy));

        // Act
        Policy result = policyService.getPolicyByNumber("POL-123456");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(policyRepository, times(1)).findByPolicyNumber("POL-123456");
    }

    @Test
    void testGetAllPolicies_Success() {
        // Arrange
        List<Policy> policies = Arrays.asList(policy);
        when(policyRepository.findAll()).thenReturn(policies);

        // Act
        List<Policy> results = policyService.getAllPolicies();

        // Assert
        assertEquals(1, results.size());
        verify(policyRepository, times(1)).findAll();
    }

    @Test
    void testGetPoliciesByCustomer_Success() {
        // Arrange
        List<Policy> policies = Arrays.asList(policy);
        when(policyRepository.findByBusiness_User_Id(1L)).thenReturn(policies);

        // Act
        List<Policy> results = policyService.getPoliciesByCustomer(1L);

        // Assert
        assertEquals(1, results.size());
        verify(policyRepository, times(1)).findByBusiness_User_Id(1L);
    }

    @Test
    void testGetPoliciesByBusiness_Success() {
        // Arrange
        List<Policy> policies = Arrays.asList(policy);
        when(policyRepository.findByBusiness_Id(1L)).thenReturn(policies);

        // Act
        List<Policy> results = policyService.getPoliciesByBusiness(1L);

        // Assert
        assertEquals(1, results.size());
        verify(policyRepository, times(1)).findByBusiness_Id(1L);
    }

    @Test
    void testGetPoliciesByStatus_Success() {
        // Arrange
        List<Policy> policies = Arrays.asList(policy);
        when(policyRepository.findByStatus(PolicyStatus.ACTIVE)).thenReturn(policies);

        // Act
        List<Policy> results = policyService.getPoliciesByStatus(PolicyStatus.ACTIVE);

        // Assert
        assertEquals(1, results.size());
        assertEquals(PolicyStatus.ACTIVE, results.get(0).getStatus());
        verify(policyRepository, times(1)).findByStatus(PolicyStatus.ACTIVE);
    }

    @Test
    void testGetExpiringPolicies_Success() {
        // Arrange
        List<Policy> expiringPolicies = Arrays.asList(policy);
        when(policyRepository.findExpiringPolicies(any(), any(), any()))
                .thenReturn(expiringPolicies);

        // Act
        List<Policy> results = policyService.getExpiringPolicies(30);

        // Assert
        assertEquals(1, results.size());
        verify(policyRepository, times(1)).findExpiringPolicies(any(), any(), any());
    }

    @Test
    void testGetExpiringPolicies_InvalidDays() {
        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> policyService.getExpiringPolicies(-5));
    }

    @Test
    void testSuspendPolicy_Success() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        // Act
        Policy result = policyService.suspendPolicy(1L);

        // Assert
        assertEquals(PolicyStatus.SUSPENDED, result.getStatus());
        verify(policyRepository, times(1)).findById(1L);
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void testSuspendPolicy_NotActive() {
        // Arrange
        policy.setStatus(PolicyStatus.SUSPENDED);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class,
                () -> policyService.suspendPolicy(1L));
    }

    @Test
    void testReactivatePolicy_Success() {
        // Arrange
        policy.setStatus(PolicyStatus.SUSPENDED);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        // Act
        Policy result = policyService.reactivatePolicy(1L);

        // Assert
        assertEquals(PolicyStatus.ACTIVE, result.getStatus());
        verify(policyRepository, times(1)).findById(1L);
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void testReactivatePolicy_NotSuspended() {
        // Arrange
        policy.setStatus(PolicyStatus.ACTIVE);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class,
                () -> policyService.reactivatePolicy(1L));
    }

    @Test
    void testCancelPolicy_Success() {
        // Arrange
        String cancellationReason = "Customer request";
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        // Act
        Policy result = policyService.cancelPolicy(1L, cancellationReason);

        // Assert
        assertEquals(PolicyStatus.CANCELLED, result.getStatus());
        assertEquals(cancellationReason, result.getCancellationReason());
        assertNotNull(result.getCancelledAt());
        verify(policyRepository, times(1)).findById(1L);
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    void testCancelPolicy_AlreadyCancelled() {
        // Arrange
        policy.setStatus(PolicyStatus.CANCELLED);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> policyService.cancelPolicy(1L, "Customer request"));
    }

    @Test
    void testCancelPolicy_MissingReason() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> policyService.cancelPolicy(1L, null));
    }

    @Test
    void testCancelPolicy_EmptyReason() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> policyService.cancelPolicy(1L, "   "));
    }
}

