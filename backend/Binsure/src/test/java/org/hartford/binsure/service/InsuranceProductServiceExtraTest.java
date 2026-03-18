package org.hartford.binsure.service;

import org.hartford.binsure.dto.InsuranceProductRequest;
import org.hartford.binsure.entity.InsuranceProduct;
import org.hartford.binsure.enums.ProductCategory;
import org.hartford.binsure.exception.DuplicateResourceException;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.InsuranceProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceProductServiceTest {

    @Mock
    private InsuranceProductRepository productRepository;

    @InjectMocks
    private InsuranceProductService insuranceProductService;

    private InsuranceProduct product;
    private InsuranceProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = new InsuranceProduct();
        product.setId(1L);
        product.setProductName("Business Liability");
        product.setProductCode("BL-001");
        product.setCategory(ProductCategory.LIABILITY);
        product.setBasePremiumRate(new BigDecimal("0.05"));
        product.setActive(true);

        productRequest = new InsuranceProductRequest();
        productRequest.setProductName("Business Liability");
        productRequest.setProductCode("BL-001");
        productRequest.setCategory(ProductCategory.LIABILITY);
        productRequest.setBasePremiumRate(new BigDecimal("0.05"));
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        InsuranceProduct result = insuranceProductService.getProductById(1L);
        assertNotNull(result);
        assertEquals("Business Liability", result.getProductName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> insuranceProductService.getProductById(999L));
    }

    @Test
    void testGetAllActiveProducts_Success() {
        List<InsuranceProduct> activeProducts = Arrays.asList(product);
        when(productRepository.findByIsActiveTrue()).thenReturn(activeProducts);
        List<InsuranceProduct> results = insuranceProductService.getAllActiveProducts();
        assertEquals(1, results.size());
        verify(productRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testCreateProduct_Success() {
        when(productRepository.existsByProductCode("BL-001")).thenReturn(false);
        when(productRepository.save(any(InsuranceProduct.class))).thenReturn(product);
        InsuranceProduct result = insuranceProductService.createProduct(productRequest);
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(InsuranceProduct.class));
    }

    @Test
    void testCreateProduct_DuplicateCode() {
        when(productRepository.existsByProductCode("BL-001")).thenReturn(true);
        assertThrows(DuplicateResourceException.class,
                () -> insuranceProductService.createProduct(productRequest));
    }

    @Test
    void testDeactivateProduct_Success() {
        product.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(InsuranceProduct.class))).thenReturn(product);
        insuranceProductService.deactivateProduct(1L);
        verify(productRepository, times(1)).save(any(InsuranceProduct.class));
    }
}

