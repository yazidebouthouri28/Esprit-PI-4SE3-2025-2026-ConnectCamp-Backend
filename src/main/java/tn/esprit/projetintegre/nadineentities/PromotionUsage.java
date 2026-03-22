package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.projetintegre.nadineentities.Order;
import tn.esprit.projetintegre.nadineentities.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_usages", indexes = {
        @Index(name = "idx_promo_usage_user", columnList = "user_id"),
        @Index(name = "idx_promo_usage_promo", columnList = "promotion_id"),
        @Index(name = "idx_promo_usage_order", columnList = "order_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"promotion_id", "order_id"}, name = "uk_promo_order")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal originalAmount;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal finalAmount;

    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private tn.esprit.projetintegre.entities.Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (usedAt == null) usedAt = LocalDateTime.now();
    }
}