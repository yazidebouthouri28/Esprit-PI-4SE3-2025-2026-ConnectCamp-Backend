package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "medals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String icon;
    private String type;

    @OneToMany(mappedBy = "medal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Badge> badges = new HashSet<>();
}
