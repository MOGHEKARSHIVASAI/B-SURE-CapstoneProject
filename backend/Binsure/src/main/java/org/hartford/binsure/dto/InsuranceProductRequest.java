package org.hartford.binsure.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hartford.binsure.enums.ProductCategory;
import java.math.BigDecimal;
@Data
public class InsuranceProductRequest {
    @NotBlank(message = "Product name is required")
    private String productName;
    @NotBlank(message = "Product code is required")
    private String productCode;
    @NotNull(message = "Category is required")
    private ProductCategory category;
    private String description;
    @NotNull(message = "Base premium rate is required")
    @DecimalMin(value = "0.0001", message = "Base premium rate must be greater than 0")
    private BigDecimal basePremiumRate;
    @DecimalMin(value = "0.00", message = "Min coverage cannot be negative")
    private BigDecimal minCoverageAmount;
    @DecimalMin(value = "0.00", message = "Max coverage cannot be negative")
    private BigDecimal maxCoverageAmount;
}
