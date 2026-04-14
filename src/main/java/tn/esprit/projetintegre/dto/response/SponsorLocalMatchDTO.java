package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SponsorLocalMatchDTO {
    private String sponsorName;
    private String eventTitle;
    private String matchedCity;
}
