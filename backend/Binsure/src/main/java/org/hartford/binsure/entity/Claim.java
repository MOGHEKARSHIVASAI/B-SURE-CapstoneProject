package org.hartford.binsure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.ClaimStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    @JsonIgnore
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false)
    private LocalDate claimDate;

    private LocalDate settlementDate;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal claimedAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal settledAmount;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String incidentDescription;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ClaimStatus status;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_analysis", columnDefinition = "TEXT")
    private String riskAnalysis;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    public int getDocumentCount() {
        return documents != null ? documents.size() : 0;
    }

    // Convenience getters for frontend
    public Long getPolicyId() {
        return policy != null ? policy.getId() : null;
    }

    public String getPolicyNumber() {
        return policy != null ? policy.getPolicyNumber() : null;
    }

    public Long getBusinessId() {
        return business != null ? business.getId() : null;
    }

    public String getCompanyName() {
        return business != null ? business.getCompanyName() : null;
    }

    public Long getAssignedOfficerId() {
        return assignedOfficer != null ? assignedOfficer.getId() : null;
    }

    public String getAssignedOfficerName() {
        return assignedOfficer != null ? assignedOfficer.getFirstName() + " " + assignedOfficer.getLastName() : null;
    }

    public String getProductName() {
        return policy != null ? policy.getProductName() : null;
    }
}