package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String icon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medal_id")
    private Medal medal;
}
