package org.hartford.binsure.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "businesses")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String companyName;

    @Column(unique = true)
    private String companyRegNumber;

    private String industryType;

    @Column(precision = 15, scale = 2)
    private BigDecimal annualRevenue;

    private Integer numEmployees;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;

    @Column(nullable = false, columnDefinition = "VARCHAR(100) DEFAULT 'India'")
    private String country = "India";

    @Column(unique = true)
    private String taxId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
