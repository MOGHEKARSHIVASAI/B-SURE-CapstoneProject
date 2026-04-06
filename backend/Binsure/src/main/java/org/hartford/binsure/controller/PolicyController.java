package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.hartford.binsure.enums.PolicyStatus;
import org.hartford.binsure.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.dto.response.PolicyResponse;
import org.hartford.binsure.mapper.EntityMapper;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/policies")
@Tag(name = "7. Policies", description = "Policy issuance and lifecycle management")
@SecurityRequirement(name = "Bearer Authentication")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private EntityMapper entityMapper;

    // ─── ADMIN APIs
    // ───────────────────────────────────────────────────────────────

    /**
     * ════════════════════════════════════════════════════════════════════════════
     * POLICY AUTO-ISSUANCE:
     * ════════════════════════════════════════════════════════════════════════════
     *
     * Policies are AUTOMATICALLY created when customer makes payment.
     *
     * FLOW:
     * 1. POST /api/v1/payments/customer/{customerId} → Payment recorded
     * 2. PremiumPaymentService automatically creates Policy in ACTIVE status
     * 3. Application status updated to POLICY_ISSUED
     * 4. Notifications sent to customer and admin
     *
     * NO MANUAL POLICY ISSUANCE ENDPOINT EXISTS.
     * Use below endpoints only for policy management (view, suspend, cancel, etc.)
     * ════════════════════════════════════════════════════════════════════════════
     */

    // GET /api/v1/policies
    // ADMIN/CLAIMS_OFFICER gets all, CUSTOMER gets their own
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIMS_OFFICER', 'CUSTOMER', 'UNDERWRITER')")
    @Operation(summary = "Get policies", description = "Returns all policies if ADMIN/OFFICER/UW, or just owned policies if CUSTOMER.")
    public ResponseEntity<List<PolicyResponse>> getPolicies() {
        if (securityUtils.hasRole("CUSTOMER")) {
            Long customerId = securityUtils.getCurrentUserId();
            return ResponseEntity.ok(policyService.getPoliciesByCustomer(customerId).stream()
                    .map(entityMapper::toDto).collect(Collectors.toList()));
        }
        return ResponseEntity.ok(policyService.getAllPolicies().stream()
                .map(entityMapper::toDto).collect(Collectors.toList()));
    }

    // GET /api/v1/policies/status/{status}
    // ADMIN filters by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get policies by status (ADMIN)", description = "Filter policies by status: ACTIVE, EXPIRED, CANCELLED, SUSPENDED")
    public ResponseEntity<List<PolicyResponse>> getPoliciesByStatus(@PathVariable("status") PolicyStatus status) {
        return ResponseEntity.ok(policyService.getPoliciesByStatus(status).stream()
                .map(entityMapper::toDto).collect(Collectors.toList()));
    }

    // GET /api/v1/policies/expiring-soon
    // ADMIN/UNDERWRITER — policies expiring in next N days
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    @Operation(summary = "Get expiring policies (ADMIN/UNDERWRITER)", description = "Returns ACTIVE policies expiring within the next N days. Default is 30 days.")
    public ResponseEntity<List<PolicyResponse>> getExpiringPolicies(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(policyService.getExpiringPolicies(days).stream()
                .map(entityMapper::toDto).collect(Collectors.toList()));
    }

    // PUT /api/v1/policies/{id}/suspend
    // ADMIN suspends an ACTIVE policy
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspend policy (ADMIN)", description = "Admin suspends an ACTIVE policy. Status → SUSPENDED.")
    public ResponseEntity<PolicyResponse> suspendPolicy(@PathVariable("id") Long id) {
        return ResponseEntity.ok(entityMapper.toDto(policyService.suspendPolicy(id)));
    }

    // PUT /api/v1/policies/{id}/reactivate
    // ADMIN reactivates a SUSPENDED policy
    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate policy (ADMIN)", description = "Admin reactivates a SUSPENDED policy. Status → ACTIVE.")
    public ResponseEntity<PolicyResponse> reactivatePolicy(@PathVariable("id") Long id) {
        return ResponseEntity.ok(entityMapper.toDto(policyService.reactivatePolicy(id)));
    }

    // POST /api/v1/policies/{id}/cancel
    // ADMIN/CUSTOMER cancels a policy
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Cancel policy (ADMIN/CUSTOMER)", description = "Cancels a policy. Body: { \"cancellationReason\": \"reason\" }")
    public ResponseEntity<PolicyResponse> cancelPolicy(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("cancellationReason");
        return ResponseEntity.ok(entityMapper.toDto(policyService.cancelPolicy(id, reason)));
    }

    // ─── ADMIN/UNDERWRITER/CUSTOMER — Lookup APIs
    // ─────────────────────────────────

    // GET /api/v1/policies/{id}
    // ADMIN/UNDERWRITER/CUSTOMER get policy by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER', 'CUSTOMER', 'CLAIMS_OFFICER')")
    @Operation(summary = "Get policy by ID", description = "Fetch a specific policy by ID. Customers can use this to view their policy details.")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(entityMapper.toDto(policyService.getPolicyById(id)));
    }

    // GET /api/v1/policies/number/{policyNumber}
    // ADMIN/UNDERWRITER/CUSTOMER look up by policy number
    @GetMapping("/number/{policyNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER', 'CUSTOMER')")
    @Operation(summary = "Get policy by number", description = "Look up a policy using its human-readable policy number (e.g. POL-1234567890).")
    public ResponseEntity<PolicyResponse> getPolicyByNumber(@PathVariable("policyNumber") String policyNumber) {
        return ResponseEntity.ok(entityMapper.toDto(policyService.getPolicyByNumber(policyNumber)));
    }

    // ─── CUSTOMER APIs
    // ────────────────────────────────────────────────────────────

    // GET /api/v1/policies/user (DEPRECATED: merged into base GET)
    // @GetMapping("/user")
    // @PreAuthorize("hasRole('CUSTOMER')")
    // @Operation(summary = "Get own policies (CUSTOMER)", description = "Customer retrieves all their own policies.")
    // public ResponseEntity<List<PolicyResponse>> getPoliciesByCustomer() {
    //     Long customerId = securityUtils.getCurrentUserId();
    //     return ResponseEntity.ok(policyService.getPoliciesByCustomer(customerId).stream()
    //             .map(entityMapper::toDto).collect(Collectors.toList()));
    // }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Get policies for a business")
    public ResponseEntity<List<PolicyResponse>> getPoliciesByBusiness(@PathVariable("businessId") Long businessId) {
        return ResponseEntity.ok(policyService.getPoliciesByBusiness(businessId).stream()
                .map(entityMapper::toDto).collect(Collectors.toList()));
    }

}
