package tn.esprit.projetintegre.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeResponse {
    private Long id;
    private String name;
    private String icon;
    private Long medalId;
    private String medalName;
    private List<BadgeRuleResponse> rules;
}
