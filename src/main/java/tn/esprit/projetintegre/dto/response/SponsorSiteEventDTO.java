package tn.esprit.projetintegre.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SponsorSiteEventDTO {
    private String sponsorName;
    private String sponsorCity;
    private String eventTitle;
    private String siteName;
    private String siteCity;
}
