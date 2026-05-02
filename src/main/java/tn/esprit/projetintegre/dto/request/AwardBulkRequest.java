package tn.esprit.projetintegre.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AwardBulkRequest {
    private List<Long> userIds;
    private Long badgeId;
    private Long eventId;
}
