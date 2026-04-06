package org.hartford.binsure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.PolicyStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String policyNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private PolicyApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @JsonIgnore
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private InsuranceProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "underwriter_id")
    private User underwriter;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal coverageAmount;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal annualPremium;

    @Column(precision = 10, scale = 2)
    private BigDecimal deductible;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    private String policyDocumentUrl;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime cancelledAt;

    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Convenience getters for frontend
    public Long getApplicationId() {
        return application != null ? application.getId() : null;
    }

    public Long getBusinessId() {
        return business != null ? business.getId() : null;
    }

    public String getCompanyName() {
        return business != null ? business.getCompanyName() : null;
    }

    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    public String getProductName() {
        return product != null ? product.getProductName() : null;
    }

    public Long getUnderwriterId() {
        return underwriter != null ? underwriter.getId() : null;
    }

    public String getUnderwriterName() {
        return underwriter != null ? underwriter.getFirstName() + " " + underwriter.getLastName() : null;
    }
}