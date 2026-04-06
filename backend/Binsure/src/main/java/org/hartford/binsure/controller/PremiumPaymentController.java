package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.dto.PremiumPaymentRequest;
import org.hartford.binsure.entity.PremiumPayment;
import org.hartford.binsure.enums.PaymentStatus;
import org.hartford.binsure.service.PremiumPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.dto.response.PremiumPaymentResponse;
import org.hartford.binsure.mapper.EntityMapper;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "9. Premium Payments", description = "Customer premium payment management")
@SecurityRequirement(name = "Bearer Authentication")
public class PremiumPaymentController {

        @Autowired
        private PremiumPaymentService paymentService;
        @Autowired
        private SecurityUtils securityUtils;
        @Autowired
        private EntityMapper entityMapper;

        // ─── CUSTOMER APIs
        // ────────────────────────────────────────────────────────────

        // POST /api/v1/payments
        // CUSTOMER makes a premium payment for a CUSTOMER_ACCEPTED application
        @PostMapping
        @PreAuthorize("hasRole('CUSTOMER')")
        @Operation(summary = "Make premium payment (CUSTOMER)", description = "Customer pays the annual premium for a CUSTOMER_ACCEPTED application.")
        public ResponseEntity<PremiumPaymentResponse> makePayment(
                        @Valid @RequestBody PremiumPaymentRequest request) {
                Long customerId = securityUtils.getCurrentUserId();
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(entityMapper.toDto(paymentService.makePayment(customerId, request)));
        }

        // GET /api/v1/payments
        // ADMIN gets all, CUSTOMER gets their own
        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
        @Operation(summary = "Get payments", description = "Returns all payments if ADMIN, or just own records if CUSTOMER.")
        public ResponseEntity<List<PremiumPaymentResponse>> getPayments() {
                if (securityUtils.hasRole("CUSTOMER")) {
                        Long customerId = securityUtils.getCurrentUserId();
                        return ResponseEntity.ok(paymentService.getPaymentsByCustomer(customerId).stream()
                                        .map(entityMapper::toDto).collect(Collectors.toList()));
                }
                return ResponseEntity.ok(paymentService.getAllPayments().stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // ─── ADMIN APIs
        // ───────────────────────────────────────────────────────────────

        // ADMIN gets all payments (DEPRECATED: merged into base GET)
        // @GetMapping
        // @PreAuthorize("hasRole('ADMIN')")
        // @Operation(summary = "Get all payments (ADMIN)")
        // public ResponseEntity<List<PremiumPaymentResponse>> getAllPayments() {
        //         return ResponseEntity.ok(paymentService.getAllPayments().stream()
        //                         .map(entityMapper::toDto).collect(Collectors.toList()));
        // }

        // GET /api/v1/payments/application/{applicationId}
        // ADMIN/CUSTOMER gets payments for a specific application
        @GetMapping("/application/{applicationId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
        @Operation(summary = "Get payments by application (ADMIN/CUSTOMER)", description = "Returns all payment records for a specific policy application.")
        public ResponseEntity<List<PremiumPaymentResponse>> getPaymentsByApplication(
                        @PathVariable("applicationId") Long applicationId) {
                return ResponseEntity.ok(paymentService.getPaymentsByApplication(applicationId).stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // GET /api/v1/payments/status/{status}
        // ADMIN filters payments by status
        @GetMapping("/status/{status}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get payments by status (ADMIN)", description = "Filter payments by status: PENDING, PAID, FAILED, REFUNDED")
        public ResponseEntity<List<PremiumPaymentResponse>> getPaymentsByStatus(
                        @PathVariable("status") PaymentStatus status) {
                return ResponseEntity.ok(paymentService.getPaymentsByStatus(status).stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }
}
