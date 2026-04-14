package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.response.SponsorResponse;
import tn.esprit.projetintegre.entities.Sponsor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SponsorRepository extends JpaRepository<Sponsor, Long> {

    Optional<Sponsor> findById(Long id);

    Optional<Sponsor> findByEmail(String email);

    List<Sponsor> findByIsActiveTrue();

    List<Sponsor> findByCity(String city);

    List<Sponsor> findByCountry(String country);

    @Query("SELECT s FROM Sponsor s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Sponsor> searchByKeyword(String keyword);

    // =========================================================================
    // JPQL 3-TABLE JOIN: Sponsor, Event, Site
    // Cross-joins Sponsor and Event, navigates Event.site, matches by city
    // =========================================================================
    @Query("SELECT new tn.esprit.projetintegre.dto.response.SponsorSiteEventDTO(s.name, s.city, e.title, si.name, si.city) " +
           "FROM Sponsor s, Event e JOIN e.site si " +
           "WHERE s.isActive = true AND si.city = s.city")
    List<tn.esprit.projetintegre.dto.response.SponsorSiteEventDTO> getSponsorsMatchedToLocalEvents();

    boolean existsByEmail(String email);

    // =========================================================================
    // SCHEDULER – Bulk update queries (JPQL, no native SQL)
    // =========================================================================
    @Modifying
    @Transactional
    @Query("UPDATE Sponsor s SET s.tier = 'SILVER' WHERE s.tier = 'BRONZE' AND s.createdAt <= :threshold")
    int upgradeBronzeToSilver(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Transactional
    @Query("UPDATE Sponsor s SET s.tier = 'GOLD' WHERE s.tier = 'SILVER' AND s.createdAt <= :threshold")
    int upgradeSilverToGold(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Transactional
    @Query("UPDATE Sponsor s SET s.isActive = false, s.notes = 'System suspended due to incomplete profile' WHERE s.phone IS NULL AND s.website IS NULL AND s.createdAt < :threshold")
    int suspendIncompleteSponsors(@Param("threshold") LocalDateTime threshold);

    // =========================================================================
    // KEYWORDS METHOD – involves two tables (Sponsor and Sponsorship)
    // =========================================================================
    // Finds all active sponsors that have at least one sponsorship with amount greater than given value.
    // Spring Data translates this into a JOIN between sponsor and sponsorships.
    List<Sponsor> findByIsActiveTrueAndSponsorships_AmountGreaterThan(Double amount);
}