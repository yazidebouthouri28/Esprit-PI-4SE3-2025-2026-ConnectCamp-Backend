package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.projetintegre.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_status", columnList = "status"),
        @Index(name = "idx_event_organizer", columnList = "organizer_id"),
        @Index(name = "idx_event_site", columnList = "site_id"),
        @Index(name = "idx_event_dates", columnList = "endDate"),
        @Index(name = "idx_event_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères")
    private String name;

    @Size(max = 500, message = "L'URL de la photo ne peut pas dépasser 500 caractères")
    private String picture;

    @Column(length = 2000)
    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_id")
    @NotNull(message = "L'organisateur est obligatoire")
    private Organizer organizer;

    @Column(name = "event_type")
    @NotBlank(message = "Le type d'événement est obligatoire")
    private String eventType;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 100, message = "La catégorie ne peut pas dépasser 100 caractères")
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    private LocalDateTime registrationDeadline;

    @Min(value = 1, message = "Le nombre maximum de participants doit être au moins 1")
    @Max(value = 100000, message = "Le nombre maximum de participants ne peut pas dépasser 100000")
    private Integer maxParticipants;

    @Min(value = 0, message = "Le nombre de participants ne peut pas être négatif")
    @Builder.Default
    private Integer currentParticipants = 0;

    @DecimalMin(value = "0.00", message = "Le prix ne peut pas être négatif")
    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Builder.Default
    private Boolean isFree = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Size(max = 500, message = "L'URL de la miniature ne peut pas dépasser 500 caractères")
    private String thumbnail;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 500, message = "Le lieu ne peut pas dépasser 500 caractères")
    private String location;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    @DecimalMin(value = "0.00", message = "La note ne peut pas être négative")
    @DecimalMax(value = "5.00", message = "La note ne peut pas dépasser 5")
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Min(value = 0, message = "Le nombre d'avis ne peut pas être négatif")
    @Builder.Default
    private Integer reviewCount = 0;

    @Min(value = 0, message = "Le nombre de likes ne peut pas être négatif")
    @Builder.Default
    private Integer likesCount = 0;

    @Min(value = 0, message = "Le nombre de dislikes ne peut pas être négatif")
    @Builder.Default
    private Integer dislikesCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null)
            status = EventStatus.DRAFT;
        if (currentParticipants == null)
            currentParticipants = 0;
        if (reviewCount == null)
            reviewCount = 0;
        if (likesCount == null)
            likesCount = 0;
        if (dislikesCount == null)
            dislikesCount = 0;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(Integer dislikesCount) {
        this.dislikesCount = dislikesCount;
    }
}
