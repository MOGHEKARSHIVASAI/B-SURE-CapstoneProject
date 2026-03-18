package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.dto.PolicyApplicationDTO;
import org.hartford.binsure.entity.PolicyApplication;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hartford.binsure.service.PolicyApplicationService;
import org.hartford.binsure.service.UnderwriterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.dto.response.PolicyApplicationResponse;
import org.hartford.binsure.mapper.EntityMapper;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "5. Policy Applications", description = "Policy application lifecycle management")
@SecurityRequirement(name = "Bearer Authentication")
public class PolicyApplicationController {

        @Autowired
        private PolicyApplicationService applicationService;
        @Autowired
        private UnderwriterService underwriterService;
        @Autowired
        private SecurityUtils securityUtils;
        @Autowired
        private EntityMapper entityMapper;

        // ─── CUSTOMER APIs
        // ────────────────────────────────────────────────────────────

        // POST /api/v1/applications
        // CUSTOMER creates a new DRAFT application
        @PostMapping
        @PreAuthorize("hasRole('CUSTOMER')")
        @Operation(summary = "Create draft application (CUSTOMER)", description = "Customer creates a new policy application in DRAFT status.")
        public ResponseEntity<PolicyApplicationResponse> submitApplication(
                        @Valid @RequestBody PolicyApplicationDTO request) {
                Long customerId = securityUtils.getCurrentUserId();
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(entityMapper.toDto(applicationService.submitApplication(request, customerId)));
        }

