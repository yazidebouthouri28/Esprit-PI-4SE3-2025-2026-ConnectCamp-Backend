package tn.esprit.projetintegre.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeRequest {
    private String name;
    private String icon;
    private Long medalId;
}
