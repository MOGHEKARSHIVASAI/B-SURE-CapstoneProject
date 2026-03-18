package org.hartford.binsure.controller;

import org.hartford.binsure.dto.InsuranceProductRequest;
import org.hartford.binsure.dto.response.InsuranceProductResponse;
import org.hartford.binsure.entity.InsuranceProduct;
import org.hartford.binsure.enums.ProductCategory;
import org.hartford.binsure.mapper.EntityMapper;
import org.hartford.binsure.service.InsuranceProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure Mockito unit tests for InsuranceProductController.
 * No Spring context, no MockMvc, no @WebMvcTest — just direct method calls.
 * We mock the Service and EntityMapper, then call controller methods directly.
 */
@ExtendWith(MockitoExtension.class)
class InsuranceProductControllerTest {

    @Mock
    private InsuranceProductService productService;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private InsuranceProductController controller;

    // ──────────────────────────────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────────────────────────────
    private InsuranceProduct buildEntity(Long id, String name, String code,
                                          ProductCategory cat) {
        InsuranceProduct p = new InsuranceProduct();
        p.setId(id);
        p.setProductName(name);
        p.setProductCode(code);
        p.setCategory(cat);
        p.setBasePremiumRate(new BigDecimal("0.0500"));
        p.setActive(true);
        return p;
    }

