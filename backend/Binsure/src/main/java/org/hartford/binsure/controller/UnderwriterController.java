package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.dto.UnderwriterDecisionRequest;
import org.hartford.binsure.entity.PolicyApplication;
import org.hartford.binsure.entity.UnderwriterDecision;
import org.hartford.binsure.service.UnderwriterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.dto.response.PolicyApplicationResponse;
import org.hartford.binsure.dto.response.UnderwriterDecisionResponse;
import org.hartford.binsure.mapper.EntityMapper;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/underwriting")
@Tag(name = "6. Underwriting", description = "Underwriting review and decision management")
@SecurityRequirement(name = "Bearer Authentication")
public class UnderwriterController {

        @Autowired
        private UnderwriterService underwriterService;
        @Autowired
        private EntityMapper entityMapper;
        @Autowired
        private SecurityUtils securityUtils;

        // ─── UNDERWRITER APIs
        // ─────────────────────────────────────────────────────────

        // GET /api/v1/underwriting/queue
        // UNDERWRITER gets their UNDER_REVIEW queue
        @GetMapping("/queue")
        @PreAuthorize("hasRole('UNDERWRITER')")
        @Operation(summary = "Get review queue (UNDERWRITER)", description = "Returns all UNDER_REVIEW applications assigned to the current underwriter.")
        public ResponseEntity<List<PolicyApplicationResponse>> getUnderwritingQueue() {
                Long underwriterId = securityUtils.getCurrentUserId();
                return ResponseEntity.ok(underwriterService.getUnderwritingQueue(underwriterId).stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // GET /api/v1/underwriting/application/{applicationId}/risk-score
        // UNDERWRITER calculates auto risk score for an application
        @GetMapping("/application/{applicationId}/risk-score")
        @PreAuthorize("hasRole('UNDERWRITER')")
        @Operation(summary = "Calculate risk score (UNDERWRITER)", description = "Auto-calculates a risk score (0-100) based on coverage amount, employee count and industry type.")
        public ResponseEntity<Map<String, Integer>> calculateRiskScore(
                        @PathVariable("applicationId") Long applicationId) {
                return ResponseEntity.ok(Map.of("riskScore", underwriterService.calculateRiskScore(applicationId)));
        }

        // POST /api/v1/underwriting/application/{applicationId}/decision
        // UNDERWRITER submits APPROVED / REJECTED / REFER_TO_SENIOR decision
        @PostMapping("/application/{applicationId}/decision")
        @PreAuthorize("hasRole('UNDERWRITER')")
        @Operation(summary = "Submit decision (UNDERWRITER)", description = "Underwriter submits approve/reject/refer decision with risk score and premium adjustment.")
        public ResponseEntity<UnderwriterDecisionResponse> submitDecision(
                        @PathVariable("applicationId") Long applicationId,
                        @Valid @RequestBody UnderwriterDecisionRequest request) {
                Long underwriterId = securityUtils.getCurrentUserId();
                return ResponseEntity.ok(entityMapper
                                .toDto(underwriterService.submitDecision(applicationId, request, underwriterId)));
        }

        // ─── ADMIN/UNDERWRITER — Decision Lookup APIs
        // ─────────────────────────────────

        // GET /api/v1/underwriting/decisions
        // ADMIN/UNDERWRITER gets all decisions
        @GetMapping("/decisions")
        @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
        @Operation(summary = "Get all decisions (ADMIN/UNDERWRITER)", description = "Returns all underwriting decisions recorded in the system.")
        public ResponseEntity<List<UnderwriterDecisionResponse>> getAllDecisions() {
                return ResponseEntity.ok(underwriterService.getAllDecisions().stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // GET /api/v1/underwriting/decisions/{decisionId}
        // ADMIN/UNDERWRITER gets one decision by ID
        @GetMapping("/decisions/{decisionId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
        @Operation(summary = "Get decision by ID (ADMIN/UNDERWRITER)", description = "Fetch a specific underwriting decision record.")
        public ResponseEntity<UnderwriterDecisionResponse> getDecisionById(
                        @PathVariable("decisionId") Long decisionId) {
                return ResponseEntity.ok(entityMapper.toDto(underwriterService.getDecisionById(decisionId)));
        }

        // GET /api/v1/underwriting/application/{applicationId}/decisions
        // ADMIN/UNDERWRITER gets all decisions for a specific application
        @GetMapping("/application/{applicationId}/decisions")
        @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER', 'CUSTOMER')")
        @Operation(summary = "Get decisions by application (ADMIN/UNDERWRITER/CUSTOMER)", description = "Returns all underwriting decisions made for a specific policy application.")
        public ResponseEntity<List<UnderwriterDecisionResponse>> getDecisionsByApplication(
                        @PathVariable("applicationId") Long applicationId) {
                return ResponseEntity.ok(underwriterService.getDecisionsByApplication(applicationId).stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // GET /api/v1/underwriting/application/{applicationId}/latest-decision
        // ADMIN/UNDERWRITER gets the latest decision for an application
        @GetMapping("/application/{applicationId}/latest-decision")
        @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER', 'CUSTOMER')")
        @Operation(summary = "Get latest decision (ADMIN/UNDERWRITER/CUSTOMER)", description = "Returns the most recent underwriting decision for a policy application.")
        public ResponseEntity<UnderwriterDecisionResponse> getLatestDecision(
                        @PathVariable("applicationId") Long applicationId) {
                return ResponseEntity.ok(
                                entityMapper.toDto(underwriterService.getLatestDecisionForApplication(applicationId)));
        }
}
