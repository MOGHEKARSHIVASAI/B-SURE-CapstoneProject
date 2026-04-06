package org.hartford.binsure.repository;

import org.hartford.binsure.entity.InsuranceProduct;
import org.hartford.binsure.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, Long> {
    Optional<InsuranceProduct> findByProductCode(String productCode);
    List<InsuranceProduct> findByCategory(ProductCategory category);
    List<InsuranceProduct> findByIsActiveTrue();
    boolean existsByProductCode(String productCode);
}

