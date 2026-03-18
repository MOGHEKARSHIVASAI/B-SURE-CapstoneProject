package org.hartford.binsure.service;

import org.hartford.binsure.dto.PolicyApplicationDTO;
import org.hartford.binsure.entity.*;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hartford.binsure.exception.*;
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
 * Unit tests for PolicyApplicationService.
 * Tests policy application lifecycle: creation, submission, assignment, review.
 */
@ExtendWith(MockitoExtension.class)
class PolicyApplicationServiceTest {

    @Mock
    private PolicyApplicationRepository applicationRepository;

    @Mock
    private InsuranceProductRepository productRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PolicyApplicationService policyApplicationService;

    private PolicyApplicationDTO applicationDTO;
    private PolicyApplication application;
    private Business business;
    private User customer;
    private InsuranceProduct product;

    @BeforeEach
    void setUp() {
        // Setup customer
        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@example.com");

        // Setup business
        business = new Business();
        business.setId(1L);
        business.setUser(customer);
        business.setCompanyName("Test Company");
        business.setNumEmployees(50);
        business.setIndustryType("Technology");

        // Setup product
        product = new InsuranceProduct();
        product.setId(1L);
        product.setProductName("Standard Coverage");
        product.setActive(true);
        product.setBasePremiumRate(new BigDecimal("0.05"));

        // Setup application DTO
        applicationDTO = new PolicyApplicationDTO();
        applicationDTO.setBusinessId(1L);
        applicationDTO.setProductId(1L);
        applicationDTO.setCoverageAmount(new BigDecimal("100000"));
        applicationDTO.setCoverageStartDate(LocalDate.now().plusDays(1));
        applicationDTO.setCoverageEndDate(LocalDate.now().plusYears(1));
        applicationDTO.setRiskNotes("Standard business");

        // Setup application entity
        application = new PolicyApplication();
        application.setId(1L);
        application.setBusiness(business);
        application.setProduct(product);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setCoverageAmount(new BigDecimal("100000"));
    }

    @Test
    void testSubmitApplication_Success() {
        // Arrange
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(applicationRepository.save(any(PolicyApplication.class))).thenReturn(application);

        // Act
        PolicyApplication result = policyApplicationService.submitApplication(applicationDTO, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.DRAFT, result.getStatus());
        assertEquals(new BigDecimal("100000"), result.getCoverageAmount());
        verify(businessRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testSubmitApplication_BusinessNotFound() {
        // Arrange
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> policyApplicationService.submitApplication(applicationDTO, 1L));
    }

    @Test
    void testSubmitApplication_UnauthorizedBusiness() {
        // Arrange
        User otherCustomer = new User();
        otherCustomer.setId(2L);
        business.setUser(otherCustomer);

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        // Act & Assert
        assertThrows(UnauthorizedActionException.class,
                () -> policyApplicationService.submitApplication(applicationDTO, 1L));
    }

    @Test
    void testSubmitApplication_ProductNotActive() {
        // Arrange
        product.setActive(false);
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> policyApplicationService.submitApplication(applicationDTO, 1L));
    }

