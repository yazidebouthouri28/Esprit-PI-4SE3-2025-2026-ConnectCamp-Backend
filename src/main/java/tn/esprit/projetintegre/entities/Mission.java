package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import tn.esprit.projetintegre.enums.MissionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mission name is required")
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private MissionType type;

    @Positive(message = "La valeur cible doit être supérieure à 0")
    private Integer targetValue; // ex: buy 5 products, attend 3 events
    @PositiveOrZero(message = "Les points de récompense ne peuvent pas être négatifs")
    private Integer rewardPoints;

    private String rewardBadge;
    private String rewardDescription;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean isActive = true;
    private Boolean isRepeatable = false;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category; // SHOPPING, EVENTS, SOCIAL, etc.

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
