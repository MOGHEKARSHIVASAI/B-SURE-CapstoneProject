package org.hartford.binsure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPaymentResponse {
    private Long id;
    private String paymentReference;
    private BigDecimal amount;
    private PaymentStatus status;
    private String remarks;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived fields
    private Long applicationId;
    private Long businessId;
    private String companyName;
}
