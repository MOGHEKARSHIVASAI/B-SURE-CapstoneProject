package org.hartford.binsure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.binsure.dto.PolicyApplicationDTO;
import org.hartford.binsure.dto.response.PolicyApplicationResponse;
import org.hartford.binsure.mapper.EntityMapper;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.service.PolicyApplicationService;
import org.hartford.binsure.service.UnderwriterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyApplicationController.class)
class PolicyApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyApplicationService applicationService;

    @MockBean
    private UnderwriterService underwriterService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private EntityMapper entityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private PolicyApplicationDTO applicationDTO;
    private PolicyApplicationResponse applicationResponse;

    @BeforeEach
    void setUp() {
        applicationDTO = new PolicyApplicationDTO();
        applicationDTO.setBusinessId(1L);
        applicationDTO.setProductId(1L);
        applicationDTO.setCoverageAmount(new BigDecimal("100000"));
        applicationDTO.setCoverageStartDate(LocalDate.now().plusDays(1));
        applicationDTO.setCoverageEndDate(LocalDate.now().plusYears(1));

        applicationResponse = new PolicyApplicationResponse();
        applicationResponse.setId(1L);
        applicationResponse.setStatus("DRAFT");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testSubmitApplication_Success() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(applicationService.submitApplication(any(), anyLong()))
                .thenReturn(new org.hartford.binsure.entity.PolicyApplication());
        when(entityMapper.toDto(any())).thenReturn(applicationResponse);

        mockMvc.perform(post("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(applicationService, times(1)).submitApplication(any(), anyLong());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetApplications_AsCustomer() throws Exception {
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(applicationService.getApplicationsByCustomer(1L))
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.PolicyApplication()));
        when(entityMapper.toDto(any())).thenReturn(applicationResponse);

        mockMvc.perform(get("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(applicationService, times(1)).getApplicationsByCustomer(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testSubmitForReview_Success() throws Exception {
        when(applicationService.submitForReview(1L))
                .thenReturn(new org.hartford.binsure.entity.PolicyApplication());
        when(entityMapper.toDto(any())).thenReturn(applicationResponse);

        mockMvc.perform(post("/api/v1/applications/1/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).submitForReview(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testDeleteApplication_Success() throws Exception {
        doNothing().when(applicationService).deleteApplication(1L);

        mockMvc.perform(delete("/api/v1/applications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(applicationService, times(1)).deleteApplication(1L);
    }

    @Test
    void testSubmitApplication_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationDTO))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}

