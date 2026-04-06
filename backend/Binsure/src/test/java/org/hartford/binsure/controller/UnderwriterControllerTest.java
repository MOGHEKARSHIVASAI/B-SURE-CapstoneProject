package org.hartford.binsure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.binsure.dto.UnderwriterDecisionRequest;
import org.hartford.binsure.dto.response.PolicyApplicationResponse;
import org.hartford.binsure.dto.response.UnderwriterDecisionResponse;
import org.hartford.binsure.enums.DecisionType;
import org.hartford.binsure.mapper.EntityMapper;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.service.UnderwriterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockbean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UnderwriterController.class)
class UnderwriterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnderwriterService underwriterService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private EntityMapper entityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private UnderwriterDecisionRequest decisionRequest;
    private PolicyApplicationResponse appResponse;
    private UnderwriterDecisionResponse decisionResponse;

    @BeforeEach
    void setUp() {
        decisionRequest = new UnderwriterDecisionRequest();
        decisionRequest.setDecision(DecisionType.APPROVED);
        decisionRequest.setRiskScore(75);
        decisionRequest.setPremiumAdjustmentPct(new BigDecimal("10"));
        decisionRequest.setComments("Approved");

        appResponse = new PolicyApplicationResponse();
        appResponse.setId(1L);
        appResponse.setStatus("UNDER_REVIEW");

        decisionResponse = new UnderwriterDecisionResponse();
        decisionResponse.setId(1L);
        decisionResponse.setDecision(DecisionType.APPROVED);
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void testGetUnderwritingQueue_Success() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(underwriterService.getUnderwritingQueue(1L))
                .thenReturn(Arrays.asList(new org.hartford.binsure.entity.PolicyApplication()));
        when(entityMapper.toDto(any())).thenReturn(appResponse);

        mockMvc.perform(get("/api/v1/underwriting/queue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(underwriterService, times(1)).getUnderwritingQueue(1L);
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void testCalculateRiskScore_Success() throws Exception {
        when(underwriterService.calculateRiskScore(1L)).thenReturn(75);

        mockMvc.perform(get("/api/v1/underwriting/application/1/risk-score")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskScore").value(75));

        verify(underwriterService, times(1)).calculateRiskScore(1L);
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void testSubmitDecision_Success() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(underwriterService.submitDecision(anyLong(), any(), anyLong()))
                .thenReturn(new org.hartford.binsure.entity.UnderwriterDecision());
        when(entityMapper.toDto(any())).thenReturn(decisionResponse);

        mockMvc.perform(post("/api/v1/underwriting/application/1/decision")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decisionRequest))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(underwriterService, times(1)).submitDecision(anyLong(), any(), anyLong());
    }

    @Test
    void testGetUnderwritingQueue_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/underwriting/queue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}

