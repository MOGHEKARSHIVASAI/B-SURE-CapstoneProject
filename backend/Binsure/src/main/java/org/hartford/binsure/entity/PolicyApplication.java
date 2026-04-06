package org.hartford.binsure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.ApplicationStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "policy_applications")
public class PolicyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @JsonIgnore
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal coverageAmount;

    @Column(nullable = false)
    private LocalDate coverageStartDate;

    @Column(nullable = false)
    private LocalDate coverageEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String riskNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_underwriter_id")
    private User assignedUnderwriter;

    @Column(name = "ai_risk_score")
    private Integer aiRiskScore;

    @Column(name = "ai_underwriting_analysis", columnDefinition = "TEXT")
    private String aiUnderwritingAnalysis;

    @Column(name = "recommended_premium", precision = 15, scale = 2)
    private BigDecimal recommendedPremium;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    @Column(precision = 15, scale = 2)
    private BigDecimal annualPremium;
    @Column(precision = 5, scale = 2)
    private BigDecimal premiumAdjustmentPct;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    public int getDocumentCount() {
        return documents != null ? documents.size() : 0;
    }

    // Convenience getters for frontend — return plain IDs instead of full objects
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

    public Long getAssignedUnderwriterId() {
        return assignedUnderwriter != null ? assignedUnderwriter.getId() : null;
    }

    public String getAssignedUnderwriterName() {
        return assignedUnderwriter != null
                ? assignedUnderwriter.getFirstName() + " " + assignedUnderwriter.getLastName()
                : null;
    }
}