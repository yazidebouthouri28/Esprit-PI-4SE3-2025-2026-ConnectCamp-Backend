package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_medals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMedal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medal_id", nullable = false)
    private Medal medal;

    @Column(name = "earned_at")
    private java.time.LocalDateTime earnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @PrePersist
    protected void onEarned() {
        if (earnedAt == null) {
            earnedAt = java.time.LocalDateTime.now();
        }
    }
}
