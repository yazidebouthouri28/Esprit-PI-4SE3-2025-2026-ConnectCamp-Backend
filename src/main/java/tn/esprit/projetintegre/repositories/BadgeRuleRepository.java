package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.BadgeRule;

import java.util.List;

@Repository
public interface BadgeRuleRepository extends JpaRepository<BadgeRule, Long> {
    List<BadgeRule> findByBadgeId(Long badgeId);
}
