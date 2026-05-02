package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRevenueDTO {
    private String eventTitle;
    private String organizerName;
    private BigDecimal totalRevenue;
    private Long participantCount;
}
