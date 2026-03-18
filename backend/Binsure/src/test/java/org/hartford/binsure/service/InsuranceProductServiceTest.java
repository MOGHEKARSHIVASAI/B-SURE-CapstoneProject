package org.hartford.binsure.service;

import org.hartford.binsure.dto.InsuranceProductRequest;
import org.hartford.binsure.entity.InsuranceProduct;
import org.hartford.binsure.enums.ProductCategory;
import org.hartford.binsure.exception.DuplicateResourceException;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.InsuranceProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure Mockito unit tests for InsuranceProductService.
 * No Spring context loaded — fast, isolated, tests only business logic.
 */
@ExtendWith(MockitoExtension.class)
class InsuranceProductServiceTest {

    @Mock
    private InsuranceProductRepository productRepository;

    @InjectMocks
    private InsuranceProductService productService;

    // ──────────────────────────────────────────────────────────────────────
    // HELPER — builds a sample InsuranceProduct entity
    // ──────────────────────────────────────────────────────────────────────
    private InsuranceProduct buildProduct(Long id, String name, String code,
                                          ProductCategory category, boolean active) {
        InsuranceProduct p = new InsuranceProduct();
        p.setId(id);
        p.setProductName(name);
        p.setProductCode(code);
        p.setCategory(category);
        p.setBasePremiumRate(new BigDecimal("0.0500"));
        p.setMinCoverageAmount(new BigDecimal("100000"));
        p.setMaxCoverageAmount(new BigDecimal("5000000"));
        p.setActive(active);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 1 — getProductById : product exists → returns it
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getProductById_WhenExists_ReturnsProduct() {
        // Arrange
        InsuranceProduct product = buildProduct(1L, "Property Insurance", "PROP-001",
                ProductCategory.PROPERTY, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        InsuranceProduct result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Property Insurance", result.getProductName());
        assertEquals("PROP-001", result.getProductCode());
        verify(productRepository, times(1)).findById(1L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 2 — getProductById : product NOT found → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getProductById_WhenNotFound_ThrowsResourceNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(99L)
        );
        assertTrue(ex.getMessage().contains("InsuranceProduct"));
        verify(productRepository).findById(99L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 3 — getAllActiveProducts : returns only active products
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getAllActiveProducts_ReturnsList() {
        List<InsuranceProduct> activeList = List.of(
                buildProduct(1L, "Product A", "A-001", ProductCategory.PROPERTY, true),
                buildProduct(2L, "Product B", "B-001", ProductCategory.CYBER, true)
        );
        when(productRepository.findByIsActiveTrue()).thenReturn(activeList);

        List<InsuranceProduct> result = productService.getAllActiveProducts();

        assertEquals(2, result.size());
        assertEquals("Product A", result.get(0).getProductName());
        verify(productRepository).findByIsActiveTrue();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 4 — getProductsByCategory : filters correctly
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getProductsByCategory_ReturnsCategoryMatches() {
        List<InsuranceProduct> cyberProducts = List.of(
                buildProduct(3L, "Cyber Shield", "CYB-001", ProductCategory.CYBER, true)
        );
        when(productRepository.findByCategory(ProductCategory.CYBER)).thenReturn(cyberProducts);

        List<InsuranceProduct> result = productService.getProductsByCategory(ProductCategory.CYBER);

        assertEquals(1, result.size());
        assertEquals(ProductCategory.CYBER, result.get(0).getCategory());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 5 — createProduct : valid request → product saved
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void createProduct_WhenCodeUnique_SavesAndReturns() {
        InsuranceProductRequest request = new InsuranceProductRequest();
        request.setProductName("Cyber Insurance");
        request.setProductCode("CYB-002");
        request.setCategory(ProductCategory.CYBER);
        request.setDescription("Covers data breaches");
        request.setBasePremiumRate(new BigDecimal("0.0300"));
        request.setMinCoverageAmount(new BigDecimal("50000"));
        request.setMaxCoverageAmount(new BigDecimal("2000000"));

        when(productRepository.existsByProductCode("CYB-002")).thenReturn(false);
        when(productRepository.save(any(InsuranceProduct.class))).thenAnswer(invocation -> {
            InsuranceProduct saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        InsuranceProduct result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Cyber Insurance", result.getProductName());
        assertEquals("CYB-002", result.getProductCode());
        assertTrue(result.isActive());
        verify(productRepository).existsByProductCode("CYB-002");
        verify(productRepository).save(any(InsuranceProduct.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 6 — createProduct : duplicate productCode → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void createProduct_WhenCodeDuplicate_ThrowsDuplicateResource() {
        InsuranceProductRequest request = new InsuranceProductRequest();
        request.setProductCode("PROP-001");

        when(productRepository.existsByProductCode("PROP-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> productService.createProduct(request));

        verify(productRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 7 — updateProduct : product active → updates successfully
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void updateProduct_WhenActive_UpdatesFields() {
        InsuranceProduct existing = buildProduct(1L, "Old Name", "PROP-001",
                ProductCategory.PROPERTY, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(InsuranceProduct.class))).thenAnswer(i -> i.getArgument(0));

        InsuranceProductRequest request = new InsuranceProductRequest();
        request.setProductName("New Name");
        request.setDescription("Updated description");

        InsuranceProduct result = productService.updateProduct(1L, request);

        assertEquals("New Name", result.getProductName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(ProductCategory.PROPERTY, result.getCategory()); // unchanged
        verify(productRepository).save(existing);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 8 — updateProduct : product deactivated → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void updateProduct_WhenDeactivated_ThrowsInvalidOperation() {
        InsuranceProduct inactive = buildProduct(2L, "Inactive", "X-001",
                ProductCategory.HEALTH, false);
        when(productRepository.findById(2L)).thenReturn(Optional.of(inactive));

        InsuranceProductRequest request = new InsuranceProductRequest();
        request.setProductName("Should Fail");

        assertThrows(InvalidOperationException.class,
                () -> productService.updateProduct(2L, request));
        verify(productRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 9 — deactivateProduct : active product → sets isActive=false
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void deactivateProduct_WhenActive_SetsInactive() {
        InsuranceProduct product = buildProduct(1L, "Product", "P-001",
                ProductCategory.PROPERTY, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);

        productService.deactivateProduct(1L);

        assertFalse(product.isActive());
        verify(productRepository).save(product);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 10 — deactivateProduct : already inactive → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void deactivateProduct_WhenAlreadyInactive_ThrowsInvalidOperation() {
        InsuranceProduct product = buildProduct(1L, "Product", "P-001",
                ProductCategory.PROPERTY, false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(InvalidOperationException.class,
                () -> productService.deactivateProduct(1L));
        verify(productRepository, never()).save(any());
    }
}

