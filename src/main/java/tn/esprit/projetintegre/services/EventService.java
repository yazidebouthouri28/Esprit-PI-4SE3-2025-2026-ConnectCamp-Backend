package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.projetintegre.dto.response.EventRevenueDTO;
import tn.esprit.projetintegre.dto.response.ParticipantResponseDTO;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Organizer;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.EventStatus;
import tn.esprit.projetintegre.exception.BusinessException;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.OrganizerRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.repositories.BadgeRepository;
import tn.esprit.projetintegre.repositories.UserBadgeRepository;
import tn.esprit.projetintegre.repositories.ParticipantRepository;
import tn.esprit.projetintegre.repositories.EventCommentRepository;
import tn.esprit.projetintegre.repositories.TicketRepository;
import tn.esprit.projetintegre.repositories.TicketReservationRepository;
import tn.esprit.projetintegre.repositories.TicketRequestRepository;
import tn.esprit.projetintegre.repositories.TimeSlotRepository;
import tn.esprit.projetintegre.repositories.EventServiceEntityRepository;
import tn.esprit.projetintegre.repositories.EventScheduleItemRepository;
import tn.esprit.projetintegre.repositories.EventInteractionRepository;
import tn.esprit.projetintegre.repositories.EventPhotoRepository;
import tn.esprit.projetintegre.entities.Badge;
import tn.esprit.projetintegre.entities.TicketReservation;
import tn.esprit.projetintegre.entities.EventInteraction;
import tn.esprit.projetintegre.entities.EventComment;
import tn.esprit.projetintegre.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {
    private static final int RECOMMENDATION_LIMIT = 8;

    private final OrganizerRepository organizerRepository;
    private final EventRepository eventRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final ParticipantRepository participantRepository;
    private final EventCommentRepository eventCommentRepository;
    private final TicketRepository ticketRepository;
    private final TicketReservationRepository ticketReservationRepository;
    private final TicketRequestRepository ticketRequestRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final EventServiceEntityRepository eventServiceEntityRepository;
    private final EventScheduleItemRepository eventScheduleItemRepository;
    private final EventInteractionRepository eventInteractionRepository;
    private final EventPhotoRepository eventPhotoRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Page<Event> getEventsByStatus(EventStatus status, Pageable pageable) {
        return eventRepository.findByStatus(status, pageable);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    public List<Event> getUpcomingEvents(int limit) {
        return eventRepository.findUpcomingEvents(LocalDateTime.now(), PageRequest.of(0, limit));
    }

    public Page<Event> searchEvents(String keyword, Pageable pageable) {
        return eventRepository.searchEvents(keyword, pageable);
    }

    public Page<Event> getEventsByOrganizer(Long organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerId(organizerId, pageable);
    }

    public Page<Event> getEventsBySite(Long siteId, Pageable pageable) {
        return eventRepository.findBySiteId(siteId, pageable);
    }

    @Transactional
    public Event createEvent(Event event, Long siteId, Long organizerId, List<Long> gamificationIds,
            Authentication authentication) {
        if (event.getStartDate() != null && event.getStartDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("La date de début doit être dans le futur pour un nouvel événement");
        }

        if (siteId != null) {
            Site site = siteRepository.findById(siteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found"));
            event.setSite(site);
        }

        String authenticatedUsername = authentication != null ? authentication.getName() : null;
        Organizer organizer;
        if (isAdmin(authentication)) {
            organizer = resolveOrganizer(organizerId, authenticatedUsername);
        } else {
            Organizer self = resolveOrganizer(null, authenticatedUsername);
            if (organizerId != null && organizerId > 0 && !self.getId().equals(organizerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only create events as yourself");
            }
            organizer = self;
        }
        event.setOrganizer(organizer);
        if (event.getStatus() == null) {
            event.setStatus(isAdmin(authentication) ? EventStatus.DRAFT : EventStatus.PUBLISHED);
        }

        if (gamificationIds != null && !gamificationIds.isEmpty()) {
            java.util.List<Badge> badges = badgeRepository.findAllById(gamificationIds);
            event.setBadges(new java.util.HashSet<>(badges));
        }

        return eventRepository.save(event);
    }

    private Organizer resolveOrganizer(Long organizerId, String authenticatedUsername) {
        // If frontend passes an organizerId but it's invalid/not found, fall back to
        // authenticated user.
        if (organizerId != null && organizerId > 0) {
            var byId = organizerRepository.findById(organizerId);
            if (byId.isPresent()) {
                return byId.get();
            }
        }

        if (authenticatedUsername == null || authenticatedUsername.isBlank()) {
            throw new ResourceNotFoundException("Organizer not found");
        }

        User user = userRepository.findByUsername(authenticatedUsername)
                .or(() -> userRepository.findByEmail(authenticatedUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        return organizerRepository.findByUser_Id(user.getId())
                .orElseGet(() -> organizerRepository.save(Organizer.builder()
                        .user(user)
                        .companyName(user.getName() + "'s Organization")
                        .verified(true)
                        .active(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }

    public void assertCanAccessOrganizerScope(Long organizerId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }
        Organizer self = resolveOrganizer(null, authentication != null ? authentication.getName() : null);
        if (!self.getId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own organizer data");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role == null ? "" : role.toUpperCase())
                .anyMatch(role -> role.contains("ADMIN"));
    }

    private void assertOrganizerOwnsEvent(Event event, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        User user = userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found"));
        Organizer org = organizerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Organizer profile required"));
        if (event.getOrganizer() == null || !event.getOrganizer().getId().equals(org.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own events");
        }
    }

    @Transactional
    public Event updateEvent(Long id, Event eventDetails, List<Long> gamificationIds, Authentication authentication) {
        Event event = getEventById(id);
        assertOrganizerOwnsEvent(event, authentication);

        if (eventDetails.getTitle() != null)
            event.setTitle(eventDetails.getTitle());
        if (eventDetails.getDescription() != null)
            event.setDescription(eventDetails.getDescription());
        if (eventDetails.getEventType() != null)
            event.setEventType(eventDetails.getEventType());
        if (eventDetails.getCategory() != null)
            event.setCategory(eventDetails.getCategory());
        if (eventDetails.getStartDate() != null)
            event.setStartDate(eventDetails.getStartDate());
        if (eventDetails.getEndDate() != null)
            event.setEndDate(eventDetails.getEndDate());
        if (eventDetails.getMaxParticipants() != null)
            event.setMaxParticipants(eventDetails.getMaxParticipants());
        if (eventDetails.getPrice() != null)
            event.setPrice(eventDetails.getPrice());
        if (eventDetails.getIsFree() != null)
            event.setIsFree(eventDetails.getIsFree());
        if (eventDetails.getImages() != null)
            event.setImages(eventDetails.getImages());
        if (eventDetails.getLocation() != null)
            event.setLocation(eventDetails.getLocation());
        if (eventDetails.getStatus() != null)
            event.setStatus(eventDetails.getStatus());

        if (gamificationIds != null) {
            java.util.List<Badge> badges = badgeRepository.findAllById(gamificationIds);
            event.setBadges(new java.util.HashSet<>(badges));
        }

        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEventStatus(Long id, EventStatus status, Authentication authentication) {
        Event event = getEventById(id);
        assertOrganizerOwnsEvent(event, authentication);
        event.setStatus(status);

        return eventRepository.save(event);
    }

    @Transactional
    public Event publishEvent(Long id, Authentication authentication) {
        Event event = getEventById(id);
        assertOrganizerOwnsEvent(event, authentication);
        if (event.getStartDate() == null || event.getEndDate() == null) {
            throw new IllegalStateException("Event dates must be set before publishing");
        }
        event.setStatus(EventStatus.PUBLISHED);
        return eventRepository.save(event);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        // Avoid triggering bean-validation on legacy events when only viewCount
        // changes.
        // (Event has an @AssertTrue on registrationDeadline/startDate, which can block
        // reads/refreshes.)
        eventRepository.incrementViewCount(id);
    }

    public List<EventRevenueDTO> getRevenueData(Authentication authentication) {
        if (isAdmin(authentication)) {
            return eventRepository.getRevenueData();
        }
        Organizer org = resolveOrganizer(null, authentication != null ? authentication.getName() : null);
        return eventRepository.getRevenueDataByOrganizerId(org.getId());
    }

    public Event getManageableEvent(Long eventId, Authentication authentication) {
        Event event = getEventById(eventId);
        assertOrganizerOwnsEvent(event, authentication);
        return event;
    }

    public List<ParticipantResponseDTO> getParticipants(Long eventId, Authentication authentication) {
        getManageableEvent(eventId, authentication);

        List<TicketReservation> reservations = ticketReservationRepository.findByEventIdAndStatusIn(eventId,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED));

        return reservations.stream().map(r -> ParticipantResponseDTO.builder()
                .id(r.getId())
                .registrationDate(r.getCreatedAt())
                .status(r.getStatus().name())
                .user(ParticipantResponseDTO.UserInfo.builder()
                        .id(r.getUser().getId())
                        .name(r.getUser().getName())
                        .email(r.getUser().getEmail())
                        .phone(r.getUser().getPhone())
                        .username(r.getUser().getUsername())
                        .build())
                .build()).toList();
    }

    @Transactional
    public void deleteEvent(Long id, Authentication authentication) {
        Event event = getEventById(id);
        assertOrganizerOwnsEvent(event, authentication);
        hardDeleteEvent(event);
    }

    @Transactional
    public void bulkDeleteEvents(List<Long> ids, Authentication authentication) {
        List<Event> events = eventRepository.findAllById(ids);
        for (Event event : events) {
            assertOrganizerOwnsEvent(event, authentication);
            hardDeleteEvent(event);
        }
    }

    public Long getTotalViewsByOrganizer(Long organizerId) {
        return eventRepository.sumViewCountByOrganizerId(organizerId);
    }

    public Long getTotalViewsForAllEvents() {
        return eventRepository.sumAllViewCounts();
    }

    public Integer getLikesCount(Long eventId) {
        return Math.toIntExact(eventInteractionRepository.countByEvent_IdAndLiked(eventId, true));
    }

    public Integer getDislikesCount(Long eventId) {
        return Math.toIntExact(eventInteractionRepository.countByEvent_IdAndDisliked(eventId, true));
    }

    private void hardDeleteEvent(Event event) {
        Long eventId = event.getId();
        // Delete dependent rows first to satisfy FK constraints.
        userBadgeRepository.deleteByEventId(eventId);
        participantRepository.deleteByEventId(eventId);
        eventCommentRepository.deleteByEventId(eventId);
        ticketReservationRepository.deleteByEventId(eventId);
        ticketRequestRepository.deleteByEventId(eventId);
        ticketRepository.deleteByEventId(eventId);
        timeSlotRepository.deleteByEventId(eventId);
        eventServiceEntityRepository.deleteByEventId(eventId);
        eventScheduleItemRepository.deleteByEventId(eventId);
        eventPhotoRepository.deleteByEventId(eventId);
        eventRepository.deleteById(eventId);
    }

    /**
     * Personalized recommendations based on user interactions:
     * - Liked events → same category / same organizer
     * - Highly rated events (≥3 stars) → same category / same organizer
     * - Confirmed/Completed reservations → same category / same organizer
     * Excludes: completed events, full events, events user already interacted with.
     */
    public List<Event> getRecommendedEventsForUser(Long userId) {
        Set<String> favoriteCategories = new HashSet<>();
        Set<Long> favoriteOrganizerIds = new HashSet<>();
        Set<Long> interactedEventIds = new HashSet<>();

        // 1. Liked events
        List<EventInteraction> likes = eventInteractionRepository.findByUser_IdAndLikedTrue(userId);
        for (EventInteraction interaction : likes) {
            Event e = interaction.getEvent();
            interactedEventIds.add(e.getId());
            if (e.getCategory() != null && !e.getCategory().isBlank()) {
                favoriteCategories.add(e.getCategory());
            }
            if (e.getOrganizer() != null) {
                favoriteOrganizerIds.add(e.getOrganizer().getId());
            }
        }

        // 2. Highly rated events (≥ 3 stars)
        List<EventComment> highRatedComments = eventCommentRepository
                .findByUserIdAndRatingGreaterThanEqual(userId, 3);
        for (EventComment comment : highRatedComments) {
            Event e = comment.getEvent();
            interactedEventIds.add(e.getId());
            if (e.getCategory() != null && !e.getCategory().isBlank()) {
                favoriteCategories.add(e.getCategory());
            }
            if (e.getOrganizer() != null) {
                favoriteOrganizerIds.add(e.getOrganizer().getId());
            }
        }

        // 3. Confirmed/Completed reservations (participated)
        List<TicketReservation> reservations = ticketReservationRepository
                .findByUserIdAndStatusIn(userId, List.of(ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED));
        for (TicketReservation reservation : reservations) {
            Event e = reservation.getEvent();
            interactedEventIds.add(e.getId());
            if (e.getCategory() != null && !e.getCategory().isBlank()) {
                favoriteCategories.add(e.getCategory());
            }
            if (e.getOrganizer() != null) {
                favoriteOrganizerIds.add(e.getOrganizer().getId());
            }
        }

        // If no interaction signal, fall back to upcoming events
        if (favoriteCategories.isEmpty() && favoriteOrganizerIds.isEmpty()) {
            return eventRepository.findUpcomingEvents(LocalDateTime.now(),
                    org.springframework.data.domain.PageRequest.of(0, RECOMMENDATION_LIMIT));
        }

        // Ensure non-empty lists for the IN clause (JPA requires at least one element)
        List<String> categories = favoriteCategories.isEmpty()
                ? List.of("__NONE__")
                : new ArrayList<>(favoriteCategories);
        List<Long> organizerIds = favoriteOrganizerIds.isEmpty()
                ? List.of(-1L)
                : new ArrayList<>(favoriteOrganizerIds);
        List<Long> excludeIds = interactedEventIds.isEmpty()
                ? List.of(-1L)
                : new ArrayList<>(interactedEventIds);

        return eventRepository.findRecommendedEvents(
                categories, organizerIds, excludeIds,
                org.springframework.data.domain.PageRequest.of(0, RECOMMENDATION_LIMIT));
    }
}
