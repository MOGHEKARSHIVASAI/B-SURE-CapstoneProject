package org.hartford.binsure.service;

import org.hartford.binsure.dto.BusinessUpdateRequest;
import org.hartford.binsure.entity.Business;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.dto.CreateBusinessRequest;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.BusinessRepository;
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
 * Unit tests for BusinessService.
 * Tests business profile CRUD operations and retrieval.
 */
@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BusinessService businessService;

    private Business business;
    private User user;
    private CreateBusinessRequest createRequest;

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
        business.setIndustryType("Technology");
        business.setAnnualRevenue(new BigDecimal("1000000"));
        business.setNumEmployees(50);
        business.setAddressLine1("123 Street");
        business.setCity("Mumbai");
        business.setState("Maharashtra");
        business.setPostalCode("400001");
        business.setCountry("India");

        // Setup create request
        createRequest = new CreateBusinessRequest();
        createRequest.setCompanyName("Test Company");
        createRequest.setIndustryType("Technology");
        createRequest.setAnnualRevenue(new BigDecimal("1000000"));
        createRequest.setNumEmployees(50);
        createRequest.setAddressLine1("123 Street");
        createRequest.setCity("Mumbai");
        createRequest.setState("Maharashtra");
        createRequest.setPostalCode("400001");
        createRequest.setCountry("India");
    }

    @Test
    void testGetBusinessById_Success() {
        // Arrange
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        // Act
        Business result = businessService.getBusinessById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Company", result.getCompanyName());
        assertEquals("Technology", result.getIndustryType());
        verify(businessRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBusinessById_NotFound() {
        // Arrange
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> businessService.getBusinessById(999L));
    }

    @Test
    void testGetBusinessesByUserId_Success() {
        // Arrange
        List<Business> businesses = Arrays.asList(business);
        when(businessRepository.findByUser_Id(1L)).thenReturn(businesses);

        // Act
        List<Business> results = businessService.getBusinessesByUserId(1L);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Test Company", results.get(0).getCompanyName());
        verify(businessRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testGetBusinessesByUserId_Null() {
        // Arrange
        List<Business> businesses = Arrays.asList(business);
        when(businessRepository.findAll()).thenReturn(businesses);

        // Act
        List<Business> results = businessService.getBusinessesByUserId(null);

        // Assert
        assertEquals(1, results.size());
        verify(businessRepository, times(1)).findAll();
    }

    @Test
    void testGetAllBusinesses_Success() {
        // Arrange
        List<Business> businesses = Arrays.asList(business);
        when(businessRepository.findAll()).thenReturn(businesses);

        // Act
        List<Business> results = businessService.getAllBusinesses();

        // Assert
        assertEquals(1, results.size());
        verify(businessRepository, times(1)).findAll();
    }

    @Test
    void testAddBusinessProfile_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        // Act
        Business result = businessService.addBusinessProfile(1L, createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Test Company", result.getCompanyName());
        assertEquals("Technology", result.getIndustryType());
        assertEquals(50, result.getNumEmployees());
        verify(userRepository, times(1)).findById(1L);
        verify(businessRepository, times(1)).save(any(Business.class));
    }

    @Test
    void testAddBusinessProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> businessService.addBusinessProfile(999L, createRequest));
    }

    @Test
    void testUpdateBusinessProfile_Success() {
        // Arrange
        BusinessUpdateRequest updateRequest = new BusinessUpdateRequest();
        updateRequest.setCompanyName("Updated Company");
        updateRequest.setIndustryType("Finance");
        updateRequest.setAnnualRevenue(new BigDecimal("2000000"));
        updateRequest.setNumEmployees(100);
        updateRequest.setCity("Bangalore");

        Business updatedBusiness = new Business();
        updatedBusiness.setId(1L);
        updatedBusiness.setUser(user);
        updatedBusiness.setCompanyName("Updated Company");
        updatedBusiness.setIndustryType("Finance");
        updatedBusiness.setAnnualRevenue(new BigDecimal("2000000"));
        updatedBusiness.setNumEmployees(100);
        updatedBusiness.setCity("Bangalore");

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenReturn(updatedBusiness);

        // Act
        Business result = businessService.updateBusinessProfile(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Company", result.getCompanyName());
        assertEquals("Finance", result.getIndustryType());
        assertEquals(100, result.getNumEmployees());
        verify(businessRepository, times(1)).findById(1L);
        verify(businessRepository, times(1)).save(any(Business.class));
    }

    @Test
    void testUpdateBusinessProfile_PartialUpdate() {
        // Arrange
        BusinessUpdateRequest updateRequest = new BusinessUpdateRequest();
        updateRequest.setCompanyName("Updated Company");
        // Other fields left null - should not be updated

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        // Act
        Business result = businessService.updateBusinessProfile(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(businessRepository, times(1)).findById(1L);
        verify(businessRepository, times(1)).save(any(Business.class));
    }

    @Test
    void testUpdateBusinessProfile_NotFound() {
        // Arrange
        BusinessUpdateRequest updateRequest = new BusinessUpdateRequest();
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> businessService.updateBusinessProfile(999L, updateRequest));
    }

    @Test
    void testDeleteBusiness_Success() {
        // Arrange
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        doNothing().when(businessRepository).delete(any(Business.class));

        // Act
        businessService.deleteBusiness(1L);

        // Assert
        verify(businessRepository, times(1)).findById(1L);
        verify(businessRepository, times(1)).delete(any(Business.class));
    }

    @Test
    void testDeleteBusiness_NotFound() {
        // Arrange
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> businessService.deleteBusiness(999L));
    }
}

