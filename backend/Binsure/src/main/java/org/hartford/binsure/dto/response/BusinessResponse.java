package org.hartford.binsure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessResponse {
    private Long id;
    private String companyName;
    private String companyRegNumber;
    private String industryType;
    private BigDecimal annualRevenue;
    private Integer numEmployees;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String taxId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User fields (linked owner)
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
}
