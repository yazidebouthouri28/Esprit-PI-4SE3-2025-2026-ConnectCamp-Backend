package tn.esprit.projetintegre.repositorynadine;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.nadineentities.CouponUsage;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    @EntityGraph(attributePaths = {"coupon", "user", "order"})
    List<CouponUsage> findByCouponId(Long couponId);

    @EntityGraph(attributePaths = {"coupon", "order"})
    List<CouponUsage> findByUserId(Long userId);

    int countByCouponIdAndUserId(Long couponId, Long userId);

    boolean existsByCouponIdAndOrderId(Long couponId, Long orderId);

}
