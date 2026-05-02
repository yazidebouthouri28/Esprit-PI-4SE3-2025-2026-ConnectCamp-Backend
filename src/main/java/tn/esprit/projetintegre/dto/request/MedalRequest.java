package tn.esprit.projetintegre.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedalRequest {
    private String name;
    private String icon;
    private String type;
}
