package tn.esprit.projetintegre.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedalResponse {
    private Long id;
    private String name;
    private String icon;
    private String type;
}
