package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badge_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Badge badge;

    @Column(nullable = false)
    private Integer numero;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String regle;
}