        // GET /api/v1/applications
        // ADMIN/UNDERWRITER gets all, CUSTOMER gets their own
        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER', 'CUSTOMER')")
        @Operation(summary = "Get applications", description = "Returns all applications if ADMIN/UW, or just owned applications if CUSTOMER.")
        public ResponseEntity<List<PolicyApplicationResponse>> getApplications() {
                if (securityUtils.hasRole("CUSTOMER")) {
                        Long customerId = securityUtils.getCurrentUserId();
                        return ResponseEntity.ok(applicationService.getApplicationsByCustomer(customerId).stream()
                                        .map(entityMapper::toDto).collect(Collectors.toList()));
                }
                return ResponseEntity.ok(applicationService.getAllApplications().stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // PUT /api/v1/applications/{id}
        // CUSTOMER updates a DRAFT application
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('CUSTOMER')")
        @Operation(summary = "Update draft application (CUSTOMER)", description = "Customer updates their application. Only DRAFT applications can be modified.")
        public ResponseEntity<PolicyApplicationResponse> updateApplication(
                        @PathVariable("id") Long id,
                        @Valid @RequestBody PolicyApplicationDTO request) {
                return ResponseEntity.ok(entityMapper.toDto(applicationService.updateApplication(id, request)));
        }

        // POST /api/v1/applications/{id}/submit
        // CUSTOMER submits a DRAFT application for review → status becomes SUBMITTED
        @PostMapping("/{id}/submit")
        @PreAuthorize("hasRole('CUSTOMER')")
        @Operation(summary = "Submit application for review (CUSTOMER)", description = "Moves application from DRAFT to SUBMITTED. Admin is notified to assign an underwriter.")
        public ResponseEntity<PolicyApplicationResponse> submitForReview(@PathVariable("id") Long id) {
                return ResponseEntity.ok(entityMapper.toDto(applicationService.submitForReview(id)));
        }

        // DELETE /api/v1/applications/{id}
        // CUSTOMER deletes a DRAFT application
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('CUSTOMER')")
        @Operation(summary = "Delete draft application (CUSTOMER)", description = "Customer deletes their application. Only DRAFT applications can be deleted.")
        public ResponseEntity<Map<String, String>> deleteApplication(@PathVariable("id") Long id) {
                applicationService.deleteApplication(id);
                return ResponseEntity.ok(Map.of("message", "Application deleted successfully. ID: " + id));
        }

        // POST /api/v1/applications/{id}/accept
        // CUSTOMER accepts the APPROVED underwriting decision → CUSTOMER_ACCEPTED
        @PostMapping("/{id}/accept")
        @PreAuthorize("hasRole('CUSTOMER')")
        @Operation(summary = "Accept underwriting decision (CUSTOMER)", description = "Customer accepts the APPROVED decision. Status → CUSTOMER_ACCEPTED. Admin is notified to issue policy.")
        public ResponseEntity<Map<String, String>> acceptDecision(
                        @PathVariable("id") Long id) {
                Long customerId = securityUtils.getCurrentUserId();
                underwriterService.customerAcceptDecision(id, customerId);
                return ResponseEntity.ok(Map.of(
                                "message", "Decision accepted. Admin will now issue your policy.",
                                "applicationId", String.valueOf(id),
                                "newStatus", "CUSTOMER_ACCEPTED"));
        }

        // POST /api/v1/applications/{id}/reject
        // CUSTOMER rejects the APPROVED underwriting decision → CUSTOMER_REJECTED
        @PostMapping("/{id}/reject")
        @PreAuthorize("hasRole('CUSTOMER')")
        @Operation(summary = "Reject underwriting decision (CUSTOMER)", description = "Customer rejects the APPROVED decision. Status → CUSTOMER_REJECTED. Admin is notified.")
        public ResponseEntity<Map<String, String>> rejectDecision(
                        @PathVariable("id") Long id) {
                Long customerId = securityUtils.getCurrentUserId();
                underwriterService.customerRejectDecision(id, customerId);
                return ResponseEntity.ok(Map.of(
                                "message", "Decision rejected. Admin has been notified.",
                                "applicationId", String.valueOf(id),
                                "newStatus", "CUSTOMER_REJECTED"));
        }

        // ─── ADMIN APIs
        // ───────────────────────────────────────────────────────────────

        // ADMIN/UNDERWRITER gets all applications (DEPRECATED: merged into base GET)
        // @GetMapping
        // @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
        // @Operation(summary = "Get all applications (ADMIN/UNDERWRITER)")
        // public ResponseEntity<List<PolicyApplicationResponse>> getAllApplications() {
        //         return ResponseEntity.ok(applicationService.getAllApplications().stream()
        //                         .map(entityMapper::toDto).collect(Collectors.toList()));
        // }

        // GET /api/v1/applications/{id}
        // ADMIN/UNDERWRITER gets one application by ID
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
        @Operation(summary = "Get application by ID (ADMIN/UNDERWRITER)", description = "Fetch a specific policy application by its ID.")
        public ResponseEntity<PolicyApplicationResponse> getApplicationById(@PathVariable("id") Long id) {
                return ResponseEntity.ok(entityMapper.toDto(applicationService.getApplicationById(id)));
        }

        // GET /api/v1/applications/status/{status}
        // ADMIN/UNDERWRITER filters by status
        @GetMapping("/status/{status}")
        @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
        @Operation(summary = "Get applications by status (ADMIN/UNDERWRITER)", description = "Filter applications by status: DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CUSTOMER_ACCEPTED, CUSTOMER_REJECTED, POLICY_ISSUED")
        public ResponseEntity<List<PolicyApplicationResponse>> getApplicationsByStatus(
                        @PathVariable("status") ApplicationStatus status) {
                return ResponseEntity.ok(applicationService.getApplicationsByStatus(status).stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // GET /api/v1/applications/unassigned
        // ADMIN gets applications not yet assigned to any underwriter
        @GetMapping("/unassigned")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get unassigned applications (ADMIN)", description = "Returns all SUBMITTED applications that have no underwriter assigned yet.")
        public ResponseEntity<List<PolicyApplicationResponse>> getUnassignedApplications() {
                return ResponseEntity.ok(applicationService.getUnassignedApplications().stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // PUT /api/v1/applications/{id}/assign/{underwriterId}
        // ADMIN assigns underwriter to a SUBMITTED application → UNDER_REVIEW
        @PutMapping("/{id}/assign/{underwriterId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Assign underwriter (ADMIN)", description = "Admin assigns an underwriter to a SUBMITTED application. Status → UNDER_REVIEW. Underwriter is notified.")
        public ResponseEntity<PolicyApplicationResponse> assignUnderwriter(
                        @PathVariable("id") Long id,
                        @PathVariable("underwriterId") Long underwriterId) {
                return ResponseEntity.ok(entityMapper.toDto(applicationService.assignUnderwriter(id, underwriterId)));
        }

        // ─── UNDERWRITER APIs
        // ─────────────────────────────────────────────────────────

        // GET /api/v1/applications/assigned
        // UNDERWRITER gets all applications assigned to them
        @GetMapping("/assigned")
        @PreAuthorize("hasRole('UNDERWRITER')")
        @Operation(summary = "Get assigned applications (UNDERWRITER)", description = "Underwriter retrieves all applications assigned to them.")
        public ResponseEntity<List<PolicyApplicationResponse>> getApplicationsByUnderwriter() {
                Long underwriterId = securityUtils.getCurrentUserId();
                return ResponseEntity.ok(applicationService.getApplicationsAssignedToUnderwriter(underwriterId).stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }
}
