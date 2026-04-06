package org.hartford.binsure.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateBusinessRequest {
    private String companyName;
    private String industryType;
    private BigDecimal annualRevenue;
    private Integer numEmployees;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String companyRegNumber;
    private String taxId;
}
