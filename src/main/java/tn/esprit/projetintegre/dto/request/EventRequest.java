package tn.esprit.projetintegre.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.projetintegre.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères")
    private String name;

    @Size(max = 500, message = "L'URL de la photo ne peut pas dépasser 500 caractères")
    private String picture;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @NotBlank(message = "Le type d'événement est obligatoire")
    @Size(max = 100, message = "Le type d'événement ne peut pas dépasser 100 caractères")
    private String eventType;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 100, message = "La catégorie ne peut pas dépasser 100 caractères")
    private String category;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 500, message = "Le lieu ne peut pas dépasser 500 caractères")
    private String location;

    private Boolean isFree;

    @Size(max = 20, message = "Maximum 20 images autorisées")
    private List<String> images;

    @Size(max = 500, message = "L'URL de la miniature ne peut pas dépasser 500 caractères")
    private String thumbnail;

    private Long siteId;

    @NotNull(message = "L'identifiant de l'organisateur est obligatoire")
    private Long organizerId;

    private EventStatus status;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;

    private java.math.BigDecimal price;
}
