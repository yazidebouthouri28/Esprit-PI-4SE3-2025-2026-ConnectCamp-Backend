package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.projetintegre.enums.PromotionType;
import tn.esprit.projetintegre.nadineentities.PromotionUsage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la promotion est obligatoire")
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private PromotionType type;

    @Column(precision = 15, scale = 2)
    private BigDecimal discountValue;

    // Conditions (depuis ConditionPromotion — les essentielles)
    @Column(precision = 15, scale = 2)
    private BigDecimal minPurchaseAmount;      // = valeurMinimale

    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Min(value = 0)
    private Integer minQuantity;               // = quantiteMinimale

    @Builder.Default
    private Boolean firstOrderOnly = false;    // = premiereCommande

    private Integer usageLimitPerUser;         // nouvelle — limite par utilisateur

    // Usage global
    private Integer maxUsage;
    @Builder.Default
    private Integer currentUsage = 0;

    // Ciblage produits / catégories
    @ElementCollection
    @CollectionTable(name = "promotion_applicable_products",
            joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "product_id")
    @Builder.Default
    private List<Long> applicableProductIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "promotion_applicable_categories",
            joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "category_id")
    @Builder.Default
    private List<Long> applicableCategoryIds = new ArrayList<>();

    private String targetAudience;             // ALL, NEW_USERS, VIP...

    // Validité
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder.Default
    private Boolean isActive = true;

    // Usages (depuis PromotionUsage — table séparée liée)
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PromotionUsage> usages = new ArrayList<>();

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

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
                (startDate == null || now.isAfter(startDate)) &&
                (endDate == null || now.isBefore(endDate)) &&
                (maxUsage == null || currentUsage < maxUsage);
    }
}