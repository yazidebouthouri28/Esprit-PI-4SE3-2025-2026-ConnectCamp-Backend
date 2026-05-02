package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.dto.response.EventRevenueDTO;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Override
    @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos", "badges" })
    Optional<Event> findById(Long id);

    @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos", "badges" })
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos", "badges" })
    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

    @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos", "badges" })
    Page<Event> findBySiteId(Long siteId, Pageable pageable);

    @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos", "badges" })
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND e.startDate > :now ORDER BY e.startDate")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos", "badges" })
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Event> searchEvents(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos" })
    List<Event> findByIsPublicTrue();

    @Query("SELECT DISTINCT e FROM Event e " +
            "LEFT JOIN FETCH e.site s " +
            "LEFT JOIN FETCH e.organizer " +
            "LEFT JOIN FETCH e.eventPhotos " +
            "LEFT JOIN FETCH e.badges")
    List<Event> findAllWithDetails();

    @Query("SELECT SUM(e.viewCount) FROM Event e WHERE e.organizer.id = :organizerId")
    Long sumViewCountByOrganizerId(@Param("organizerId") Long organizerId);

    @Query("SELECT SUM(e.viewCount) FROM Event e")
    Long sumAllViewCounts();

    @Query("SELECT new tn.esprit.projetintegre.dto.response.EventRevenueDTO(e.title, u.username, SUM(tr.totalPrice), SUM(tr.quantity)) "
            + "FROM Event e JOIN e.organizer org JOIN org.user u JOIN TicketReservation tr ON tr.event = e "
            + "WHERE tr.status = 'CONFIRMED' "
            + "GROUP BY e.id, e.title, u.username "
            + "ORDER BY SUM(tr.totalPrice) DESC")
    List<EventRevenueDTO> getRevenueData();

    @Query("SELECT new tn.esprit.projetintegre.dto.response.EventRevenueDTO(e.title, u.username, SUM(tr.totalPrice), SUM(tr.quantity)) "
            + "FROM Event e JOIN e.organizer org JOIN org.user u JOIN TicketReservation tr ON tr.event = e "
            + "WHERE tr.status = 'CONFIRMED' AND org.id = :organizerId "
            + "GROUP BY e.id, e.title, u.username "
            + "ORDER BY SUM(tr.totalPrice) DESC")
    List<EventRevenueDTO> getRevenueDataByOrganizerId(@Param("organizerId") Long organizerId);

    @Modifying
    @Query("UPDATE Event e SET e.rating = :rating WHERE e.id = :eventId")
    int updateRating(@Param("eventId") Long eventId, @Param("rating") BigDecimal rating);

    @Modifying
    @Query("UPDATE Event e SET e.currentParticipants = COALESCE(e.currentParticipants, 0) + :quantity WHERE e.id = :eventId")
    int incrementCurrentParticipants(@Param("eventId") Long eventId, @Param("quantity") int quantity);
}
