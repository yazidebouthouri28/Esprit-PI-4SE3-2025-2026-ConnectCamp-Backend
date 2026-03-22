package tn.esprit.projetintegre.repositorynadine;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.PromotionUsage;

import java.util.List;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {
    @EntityGraph(attributePaths = {"promotion", "order"})
    List<PromotionUsage> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "order"})
    List<PromotionUsage> findByPromotionId(Long promotionId);

    int countByPromotionIdAndUserId(Long promotionId, Long userId);

    boolean existsByPromotionIdAndOrderId(Long promotionId, Long orderId);

}
