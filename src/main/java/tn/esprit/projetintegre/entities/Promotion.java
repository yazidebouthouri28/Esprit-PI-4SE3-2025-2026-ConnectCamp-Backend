package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import tn.esprit.projetintegre.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Promotion name is required")
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private PromotionType type;

    @Column(precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 15, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    private Integer maxUsage;
    private Integer currentUsage = 0;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "promotion_applicable_products", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "product_id")
    private List<Long> applicableProductIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "promotion_applicable_categories", joinColumns = @JoinColumn(name = "promotion_id"))
    @Column(name = "category_id")
    private List<Long> applicableCategoryIds = new ArrayList<>();

    private String targetAudience; // ALL, NEW_USERS, VIP, etc.

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
