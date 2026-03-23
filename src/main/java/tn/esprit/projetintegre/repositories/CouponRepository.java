package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Coupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Override
    @EntityGraph(attributePaths = {}) // Aucune relation spécifique à charger pour Coupon
    Optional<Coupon> findById(Long id);

    @EntityGraph(attributePaths = {})
    Optional<Coupon> findByCode(String code);

    @EntityGraph(attributePaths = {})
    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {})
    List<Coupon> findByIsActiveTrue();

    @EntityGraph(attributePaths = {})
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND (c.validFrom IS NULL OR c.validFrom <= :now) AND (c.validUntil IS NULL OR c.validUntil >= :now)")
    List<Coupon> findValidCoupons(LocalDateTime now);
}