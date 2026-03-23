package tn.esprit.projetintegre.dto.response;

import lombok.*;
import tn.esprit.projetintegre.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private String picture;
    private String description;
    private String eventType;
    private String category;
    private LocalDateTime endDate;
    private String location;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal price;
    private Boolean isFree;
    private List<String> images;
    private EventStatus status;
    private Long siteId;
    private String siteName;
    private Long organizerId;
    private String organizerName;
    private Long organizerUserId;
    private Integer likesCount;
    private Integer dislikesCount;
    private BigDecimal rating;
    private LocalDateTime createdAt;
}
