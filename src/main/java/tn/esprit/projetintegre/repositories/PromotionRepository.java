package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Promotion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Override
    @EntityGraph(attributePaths = {}) // Aucune relation complexe à charger pour l'entité Promotion
    Optional<Promotion> findById(Long id);

    @EntityGraph(attributePaths = {})
    List<Promotion> findByIsActiveTrue();

    @EntityGraph(attributePaths = {})
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND (p.startDate IS NULL OR p.startDate <= :now) AND (p.endDate IS NULL OR p.endDate >= :now)")
    List<Promotion> findActivePromotions(LocalDateTime now);
}