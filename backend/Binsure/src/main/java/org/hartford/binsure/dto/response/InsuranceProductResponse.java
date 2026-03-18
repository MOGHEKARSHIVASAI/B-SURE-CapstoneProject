package org.hartford.binsure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.ProductCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceProductResponse {
    private Long id;
    private String productName;
    private String productCode;
    private ProductCategory category;
    private String description;
    private BigDecimal basePremiumRate;
    private BigDecimal minCoverageAmount;
    private BigDecimal maxCoverageAmount;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived fields
    private Long createdById;
    private String createdByName;
}
