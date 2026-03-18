package org.hartford.binsure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.binsure.dto.response.PolicyResponse;
import org.hartford.binsure.enums.PolicyStatus;
import org.hartford.binsure.mapper.EntityMapper;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private EntityMapper entityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private PolicyResponse policyResponse;

    @BeforeEach
    void setUp() {
        policyResponse = new PolicyResponse();
        policyResponse.setId(1L);
        policyResponse.setPolicyNumber("POL-123456");
        policyResponse.setStatus(PolicyStatus.ACTIVE);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetPolicies_AsCustomer() throws Exception {
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(policyService.getPoliciesByCustomer(1L))
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.Policy()));
        when(entityMapper.toDto(any())).thenReturn(policyResponse);

        mockMvc.perform(get("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(policyService, times(1)).getPoliciesByCustomer(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPoliciesByStatus_Success() throws Exception {
        when(policyService.getPoliciesByStatus(PolicyStatus.ACTIVE))
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.Policy()));
        when(entityMapper.toDto(any())).thenReturn(policyResponse);

        mockMvc.perform(get("/api/v1/policies/status/ACTIVE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(policyService, times(1)).getPoliciesByStatus(PolicyStatus.ACTIVE);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetExpiringPolicies_Success() throws Exception {
        when(policyService.getExpiringPolicies(30))
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.Policy()));
        when(entityMapper.toDto(any())).thenReturn(policyResponse);

        mockMvc.perform(get("/api/v1/policies/expiring-soon?days=30")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(policyService, times(1)).getExpiringPolicies(30);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSuspendPolicy_Success() throws Exception {
        when(policyService.suspendPolicy(1L))
                .thenReturn(new org.hartford.binsure.entity.Policy());
        when(entityMapper.toDto(any())).thenReturn(policyResponse);

        mockMvc.perform(put("/api/v1/policies/1/suspend")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(policyService, times(1)).suspendPolicy(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReactivatePolicy_Success() throws Exception {
        when(policyService.reactivatePolicy(1L))
                .thenReturn(new org.hartford.binsure.entity.Policy());
        when(entityMapper.toDto(any())).thenReturn(policyResponse);

        mockMvc.perform(put("/api/v1/policies/1/reactivate")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(policyService, times(1)).reactivatePolicy(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCancelPolicy_Success() throws Exception {
        when(policyService.cancelPolicy(anyLong(), any()))
                .thenReturn(new org.hartford.binsure.entity.Policy());
        when(entityMapper.toDto(any())).thenReturn(policyResponse);

        mockMvc.perform(put("/api/v1/policies/1/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .param("cancellationReason", "Customer request")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(policyService, times(1)).cancelPolicy(anyLong(), any());
    }
}

