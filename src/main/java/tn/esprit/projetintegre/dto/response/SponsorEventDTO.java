package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SponsorEventDTO {
    private String sponsorName;
    private String eventTitle;
    private String sponsorshipLevel;
    private LocalDateTime startDate;
}
