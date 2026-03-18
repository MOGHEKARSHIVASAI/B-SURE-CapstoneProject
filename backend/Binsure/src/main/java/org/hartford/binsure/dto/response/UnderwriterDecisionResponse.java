package org.hartford.binsure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.DecisionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnderwriterDecisionResponse {
    private Long id;
    private DecisionType decision;
    private Integer riskScore;
    private BigDecimal premiumAdjustmentPct;
    private String comments;
    private LocalDateTime decidedAt;

    // Derived fields
    private Long applicationId;
    private Long underwriterId;
    private String underwriterName;
}
