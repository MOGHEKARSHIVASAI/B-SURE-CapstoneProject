package org.hartford.binsure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.binsure.dto.ClaimRequest;
import org.hartford.binsure.dto.response.ClaimResponse;
import org.hartford.binsure.enums.ClaimStatus;
import org.hartford.binsure.mapper.EntityMapper;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.service.ClaimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockbean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc Controller tests for ClaimController.
 * Tests claim filing, management, and lifecycle endpoints.
 */
@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimService claimService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private EntityMapper entityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private ClaimResponse claimResponse;
    private ClaimRequest claimRequest;

    @BeforeEach
    void setUp() {
        // Setup claim request
        claimRequest = new ClaimRequest();
        claimRequest.setPolicyId(1L);
        claimRequest.setIncidentDate(LocalDate.now().minusDays(5));
        claimRequest.setClaimedAmount(new BigDecimal("50000"));
        claimRequest.setIncidentDescription("Property damage claim");

        // Setup claim response
        claimResponse = new ClaimResponse();
        claimResponse.setId(1L);
        claimResponse.setClaimNumber("CLM-123456789");
        claimResponse.setStatus(ClaimStatus.DRAFT);
        claimResponse.setClaimedAmount(new BigDecimal("50000"));
        claimResponse.setIncidentDescription("Property damage claim");
        claimResponse.setIncidentDate(LocalDate.now().minusDays(5));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testFileClaim_Success() throws Exception {
        // Arrange
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(claimService.fileNewClaim(any(ClaimRequest.class), anyLong()))
                .thenReturn(new org.hartford.binsure.entity.Claim());
        when(entityMapper.toDto(any())).thenReturn(claimResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(claimRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimNumber").exists())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(claimService, times(1)).fileNewClaim(any(ClaimRequest.class), anyLong());
        verify(securityUtils, times(1)).getCurrentUserId();
    }

    @Test
    void testFileClaim_Unauthorized() throws Exception {
        // Act & Assert - No authentication
        mockMvc.perform(post("/api/v1/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(claimRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetClaims_AsCustomer() throws Exception {
        // Arrange
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(claimService.getClaimsByCustomer(1L))
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.Claim()));
        when(entityMapper.toDto(any())).thenReturn(claimResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/claims")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(claimService, times(1)).getClaimsByCustomer(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetClaims_AsAdmin() throws Exception {
        // Arrange
        when(securityUtils.hasRole("CUSTOMER")).thenReturn(false);
        when(claimService.getAllClaims())
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.Claim()));
        when(entityMapper.toDto(any())).thenReturn(claimResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/claims")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(claimService, times(1)).getAllClaims();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateClaim_Success() throws Exception {
        // Arrange
        when(claimService.updateClaim(anyLong(), any(ClaimRequest.class)))
                .thenReturn(new org.hartford.binsure.entity.Claim());
        when(entityMapper.toDto(any())).thenReturn(claimResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/claims/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(claimRequest))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(claimService, times(1)).updateClaim(anyLong(), any(ClaimRequest.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testAppealClaim_Success() throws Exception {
        // Arrange
        ClaimResponse appealedResponse = new ClaimResponse();
        appealedResponse.setStatus(ClaimStatus.SUBMITTED); // Appealed claim becomes SUBMITTED

        when(claimService.appealClaim(1L))
                .thenReturn(new org.hartford.binsure.entity.Claim());
        when(entityMapper.toDto(any())).thenReturn(appealedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/claims/1/appeal")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(claimService, times(1)).appealClaim(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testDeleteClaim_Success() throws Exception {
        // Arrange
        doNothing().when(claimService).deleteClaim(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/claims/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(claimService, times(1)).deleteClaim(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSubmitClaim_AsAdmin() throws Exception {
        // Arrange
        ClaimResponse submittedResponse = new ClaimResponse();
        submittedResponse.setStatus(ClaimStatus.SUBMITTED);

        when(claimService.submitClaim(1L))
                .thenReturn(new org.hartford.binsure.entity.Claim());
        when(entityMapper.toDto(any())).thenReturn(submittedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/claims/1/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(claimService, times(1)).submitClaim(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testApproveClaim_Success() throws Exception {
        // Arrange
        ClaimResponse approvedResponse = new ClaimResponse();
        approvedResponse.setStatus(ClaimStatus.APPROVED);
        approvedResponse.setApprovedAmount(new BigDecimal("45000"));

        when(claimService.approveClaim(anyLong(), any(BigDecimal.class)))
                .thenReturn(new org.hartford.binsure.entity.Claim());
        when(entityMapper.toDto(any())).thenReturn(approvedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/claims/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .param("approvedAmount", "45000")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(claimService, times(1)).approveClaim(anyLong(), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRejectClaim_Success() throws Exception {
        // Arrange
        ClaimResponse rejectedResponse = new ClaimResponse();
        rejectedResponse.setStatus(ClaimStatus.REJECTED);

        when(claimService.rejectClaim(anyLong(), anyString()))
                .thenReturn(new org.hartford.binsure.entity.Claim());
        when(entityMapper.toDto(any())).thenReturn(rejectedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/claims/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .param("rejectionReason", "Insufficient documentation")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(claimService, times(1)).rejectClaim(anyLong(), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSettleClaim_Success() throws Exception {
        // Arrange
        ClaimResponse settledResponse = new ClaimResponse();
        settledResponse.setStatus(ClaimStatus.SETTLED);

        when(claimService.settleClaim(anyLong(), any(BigDecimal.class)))
                .thenReturn(new org.hartford.binsure.entity.Claim());
        when(entityMapper.toDto(any())).thenReturn(settledResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/claims/1/settle")
                .contentType(MediaType.APPLICATION_JSON)
                .param("settledAmount", "45000")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(claimService, times(1)).settleClaim(anyLong(), any(BigDecimal.class));
    }
}

