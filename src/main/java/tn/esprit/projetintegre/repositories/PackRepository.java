package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Pack;
import tn.esprit.projetintegre.enums.PackType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import tn.esprit.projetintegre.repositories.projections.PackProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackRepository extends JpaRepository<Pack, Long> {

    @Override
    @EntityGraph(attributePaths = { "site" }) // Charge les relations nécessaires
    Optional<Pack> findById(Long id);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE p.id = :id")
    Optional<PackProjection> findByIdProjected(@Param("id") Long id);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE p.isActive = true")
    Page<PackProjection> findByIsActiveTrueProjected(Pageable pageable);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s")
    Page<PackProjection> findAllProjected(Pageable pageable);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE p.packType = :type AND p.isActive = true")
    Page<PackProjection> findByPackTypeProjected(@Param("type") PackType type, Pageable pageable);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE s.id = :siteId AND p.isActive = true")
    Page<PackProjection> findBySiteIdProjected(@Param("siteId") Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true AND p.isFeatured = true")
    List<Pack> findFeaturedPacks();

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Pack> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true AND p.isLimitedOffer = true AND p.validUntil > :now")
    List<Pack> findActiveLimitedOffers(@Param("now") LocalDateTime now);

    @Query("SELECT p.id as id, p.name as name, p.description as description, p.packType as packType, " +
           "p.price as price, p.originalPrice as originalPrice, " +
           "p.durationDays as durationDays, p.maxPersons as maxPersons, p.isActive as isActive, " +
           "p.isFeatured as isFeatured, p.isLimitedOffer as isLimitedOffer, " +
           "p.availableQuantity as availableQuantity, p.soldCount as soldCount, " +
           "p.rating as rating, p.reviewCount as reviewCount, p.validFrom as validFrom, " +
           "p.validUntil as validUntil, p.createdAt as createdAt, " +
           "p.imageUrl as imageUrl, size(p.services) as serviceCount, " +
           "s.id as siteId, s.name as siteName " +
           "FROM Pack p LEFT JOIN p.site s WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PackProjection> searchPacksProjected(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true AND p.maxPersons >= :persons")
    Page<Pack> findByMinPersons(@Param("persons") Integer persons, Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true ORDER BY p.soldCount DESC")
    List<Pack> findTopSellingPacks(Pageable pageable);

    @EntityGraph(attributePaths = { "site" })
    @Query("SELECT p FROM Pack p WHERE p.isActive = true ORDER BY p.rating DESC")
    List<Pack> findTopRatedPacks(Pageable pageable);
}