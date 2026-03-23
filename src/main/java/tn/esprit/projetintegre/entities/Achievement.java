package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'achievement name est obligatoire")
    private String name;

    @Column(length = 500)
    private String description;

    private String badge;
    private String icon;

    @PositiveOrZero(message = "Les points requis ne peuvent pas être négatifs")
    private Integer requiredPoints;
    @PositiveOrZero(message = "Les points de récompense ne peuvent pas être négatifs")
    private Integer rewardPoints;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category; // SHOPPING, EVENTS, SOCIAL, LOYALTY
    private Integer level; //

    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
