package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Sponsorship;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SponsorshipRepository extends JpaRepository<Sponsorship, Long> {

    @Override
    @EntityGraph(attributePaths = { "sponsor", "event" })
    Optional<Sponsorship> findById(Long id);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    List<Sponsorship> findBySponsor_Id(Long sponsorId);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    List<Sponsorship> findBySponsor_IdAndStatus(Long sponsorId, String status);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    List<Sponsorship> findByEvent_Id(Long eventId);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    List<Sponsorship> findByStatus(String status);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    @Query("SELECT s FROM Sponsorship s LEFT JOIN FETCH s.sponsor LEFT JOIN FETCH s.event")
    Page<Sponsorship> findAllWithDetails(Pageable pageable);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    Page<Sponsorship> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    List<Sponsorship> findByIsActiveTrue();

    @EntityGraph(attributePaths = { "sponsor", "event" })
    List<Sponsorship> findByIsPaidFalse();

    @EntityGraph(attributePaths = { "sponsor", "event" })
    @Query("SELECT s FROM Sponsorship s WHERE s.startDate <= :date AND s.endDate >= :date AND s.isActive = true")
    List<Sponsorship> findActiveOnDate(@Param("date") LocalDate date);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    @Query("SELECT SUM(s.amount) FROM Sponsorship s WHERE s.event.id = :eventId AND s.isPaid = true")
    java.math.BigDecimal getTotalPaidAmountByEventId(@Param("eventId") Long eventId);

    @EntityGraph(attributePaths = { "sponsor", "event" })
    @Query("SELECT s FROM Sponsorship s LEFT JOIN FETCH s.sponsor LEFT JOIN FETCH s.event WHERE s.id = :id")
    Optional<Sponsorship> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM Sponsorship s LEFT JOIN FETCH s.sponsor sp LEFT JOIN FETCH s.event e WHERE sp.id = :sponsorId")
    List<Sponsorship> findBySponsorIdWithEvent(@Param("sponsorId") Long sponsorId);
}