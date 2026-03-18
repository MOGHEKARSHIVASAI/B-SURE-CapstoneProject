package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.dto.InsuranceProductRequest;
import org.hartford.binsure.entity.InsuranceProduct;
import org.hartford.binsure.enums.ProductCategory;
import org.hartford.binsure.service.InsuranceProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.hartford.binsure.dto.response.InsuranceProductResponse;
import org.hartford.binsure.mapper.EntityMapper;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "4. Insurance Products", description = "Insurance product management")
@SecurityRequirement(name = "Bearer Authentication")
public class InsuranceProductController {

        @Autowired
        private InsuranceProductService productService;
        @Autowired
        private EntityMapper entityMapper;

        // GET /api/v1/products
        // Public — all active products (customers browse before applying)
        @GetMapping
        @Operation(summary = "Get all active products (Public)", description = "Returns all active insurance products. Public endpoint — no token required.")
        public ResponseEntity<List<InsuranceProductResponse>> getAllActiveProducts() {
                return ResponseEntity.ok(productService.getAllActiveProducts().stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // GET /api/v1/products/all
        // ADMIN — all products including inactive
        @GetMapping("/all")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get ALL products including inactive (ADMIN only)", description = "Admin sees all products including deactivated ones.")
        public ResponseEntity<List<InsuranceProductResponse>> getAllProducts() {
                return ResponseEntity.ok(productService.getAllProducts().stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // GET /api/v1/products/{id}
        // Public — product detail
        @GetMapping("/{id}")
        @Operation(summary = "Get product by ID (Public)", description = "Returns details of a specific insurance product.")
        public ResponseEntity<InsuranceProductResponse> getProductById(@PathVariable("id") Long id) {
                return ResponseEntity.ok(entityMapper.toDto(productService.getProductById(id)));
        }

        // GET /api/v1/products/category/{category}
        // Public — products filtered by category
        @GetMapping("/category/{category}")
        @Operation(summary = "Get products by category (Public)", description = "Filter products by category: LIABILITY, PROPERTY, CYBER, HEALTH, VEHICLE, MARINE")
        public ResponseEntity<List<InsuranceProductResponse>> getProductsByCategory(
                        @PathVariable("category") ProductCategory category) {
                return ResponseEntity.ok(productService.getProductsByCategory(category).stream()
                                .map(entityMapper::toDto).collect(Collectors.toList()));
        }

        // POST /api/v1/products
        // ADMIN creates a new product
        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Create insurance product (ADMIN only)", description = "Admin creates a new insurance product. productCode must be unique.")
        public ResponseEntity<InsuranceProductResponse> createProduct(
                        @Valid @RequestBody InsuranceProductRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(entityMapper.toDto(productService.createProduct(request)));
        }

        // PUT /api/v1/products/{id}
        // ADMIN updates a product
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update insurance product (ADMIN only)", description = "Update product details. Only non-null fields are updated. Cannot update a deactivated product.")
        public ResponseEntity<InsuranceProductResponse> updateProduct(
                        @PathVariable("id") Long id,
                        @Valid @RequestBody InsuranceProductRequest request) {
                return ResponseEntity.ok(entityMapper.toDto(productService.updateProduct(id, request)));
        }

        // DELETE /api/v1/products/{id}
        // ADMIN soft-deletes (deactivates) a product
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Deactivate product (ADMIN only)", description = "Soft-delete — sets isActive=false. Product will no longer appear in public listings.")
        public ResponseEntity<Map<String, String>> deactivateProduct(@PathVariable("id") Long id) {
                productService.deactivateProduct(id);
                return ResponseEntity.ok(Map.of("message", "Insurance product deactivated successfully. ID: " + id));
        }
}
