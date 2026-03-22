// CouponService.java
package tn.esprit.projetintegre.servicenadine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.nadineentities.Coupon;
import tn.esprit.projetintegre.nadineentities.CouponUsage;
import tn.esprit.projetintegre.nadineentities.Order;
import tn.esprit.projetintegre.nadineentities.User;
import tn.esprit.projetintegre.repositorynadine.CouponRepository;
import tn.esprit.projetintegre.repositorynadine.CouponUsageRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    public Coupon validate(String code, User user, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon introuvable"));

        if (!coupon.isValid())
            throw new RuntimeException("Coupon expiré ou inactif");

        if (coupon.getMinOrderAmount() != null
                && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0)
            throw new RuntimeException("Montant minimum non atteint");

        if (coupon.getUsageLimitPerUser() != null) {
            int used = couponUsageRepository.countByCouponIdAndUserId(
                    coupon.getId(), user.getId());
            if (used >= coupon.getUsageLimitPerUser())
                throw new RuntimeException("Limite d'utilisation atteinte");
        }

        if (coupon.getIsFirstOrderOnly()) {
            boolean hasOrders = !couponUsageRepository
                    .findByUserId(user.getId()).isEmpty();
            if (hasOrders)
                throw new RuntimeException("Coupon réservé à la première commande");
        }

        return coupon;
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount = switch (coupon.getType()) {
            case PERCENTAGE -> orderAmount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
            case FIXED_AMOUNT -> coupon.getDiscountValue();
            default -> BigDecimal.ZERO;
        };

        if (coupon.getMaxDiscountAmount() != null
                && discount.compareTo(coupon.getMaxDiscountAmount()) > 0)
            discount = coupon.getMaxDiscountAmount();

        return discount;
    }

    @Transactional
    public void trackUsage(Coupon coupon, User user, Order order,
                           BigDecimal discountAmount, BigDecimal orderAmount) {
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        couponUsageRepository.save(CouponUsage.builder()
                .coupon(coupon)
                .user(user)
                .order(order)
                .discountAmount(discountAmount)
                .orderAmount(orderAmount)
                .build());
    }

    public List<Coupon> getActiveCoupons() {
        return couponRepository.findByIsActiveTrue();
    }

    @Transactional
    public void deactivate(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon introuvable"));
        coupon.setIsActive(false);
        couponRepository.save(coupon);
    }
}