    private InsuranceProductResponse buildResponse(Long id, String name, String code,
                                                    ProductCategory cat) {
        return InsuranceProductResponse.builder()
                .id(id)
                .productName(name)
                .productCode(code)
                .category(cat)
                .basePremiumRate(new BigDecimal("0.0500"))
                .isActive(true)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 1 — getAllActiveProducts : returns 200 with list
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getAllActiveProducts_Returns200WithList() {
        InsuranceProduct entity1 = buildEntity(1L, "Property Insurance", "PROP-001", ProductCategory.PROPERTY);
        InsuranceProduct entity2 = buildEntity(2L, "Cyber Insurance", "CYB-001", ProductCategory.CYBER);
        InsuranceProductResponse resp1 = buildResponse(1L, "Property Insurance", "PROP-001", ProductCategory.PROPERTY);
        InsuranceProductResponse resp2 = buildResponse(2L, "Cyber Insurance", "CYB-001", ProductCategory.CYBER);

        when(productService.getAllActiveProducts()).thenReturn(List.of(entity1, entity2));
        when(entityMapper.toDto(entity1)).thenReturn(resp1);
        when(entityMapper.toDto(entity2)).thenReturn(resp2);

        ResponseEntity<List<InsuranceProductResponse>> response = controller.getAllActiveProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Property Insurance", response.getBody().get(0).getProductName());
        assertEquals("Cyber Insurance", response.getBody().get(1).getProductName());

        verify(productService).getAllActiveProducts();
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 2 — getAllActiveProducts : empty list → returns 200 with []
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getAllActiveProducts_WhenNone_Returns200WithEmptyList() {
        when(productService.getAllActiveProducts()).thenReturn(List.of());

        ResponseEntity<List<InsuranceProductResponse>> response = controller.getAllActiveProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 3 — getProductById : returns 200 with product
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getProductById_Returns200WithProduct() {
        InsuranceProduct entity = buildEntity(5L, "Marine Insurance", "MAR-001", ProductCategory.MARINE);
        InsuranceProductResponse resp = buildResponse(5L, "Marine Insurance", "MAR-001", ProductCategory.MARINE);

        when(productService.getProductById(5L)).thenReturn(entity);
        when(entityMapper.toDto(entity)).thenReturn(resp);

        ResponseEntity<InsuranceProductResponse> response = controller.getProductById(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getId());
        assertEquals("Marine Insurance", response.getBody().getProductName());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 4 — getProductsByCategory : filters by CYBER → returns list
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getProductsByCategory_ReturnsFilteredList() {
        InsuranceProduct entity = buildEntity(3L, "Cyber Shield", "CYB-002", ProductCategory.CYBER);
        InsuranceProductResponse resp = buildResponse(3L, "Cyber Shield", "CYB-002", ProductCategory.CYBER);

        when(productService.getProductsByCategory(ProductCategory.CYBER)).thenReturn(List.of(entity));
        when(entityMapper.toDto(entity)).thenReturn(resp);

        ResponseEntity<List<InsuranceProductResponse>> response =
                controller.getProductsByCategory(ProductCategory.CYBER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(ProductCategory.CYBER, response.getBody().get(0).getCategory());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 5 — createProduct : returns 201 CREATED
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void createProduct_Returns201WithCreatedProduct() {
        InsuranceProductRequest request = new InsuranceProductRequest();
        request.setProductName("Health Insurance");
        request.setProductCode("HLT-001");
        request.setCategory(ProductCategory.HEALTH);
        request.setBasePremiumRate(new BigDecimal("0.0400"));

        InsuranceProduct savedEntity = buildEntity(10L, "Health Insurance", "HLT-001", ProductCategory.HEALTH);
        InsuranceProductResponse resp = buildResponse(10L, "Health Insurance", "HLT-001", ProductCategory.HEALTH);

        when(productService.createProduct(any(InsuranceProductRequest.class))).thenReturn(savedEntity);
        when(entityMapper.toDto(savedEntity)).thenReturn(resp);

        ResponseEntity<InsuranceProductResponse> response = controller.createProduct(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("Health Insurance", response.getBody().getProductName());
        verify(productService).createProduct(any(InsuranceProductRequest.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 6 — updateProduct : returns 200 with updated product
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void updateProduct_Returns200WithUpdatedProduct() {
        InsuranceProductRequest request = new InsuranceProductRequest();
        request.setProductName("Updated Property Insurance");

        InsuranceProduct updatedEntity = buildEntity(1L, "Updated Property Insurance", "PROP-001",
                ProductCategory.PROPERTY);
        InsuranceProductResponse resp = buildResponse(1L, "Updated Property Insurance", "PROP-001",
                ProductCategory.PROPERTY);

        when(productService.updateProduct(eq(1L), any(InsuranceProductRequest.class))).thenReturn(updatedEntity);
        when(entityMapper.toDto(updatedEntity)).thenReturn(resp);

        ResponseEntity<InsuranceProductResponse> response = controller.updateProduct(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Property Insurance", response.getBody().getProductName());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 7 — deactivateProduct : returns 200 with success message
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void deactivateProduct_Returns200WithMessage() {
        doNothing().when(productService).deactivateProduct(1L);

        ResponseEntity<Map<String, String>> response = controller.deactivateProduct(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Insurance product deactivated successfully. ID: 1",
                response.getBody().get("message"));
        verify(productService).deactivateProduct(1L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 8 — getAllProducts (admin) : returns 200 with all products
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getAllProducts_Returns200WithAllIncludingInactive() {
        InsuranceProduct active = buildEntity(1L, "Active", "A-001", ProductCategory.PROPERTY);
        InsuranceProduct inactive = buildEntity(2L, "Inactive", "I-001", ProductCategory.HEALTH);
        inactive.setActive(false);

        InsuranceProductResponse respActive = buildResponse(1L, "Active", "A-001", ProductCategory.PROPERTY);
        InsuranceProductResponse respInactive = InsuranceProductResponse.builder()
                .id(2L).productName("Inactive").productCode("I-001")
                .category(ProductCategory.HEALTH).isActive(false).build();

        when(productService.getAllProducts()).thenReturn(List.of(active, inactive));
        when(entityMapper.toDto(active)).thenReturn(respActive);
        when(entityMapper.toDto(inactive)).thenReturn(respInactive);

        ResponseEntity<List<InsuranceProductResponse>> response = controller.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).isActive());
        assertFalse(response.getBody().get(1).isActive());
    }
}

