package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.dto.ClaimRequest;
import org.hartford.binsure.enums.ClaimStatus;
import org.hartford.binsure.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.dto.response.ClaimResponse;
import org.hartford.binsure.mapper.EntityMapper;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "8. Claims", description = "Claims filing, investigation and settlement")
@SecurityRequirement(name = "Bearer Authentication")
public class ClaimController {

    @Autowired
    private ClaimService claimService;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private EntityMapper entityMapper;

    // ─── CUSTOMER APIs
    // ────────────────────────────────────────────────────────────

    // POST /api/v1/claims
    // CUSTOMER files a new claim against an active policy
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "File a claim (CUSTOMER)")
    public ResponseEntity<ClaimResponse> fileClaim(@Valid @RequestBody ClaimRequest request) {
        Long customerId = securityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entityMapper.toDto(claimService.fileNewClaim(request, customerId)));
    }

    // GET /api/v1/claims
    // ADMIN/CLAIMS_OFFICER gets all, CUSTOMER gets their own
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIMS_OFFICER', 'CUSTOMER')")
    @Operation(summary = "Get claims", description = "Returns all claims if ADMIN/OFFICER, or just owned claims if CUSTOMER.")
    public ResponseEntity<List<ClaimResponse>> getClaims() {
        if (securityUtils.hasRole("CUSTOMER")) {
            Long customerId = securityUtils.getCurrentUserId();
            return ResponseEntity.ok(claimService.getClaimsByCustomer(customerId).stream()
                    .map(entityMapper::toDto).collect(Collectors.toList()));
        }
        return ResponseEntity.ok(claimService.getAllClaims().stream()
                .map(entityMapper::toDto).collect(Collectors.toList()));
    }

    // PUT /api/v1/claims/{id}
    // CUSTOMER updates their SUBMITTED claim (before assignment)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update claim (CUSTOMER)", description = "Only SUBMITTED claims can be updated. Body: claimedAmount, incidentDescription")
    public ResponseEntity<ClaimResponse> updateClaim(@PathVariable("id") Long id,
            @Valid @RequestBody ClaimRequest request) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.updateClaim(id, request)));
    }

    // POST /api/v1/claims/{id}/appeal
    // CUSTOMER appeals a REJECTED claim
    @PostMapping("/{id}/appeal")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Appeal rejected claim (CUSTOMER)")
    public ResponseEntity<ClaimResponse> appealClaim(@PathVariable("id") Long id) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.appealClaim(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Delete claim draft (CUSTOMER)", description = "Only DRAFT claims can be deleted.")
    public ResponseEntity<Void> deleteClaim(@PathVariable("id") Long id) {
        claimService.deleteClaim(id);
        return ResponseEntity.noContent().build();
    }

    // ─── ADMIN APIs
    // ───────────────────────────────────────────────────────────────

    // ADMIN/CLAIMS_OFFICER gets all claims (DEPRECATED: merged into base GET)
    // @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN', 'CLAIMS_OFFICER')")
    // @Operation(summary = "Get all claims (ADMIN/CLAIMS_OFFICER)")
    // public ResponseEntity<List<ClaimResponse>> getAllClaims() {
    //     return ResponseEntity.ok(claimService.getAllClaims().stream()
    //             .map(entityMapper::toDto).collect(Collectors.toList()));
    // }

    // GET /api/v1/claims/status/{status}
    // ADMIN/CLAIMS_OFFICER filters by status
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Submit claim (CUSTOMER)", description = "Submits a DRAFT claim for review. Customers must upload documents before submitting.")
    public ResponseEntity<ClaimResponse> submitClaim(@PathVariable("id") Long id) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.submitClaim(id)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIMS_OFFICER')")
    @Operation(summary = "Get claims by status (ADMIN/CLAIMS_OFFICER)")
    public ResponseEntity<List<ClaimResponse>> getClaimsByStatus(@PathVariable("status") ClaimStatus status) {
        return ResponseEntity.ok(claimService.getClaimsByStatus(status).stream()
                .map(entityMapper::toDto).collect(Collectors.toList()));
    }

    // PUT /api/v1/claims/{id}/assign/{officerId}
    // ADMIN assigns a claims officer to a claim
    @PutMapping("/{id}/assign/{officerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign claims officer (ADMIN)")
    public ResponseEntity<ClaimResponse> assignClaimsOfficer(@PathVariable("id") Long id,
            @PathVariable("officerId") Long officerId) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.assignClaimsOfficer(id, officerId)));
    }

    // ─── SHARED LOOKUP APIs
    // ───────────────────────────────────────────────────────

    // GET /api/v1/claims/{id}
    // ADMIN/CLAIMS_OFFICER/CUSTOMER get claim by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIMS_OFFICER', 'CUSTOMER')")
    @Operation(summary = "Get claim by ID")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.getClaimById(id)));
    }

    // GET /api/v1/claims/number/{claimNumber}
    // ADMIN/CLAIMS_OFFICER/CUSTOMER look up by claim number
    @GetMapping("/number/{claimNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLAIMS_OFFICER', 'CUSTOMER')")
    @Operation(summary = "Get claim by number")
    public ResponseEntity<ClaimResponse> getClaimByNumber(@PathVariable("claimNumber") String claimNumber) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.getClaimByNumber(claimNumber)));
    }

    // ─── CLAIMS OFFICER APIs
    // ──────────────────────────────────────────────────────

    // GET /api/v1/claims/assigned
    // CLAIMS_OFFICER gets all claims assigned to them
    @GetMapping("/assigned")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Get assigned claims (CLAIMS_OFFICER)")
    public ResponseEntity<List<ClaimResponse>> getClaimsByOfficer() {
        Long officerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(claimService.getClaimsAssignedToOfficer(officerId).stream()
                .map(entityMapper::toDto).collect(Collectors.toList()));
    }

    // POST /api/v1/claims/{id}/investigate
    // CLAIMS_OFFICER marks claim as UNDER_INVESTIGATION
    @PostMapping("/{id}/investigate")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Mark under investigation (CLAIMS_OFFICER)")
    public ResponseEntity<ClaimResponse> markUnderInvestigation(@PathVariable("id") Long id) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.markUnderInvestigation(id)));
    }

    // POST /api/v1/claims/{id}/approve
    // CLAIMS_OFFICER approves a claim
    // Body: { "approvedAmount": 45000.00 }
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Approve claim (CLAIMS_OFFICER)", description = "Body: { \"approvedAmount\": 45000.00 }")
    public ResponseEntity<ClaimResponse> approveClaim(@PathVariable("id") Long id,
            @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.approveClaim(id, body.get("approvedAmount"))));
    }

    // POST /api/v1/claims/{id}/reject
    // CLAIMS_OFFICER rejects a claim
    // Body: { "rejectionReason": "Claim falls outside coverage period" }
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Reject claim (CLAIMS_OFFICER)", description = "Body: { \"rejectionReason\": \"reason\" }")
    public ResponseEntity<ClaimResponse> rejectClaim(@PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.rejectClaim(id, body.get("rejectionReason"))));
    }

    // POST /api/v1/claims/{id}/settle
    // CLAIMS_OFFICER settles an APPROVED claim
    // Body: { "settledAmount": 45000.00 }
    @PostMapping("/{id}/settle")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @Operation(summary = "Settle claim (CLAIMS_OFFICER)", description = "Body: { \"settledAmount\": 45000.00 }")
    public ResponseEntity<ClaimResponse> settleClaim(@PathVariable("id") Long id,
            @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(entityMapper.toDto(claimService.settleClaim(id, body.get("settledAmount"))));
    }
}
