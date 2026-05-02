package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipMatchResponse {
    private String eventTitle;
    private Long eventId;
    private String sponsorName;
    private Long sponsorId;
    private String sponsorTier;
    private String category;
    private String estimatedValue;
    private Integer matchScore;
}