    @Test
    void testSubmitApplication_StartDateInPast() {
        // Arrange
        applicationDTO.setCoverageStartDate(LocalDate.now().minusDays(1));
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> policyApplicationService.submitApplication(applicationDTO, 1L));
    }

    @Test
    void testSubmitApplication_EndDateBeforeStartDate() {
        // Arrange
        applicationDTO.setCoverageStartDate(LocalDate.now().plusDays(10));
        applicationDTO.setCoverageEndDate(LocalDate.now().plusDays(5)); // Before start
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(InvalidOperationException.class,
                () -> policyApplicationService.submitApplication(applicationDTO, 1L));
    }

    @Test
    void testGetApplicationById_Success() {
        // Arrange
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act
        PolicyApplication result = policyApplicationService.getApplicationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(applicationRepository, times(1)).findById(1L);
    }

    @Test
    void testGetApplicationById_NotFound() {
        // Arrange
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> policyApplicationService.getApplicationById(999L));
    }

    @Test
    void testGetApplicationsByCustomer_Success() {
        // Arrange
        List<PolicyApplication> applications = Arrays.asList(application);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(applicationRepository.findByBusiness_User_Id(1L)).thenReturn(applications);

        // Act
        List<PolicyApplication> results = policyApplicationService.getApplicationsByCustomer(1L);

        // Assert
        assertEquals(1, results.size());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetApplicationsByStatus_Success() {
        // Arrange
        List<PolicyApplication> applications = Arrays.asList(application);
        when(applicationRepository.findByStatus(ApplicationStatus.DRAFT))
                .thenReturn(applications);

        // Act
        List<PolicyApplication> results = policyApplicationService.getApplicationsByStatus(ApplicationStatus.DRAFT);

        // Assert
        assertEquals(1, results.size());
        assertEquals(ApplicationStatus.DRAFT, results.get(0).getStatus());
    }

    @Test
    void testUpdateApplication_Success() {
        // Arrange
        application.setStatus(ApplicationStatus.DRAFT);
        PolicyApplicationDTO updateDTO = new PolicyApplicationDTO();
        updateDTO.setCoverageAmount(new BigDecimal("150000"));

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(PolicyApplication.class))).thenReturn(application);

        // Act
        PolicyApplication result = policyApplicationService.updateApplication(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(applicationRepository, times(1)).findById(1L);
        verify(applicationRepository, times(1)).save(any(PolicyApplication.class));
    }

    @Test
    void testUpdateApplication_NotDraft() {
        // Arrange
        application.setStatus(ApplicationStatus.SUBMITTED);
        PolicyApplicationDTO updateDTO = new PolicyApplicationDTO();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class,
                () -> policyApplicationService.updateApplication(1L, updateDTO));
    }

    @Test
    void testSubmitForReview_Success() {
        // Arrange
        application.setStatus(ApplicationStatus.DRAFT);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(PolicyApplication.class))).thenReturn(application);
        doNothing().when(notificationService).notifyAdminNewApplication(any(), anyLong());

        // Act
        PolicyApplication result = policyApplicationService.submitForReview(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ApplicationStatus.SUBMITTED, result.getStatus());
        verify(applicationRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUnassignedApplications_Success() {
        // Arrange
        List<PolicyApplication> unassigned = Arrays.asList(application);
        when(applicationRepository.findByAssignedUnderwriterIsNull())
                .thenReturn(unassigned);

        // Act
        List<PolicyApplication> results = policyApplicationService.getUnassignedApplications();

        // Assert
        assertEquals(1, results.size());
        verify(applicationRepository, times(1)).findByAssignedUnderwriterIsNull();
    }

    @Test
    void testGetApplicationsAssignedToUnderwriter_Success() {
        // Arrange
        User underwriter = new User();
        underwriter.setId(2L);

        List<PolicyApplication> assigned = Arrays.asList(application);
        when(userRepository.findById(2L)).thenReturn(Optional.of(underwriter));
        when(applicationRepository.findByAssignedUnderwriter_Id(2L)).thenReturn(assigned);

        // Act
        List<PolicyApplication> results = policyApplicationService.getApplicationsAssignedToUnderwriter(2L);

        // Assert
        assertEquals(1, results.size());
        verify(applicationRepository, times(1)).findByAssignedUnderwriter_Id(2L);
    }

    @Test
    void testDeleteApplication_Success() {
        // Arrange
        application.setStatus(ApplicationStatus.DRAFT);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        doNothing().when(applicationRepository).delete(any(PolicyApplication.class));

        // Act
        policyApplicationService.deleteApplication(1L);

        // Assert
        verify(applicationRepository, times(1)).findById(1L);
        verify(applicationRepository, times(1)).delete(any(PolicyApplication.class));
    }

    @Test
    void testDeleteApplication_NotDraft() {
        // Arrange
        application.setStatus(ApplicationStatus.APPROVED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        // Act & Assert
        assertThrows(InvalidStatusTransitionException.class,
                () -> policyApplicationService.deleteApplication(1L));
    }
}

