package org.hartford.binsure.service;

import org.hartford.binsure.dto.InsuranceProductRequest;
import org.hartford.binsure.entity.InsuranceProduct;
import org.hartford.binsure.enums.ProductCategory;
import org.hartford.binsure.exception.DuplicateResourceException;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.InsuranceProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InsuranceProductService {

    @Autowired
    private InsuranceProductRepository productRepository;

    public InsuranceProduct getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", "ID", productId));
    }

    public List<InsuranceProduct> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public List<InsuranceProduct> getAllProducts() {
        return productRepository.findAll();
    }

    public List<InsuranceProduct> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    public InsuranceProduct createProduct(InsuranceProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new DuplicateResourceException("InsuranceProduct", "productCode", request.getProductCode());
        }
        InsuranceProduct product = new InsuranceProduct();
        product.setProductName(request.getProductName());
        product.setProductCode(request.getProductCode());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setBasePremiumRate(request.getBasePremiumRate());
        product.setMinCoverageAmount(request.getMinCoverageAmount());
        product.setMaxCoverageAmount(request.getMaxCoverageAmount());
        product.setActive(true);
        return productRepository.save(product);
    }

    public InsuranceProduct updateProduct(Long productId, InsuranceProductRequest request) {
        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", "ID", productId));
        if (!product.isActive())
            throw new InvalidOperationException("Cannot update a deactivated product. Re-activate it first.");
        if (request.getProductName() != null)      product.setProductName(request.getProductName());
        if (request.getCategory() != null)         product.setCategory(request.getCategory());
        if (request.getDescription() != null)      product.setDescription(request.getDescription());
        if (request.getBasePremiumRate() != null)  product.setBasePremiumRate(request.getBasePremiumRate());
        if (request.getMinCoverageAmount() != null) product.setMinCoverageAmount(request.getMinCoverageAmount());
        if (request.getMaxCoverageAmount() != null) product.setMaxCoverageAmount(request.getMaxCoverageAmount());
        return productRepository.save(product);
    }

    public void deactivateProduct(Long productId) {
        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceProduct", "ID", productId));
        if (!product.isActive())
            throw new InvalidOperationException("InsuranceProduct with ID " + productId + " is already deactivated.");
        product.setActive(false);
        productRepository.save(product);
    }
}
