package org.hartford.binsure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.binsure.dto.CreateBusinessRequest;
import org.hartford.binsure.dto.BusinessUpdateRequest;
import org.hartford.binsure.dto.response.BusinessResponse;
import org.hartford.binsure.mapper.EntityMapper;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.service.BusinessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc Controller tests for BusinessController.
 * Tests business profile management endpoints.
 */
@WebMvcTest(BusinessController.class)
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessService businessService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private EntityMapper entityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private BusinessResponse businessResponse;
    private CreateBusinessRequest createRequest;
    private BusinessUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Setup business response
        businessResponse = new BusinessResponse();
        businessResponse.setId(1L);
        businessResponse.setCompanyName("Test Company");
        businessResponse.setIndustryType("Technology");
        businessResponse.setAnnualRevenue(new BigDecimal("1000000"));
        businessResponse.setNumEmployees(50);
        businessResponse.setCity("Mumbai");
        businessResponse.setState("Maharashtra");

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

        // Setup update request
        updateRequest = new BusinessUpdateRequest();
        updateRequest.setCompanyName("Updated Company");
        updateRequest.setCity("Bangalore");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetBusinesses_AsCustomer() throws Exception {
        // Arrange
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(businessService.getBusinessesByUserId(1L))
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.Business()));
        when(entityMapper.toDto(any())).thenReturn(businessResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/businesses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(businessService, times(1)).getBusinessesByUserId(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBusinesses_AsAdmin() throws Exception {
        // Arrange
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(false);
        when(businessService.getAllBusinesses())
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.Business()));
        when(entityMapper.toDto(any())).thenReturn(businessResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/businesses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(businessService, times(1)).getAllBusinesses();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBusinessById_Success() throws Exception {
        // Arrange
        when(businessService.getBusinessById(1L))
                .thenReturn(new org.hartford.binsure.entity.Business());
        when(entityMapper.toDto(any())).thenReturn(businessResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/businesses/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyName").value("Test Company"));

        verify(businessService, times(1)).getBusinessById(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testAddBusiness_Success() throws Exception {
        // Arrange
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(businessService.addBusinessProfile(anyLong(), any(CreateBusinessRequest.class)))
                .thenReturn(new org.hartford.binsure.entity.Business());
        when(entityMapper.toDto(any())).thenReturn(businessResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("Test Company"));

        verify(businessService, times(1)).addBusinessProfile(anyLong(), any(CreateBusinessRequest.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testAddBusiness_MissingFields() throws Exception {
        // Arrange
        CreateBusinessRequest invalidRequest = new CreateBusinessRequest();
        invalidRequest.setCompanyName("Test"); // Missing other required fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateBusiness_Success() throws Exception {
        // Arrange
        BusinessResponse updatedResponse = new BusinessResponse();
        updatedResponse.setId(1L);
        updatedResponse.setCompanyName("Updated Company");
        updatedResponse.setCity("Bangalore");

        when(businessService.updateBusinessProfile(anyLong(), any(BusinessUpdateRequest.class)))
                .thenReturn(new org.hartford.binsure.entity.Business());
        when(entityMapper.toDto(any())).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/businesses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Updated Company"))
                .andExpect(jsonPath("$.city").value("Bangalore"));

        verify(businessService, times(1)).updateBusinessProfile(anyLong(), any(BusinessUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateBusiness_AsCustomer() throws Exception {
        // Arrange
        when(businessService.updateBusinessProfile(anyLong(), any(BusinessUpdateRequest.class)))
                .thenReturn(new org.hartford.binsure.entity.Business());
        when(entityMapper.toDto(any())).thenReturn(businessResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/businesses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(businessService, times(1)).updateBusinessProfile(anyLong(), any(BusinessUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteBusiness_Success() throws Exception {
        // Arrange
        doNothing().when(businessService).deleteBusiness(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/businesses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(businessService, times(1)).deleteBusiness(1L);
    }

    @Test
    void testGetBusinesses_Unauthorized() throws Exception {
        // Act & Assert - No authentication
        mockMvc.perform(get("/api/v1/businesses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddBusiness_Unauthorized() throws Exception {
        // Act & Assert - No authentication
        mockMvc.perform(post("/api/v1/businesses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}

