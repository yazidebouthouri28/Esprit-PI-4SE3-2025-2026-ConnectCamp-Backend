// PromotionService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Promotion;
import tn.esprit.projetintegre.entities.PromotionUsage;
import tn.esprit.projetintegre.nadineentities.Order;

import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.repositorynadine.PromotionRepository;
import tn.esprit.projetintegre.repositorynadine.PromotionUsageRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;

    public List<Promotion> getActivePromotions() {
        return promotionRepository.findAllValid(LocalDateTime.now());
    }

    public BigDecimal calculateDiscount(Promotion promotion, BigDecimal amount) {
        return switch (promotion.getType()) {
            case PERCENTAGE -> amount.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
            case FIXED_AMOUNT -> promotion.getDiscountValue();
            default -> BigDecimal.ZERO;
        };
    }

    @Transactional
    public void trackUsage(Promotion promotion, User user, Order order,
                           BigDecimal discount, BigDecimal original) {
        promotion.setCurrentUsage(promotion.getCurrentUsage() + 1);
        promotionRepository.save(promotion);

        promotionUsageRepository.save(PromotionUsage.builder()
                .promotion(promotion)
                .user(user)
                .order(order)
                .discountAmount(discount)
                .originalAmount(original)
                .finalAmount(original.subtract(discount))
                .build());
    }

    @Transactional
    public Promotion create(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Transactional
    public void deactivate(Long id) {
        Promotion p = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
        p.setIsActive(false);
        promotionRepository.save(p);
    }
}