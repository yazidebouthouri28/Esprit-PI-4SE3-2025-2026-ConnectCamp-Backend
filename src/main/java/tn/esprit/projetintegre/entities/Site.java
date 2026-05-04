package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "sites", indexes = {
        @Index(name = "idx_site_city", columnList = "city"),
        @Index(name = "idx_site_active", columnList = "isActive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Site name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    @Size(max = 50)
    private String type; // BEACH, MOUNTAIN, FOREST, DESERT

    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String country;
    private String postalCode;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @ElementCollection
    @CollectionTable(name = "site_images", joinColumns = @JoinColumn(name = "site_id"))
    @Lob
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String thumbnail;

    @ElementCollection
    @CollectionTable(name = "site_amenities", joinColumns = @JoinColumn(name = "site_id"))
    @Column(name = "amenity")
    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Builder.Default
    private Boolean isActive = true;

    /** Platform verification badge (e.g. after certification approved). */
    @Builder.Default
    private Boolean verified = false;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Price must be positive")
    private BigDecimal pricePerNight;

    @Column(precision = 3, scale = 2)
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private BigDecimal averageRating;

    @Builder.Default
    private Integer reviewCount = 0;

    private String contactPhone;
    private String contactEmail;
    private String website;

    private String checkInTime;
    private String checkOutTime;
    @Column(length = 1000)
    private String houseRules;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VirtualTour> virtualTours = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RouteGuide> routeGuides = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CampHighlight> campHighlights = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    @Column(updatable = false)
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
