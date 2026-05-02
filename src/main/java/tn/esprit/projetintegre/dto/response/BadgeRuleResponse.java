package tn.esprit.projetintegre.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeRuleResponse {
    private Long id;
    private Integer numero;
    private String regle;
}
