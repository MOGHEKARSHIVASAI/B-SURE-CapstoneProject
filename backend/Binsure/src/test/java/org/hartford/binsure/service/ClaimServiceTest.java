package org.hartford.binsure.service;

import org.hartford.binsure.dto.ClaimRequest;
import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.ClaimStatus;
import org.hartford.binsure.enums.PolicyStatus;
import org.hartford.binsure.exception.InvalidOperationException;
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
 * Unit tests for ClaimService.
 * Tests claim lifecycle: filing, assignment, investigation, approval, rejection, settlement.
 */
@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClaimService claimService;

    private Policy policy;
    private Claim claim;
    private ClaimRequest claimRequest;
    private User customer;
    private Business business;

    @BeforeEach
    void setUp() {
        // Setup customer
        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@example.com");
        customer.setRole(org.hartford.binsure.enums.UserRole.CUSTOMER);

        // Setup business
        business = new Business();
        business.setId(1L);
        business.setUser(customer);
        business.setCompanyName("Test Company");

        // Setup policy
        policy = new Policy();
        policy.setId(1L);
        policy.setPolicyNumber("POL-123456");
        policy.setBusiness(business);
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.now().minusDays(10));
        policy.setEndDate(LocalDate.now().plusDays(100));

        // Setup claim request
        claimRequest = new ClaimRequest();
        claimRequest.setPolicyId(1L);
        claimRequest.setIncidentDate(LocalDate.now().minusDays(5));
        claimRequest.setClaimedAmount(new BigDecimal("50000"));
        claimRequest.setIncidentDescription("Property damage claim");

        // Setup claim
        claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-" + System.currentTimeMillis());
        claim.setPolicy(policy);
        claim.setBusiness(business);
        claim.setStatus(ClaimStatus.DRAFT);
        claim.setClaimedAmount(new BigDecimal("50000"));
        claim.setIncidentDescription("Property damage claim");
        claim.setIncidentDate(LocalDate.now().minusDays(5));
    }

    @Test
    void testFileNewClaim_Success() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        // Act
        Claim result = claimService.fileNewClaim(claimRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(ClaimStatus.DRAFT, result.getStatus());
        assertEquals(new BigDecimal("50000"), result.getClaimedAmount());
        verify(policyRepository, times(1)).findById(1L);
        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    void testFileNewClaim_PolicyNotFound() {
        // Arrange
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> claimService.fileNewClaim(claimRequest, 1L));
    }

    @Test
    void testFileNewClaim_IncidentDateInFuture() {
        // Arrange
        claimRequest.setIncidentDate(LocalDate.now().plusDays(10)); // Future date
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> claimService.fileNewClaim(claimRequest, 1L));
    }

    @Test
    void testFileNewClaim_IncidentDateBeforePolicyStart() {
        // Arrange
        claimRequest.setIncidentDate(LocalDate.now().minusDays(20)); // Before policy start
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> claimService.fileNewClaim(claimRequest, 1L));
    }

    @Test
    void testSubmitClaim_Success() {
        // Arrange
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        doNothing().when(notificationService).notifyClaimSubmitted(any(), anyLong());

        // Act
        Claim result = claimService.submitClaim(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ClaimStatus.SUBMITTED, result.getStatus());
        verify(claimRepository, times(1)).findById(1L);
    }

    @Test
    void testSubmitClaim_OnlyDraftCanBeSubmitted() {
        // Arrange
        claim.setStatus(ClaimStatus.SUBMITTED); // Already submitted
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> claimService.submitClaim(1L));
    }

    @Test
    void testAssignClaimsOfficer_Success() {
        // Arrange
        User officer = new User();
        officer.setId(2L);
        officer.setEmail("officer@example.com");
        officer.setRole(org.hartford.binsure.enums.UserRole.CLAIMS_OFFICER);

        claim.setStatus(ClaimStatus.SUBMITTED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(userRepository.findById(2L)).thenReturn(Optional.of(officer));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        doNothing().when(notificationService).notifyOfficerClaimAssigned(any(), any(), anyLong());

        // Act
        Claim result = claimService.assignClaimsOfficer(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(ClaimStatus.ASSIGNED, result.getStatus());
        assertNotNull(result.getAssignedOfficer());
        verify(claimRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void testMarkUnderInvestigation_Success() {
        // Arrange
        claim.setStatus(ClaimStatus.SUBMITTED);
        User officer = new User();
        officer.setId(2L);
        claim.setAssignedOfficer(officer);

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        doNothing().when(notificationService).notifyCustomerClaimInvestigation(any(), any(), anyLong());

        // Act
        Claim result = claimService.markUnderInvestigation(1L);

        // Assert
        assertEquals(ClaimStatus.UNDER_INVESTIGATION, result.getStatus());
        verify(claimRepository, times(1)).findById(1L);
    }

    @Test
    void testApproveClaim_Success() {
        // Arrange
        claim.setStatus(ClaimStatus.UNDER_INVESTIGATION);
        BigDecimal approvedAmount = new BigDecimal("45000");

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        doNothing().when(notificationService).notifyCustomerClaimApproved(any(), any(), anyLong(), anyString());

        // Act
        Claim result = claimService.approveClaim(1L, approvedAmount);

        // Assert
        assertEquals(ClaimStatus.APPROVED, result.getStatus());
        assertEquals(approvedAmount, result.getApprovedAmount());
        verify(claimRepository, times(1)).findById(1L);
    }

    @Test
    void testApproveClaim_ApprovedAmountExceedsClaimed() {
        // Arrange
        claim.setStatus(ClaimStatus.UNDER_INVESTIGATION);
        BigDecimal excessiveAmount = new BigDecimal("60000"); // Exceeds claimed 50000

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> claimService.approveClaim(1L, excessiveAmount));
    }

    @Test
    void testRejectClaim_Success() {
        // Arrange
        claim.setStatus(ClaimStatus.UNDER_INVESTIGATION);
        String rejectionReason = "Insufficient documentation";

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        doNothing().when(notificationService).notifyCustomerClaimRejected(any(), any(), anyLong(), anyString());

        // Act
        Claim result = claimService.rejectClaim(1L, rejectionReason);

        // Assert
        assertEquals(ClaimStatus.REJECTED, result.getStatus());
        assertEquals(rejectionReason, result.getRejectionReason());
        verify(claimRepository, times(1)).findById(1L);
    }

    @Test
    void testSettleClaim_Success() {
        // Arrange
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAmount(new BigDecimal("45000"));
        BigDecimal settledAmount = new BigDecimal("45000");

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        doNothing().when(notificationService).notifyCustomerClaimSettled(any(), any(), anyLong(), anyString());

        // Act
        Claim result = claimService.settleClaim(1L, settledAmount);

        // Assert
        assertEquals(ClaimStatus.SETTLED, result.getStatus());
        assertEquals(settledAmount, result.getSettledAmount());
        assertNotNull(result.getSettlementDate());
        verify(claimRepository, times(1)).findById(1L);
    }

    @Test
    void testGetClaimsByCustomer_Success() {
        // Arrange
        List<Claim> claims = Arrays.asList(claim);
        when(claimRepository.findByBusiness_User_Id(1L)).thenReturn(claims);

        // Act
        List<Claim> results = claimService.getClaimsByCustomer(1L);

        // Assert
        assertEquals(1, results.size());
        verify(claimRepository, times(1)).findByBusiness_User_Id(1L);
    }

    @Test
    void testGetAllClaims_Success() {
        // Arrange
        List<Claim> claims = Arrays.asList(claim);
        when(claimRepository.findAll()).thenReturn(claims);

        // Act
        List<Claim> results = claimService.getAllClaims();

        // Assert
        assertEquals(1, results.size());
        verify(claimRepository, times(1)).findAll();
    }
}

