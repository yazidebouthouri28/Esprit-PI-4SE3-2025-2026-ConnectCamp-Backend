package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.projetintegre.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    private PromotionType type = PromotionType.PERCENTAGE;

    @Column(precision = 15, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    private Integer usageLimit;
    private Integer usageCount = 0;
    private Integer usageLimitPerUser = 1;

    private Boolean isActive = true;
    private Boolean isFirstOrderOnly = false;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category applicableCategory;

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
               (validFrom == null || now.isAfter(validFrom)) && 
               (validUntil == null || now.isBefore(validUntil)) &&
               (usageLimit == null || usageCount < usageLimit);
    }
}
