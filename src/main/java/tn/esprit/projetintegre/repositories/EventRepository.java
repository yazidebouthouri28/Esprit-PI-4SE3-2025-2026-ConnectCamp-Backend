package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.enums.EventStatus;

import tn.esprit.projetintegre.dto.response.EventRevenueDTO;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

        @Override
        @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos", "badges" })
        List<Event> findAll();

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
        @Query("SELECT DISTINCT e FROM Event e " +
                        "LEFT JOIN e.badges b " +
                        "LEFT JOIN e.organizer o " +
                        "LEFT JOIN o.user u " +
                        "LEFT JOIN e.site s " +
                        "WHERE (e.status = 'PUBLISHED' OR e.status = 'COMPLETED') " +
                        "AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Event> searchEvents(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT new tn.esprit.projetintegre.dto.response.EventRevenueDTO(e.title, u.username, SUM(tr.totalPrice), SUM(tr.quantity)) "
                        +
                        "FROM Event e JOIN e.organizer org JOIN org.user u JOIN TicketReservation tr ON tr.event = e " +
                        "WHERE tr.status = 'CONFIRMED' " +
                        "GROUP BY e.id, e.title, u.username " +
                        "ORDER BY SUM(tr.totalPrice) DESC")
        List<EventRevenueDTO> getRevenueData();

        @Query("SELECT new tn.esprit.projetintegre.dto.response.EventRevenueDTO(e.title, u.username, SUM(tr.totalPrice), SUM(tr.quantity)) "
                        +
                        "FROM Event e JOIN e.organizer org JOIN org.user u JOIN TicketReservation tr ON tr.event = e " +
                        "WHERE tr.status = 'CONFIRMED' AND org.id = :organizerId " +
                        "GROUP BY e.id, e.title, u.username " +
                        "ORDER BY SUM(tr.totalPrice) DESC")
        List<EventRevenueDTO> getRevenueDataByOrganizerId(@Param("organizerId") Long organizerId);

        @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos" })
        List<Event> findByIsPublicTrue();

        @Query("SELECT SUM(e.viewCount) FROM Event e WHERE e.organizer.id = :organizerId")
        Long sumViewCountByOrganizerId(@Param("organizerId") Long organizerId);

        @Query("SELECT SUM(e.viewCount) FROM Event e")
        Long sumAllViewCounts();

        @Modifying
        @Query("UPDATE Event e SET e.rating = :rating WHERE e.id = :eventId")
        void updateRating(@Param("eventId") Long eventId, @Param("rating") BigDecimal rating);

        @Modifying
        @Query("UPDATE Event e SET e.currentParticipants = e.currentParticipants + :quantity WHERE e.id = :eventId")
        int incrementCurrentParticipants(@Param("eventId") Long eventId, @Param("quantity") int quantity);

        @Modifying
        @Query("UPDATE Event e SET e.viewCount = COALESCE(e.viewCount, 0) + 1 WHERE e.id = :eventId")
        int incrementViewCount(@Param("eventId") Long eventId);

        @EntityGraph(attributePaths = { "site", "organizer", "eventPhotos", "badges" })
        @Query("SELECT DISTINCT e FROM Event e WHERE e.status = 'PUBLISHED' " +
                        "AND e.id NOT IN :excludeIds " +
                        "AND (e.category IN :categories OR e.organizer.id IN :organizerIds) " +
                        "AND (e.maxParticipants IS NULL OR e.currentParticipants < e.maxParticipants) " +
                        "ORDER BY e.rating DESC NULLS LAST, e.startDate ASC")
        List<Event> findRecommendedEvents(
                        @Param("categories") List<String> categories,
                        @Param("organizerIds") List<Long> organizerIds,
                        @Param("excludeIds") List<Long> excludeIds,
                        Pageable pageable);
}
