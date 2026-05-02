package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorDashboardResponse {

    private Long sponsorId;
    private String sponsorName;
    private String tier;
    private Boolean isActive;

    // Performance metrics
    private BigDecimal totalAmountSponsored;
    private Double averageAmountPerEvent;
    private Double averageEventDurationDays; // Actually stores number of distinct events
    private Integer compositeScore; // 0-100
    private String statusLabel; // "High Performer" or "Needs Improvement"

    // Additional details
    private Long totalSponsorships;
    private Long distinctEventsCount;

    // New criteria for composite score
    private Long tenureDays; // Days since sponsor was created
}
