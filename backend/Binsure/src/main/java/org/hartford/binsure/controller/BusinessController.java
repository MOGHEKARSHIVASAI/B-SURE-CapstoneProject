package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.dto.BusinessUpdateRequest;
import org.hartford.binsure.dto.CreateBusinessRequest;
import org.hartford.binsure.dto.response.BusinessResponse;
import org.hartford.binsure.entity.Business;
import org.hartford.binsure.mapper.EntityMapper;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/businesses")
@Tag(name = "Business Management", description = "Endpoints for managing business profiles")
@SecurityRequirement(name = "Bearer Authentication")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private EntityMapper entityMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER', 'CLAIMS_OFFICER', 'CUSTOMER')")
    @Operation(summary = "Get businesses", description = "Returns all businesses if ADMIN/UW/CO, or just owned businesses if CUSTOMER.")
    public ResponseEntity<List<BusinessResponse>> getBusinesses() {
        if (securityUtils.hasRole("CUSTOMER")) {
            Long userId = securityUtils.getCurrentUserId();
            return ResponseEntity.ok(businessService.getBusinessesByUserId(userId).stream()
                    .map(entityMapper::toDto).collect(Collectors.toList()));
        }
        return ResponseEntity.ok(businessService.getAllBusinesses().stream()
                .map(entityMapper::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'UNDERWRITER', 'CLAIMS_OFFICER')")
    @Operation(summary = "Get business by ID")
    public ResponseEntity<BusinessResponse> getBusinessById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(entityMapper.toDto(businessService.getBusinessById(id)));
    }

    // GET /api/v1/businesses/my (DEPRECATED: merged into base GET)
    // @GetMapping("/my")
    // @PreAuthorize("hasRole('CUSTOMER')")
    // @Operation(summary = "Get current user's businesses")
    // public ResponseEntity<List<BusinessResponse>> getMyBusinesses() {
    //     Long userId = securityUtils.getCurrentUserId();
    //     return ResponseEntity.ok(businessService.getBusinessesByUserId(userId).stream()
    //             .map(entityMapper::toDto).collect(Collectors.toList()));
    // }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Add a new business profile")
    public ResponseEntity<BusinessResponse> addBusiness(@Valid @RequestBody CreateBusinessRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entityMapper.toDto(businessService.addBusinessProfile(userId, request)));
    }

    // Alias for frontend compatibility (Removed /profile)
    // Use /businesses/{id} for updates instead.

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Update business profile")
    public ResponseEntity<BusinessResponse> updateBusiness(@PathVariable("id") Long id,
            @Valid @RequestBody BusinessUpdateRequest request) {
        return ResponseEntity.ok(entityMapper.toDto(businessService.updateBusinessProfile(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Delete business profile")
    public ResponseEntity<java.util.Map<String, String>> deleteBusiness(@PathVariable("id") Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Business profile deleted successfully. ID: " + id));
    }
}
