package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.dto.request.EventRequest;
import tn.esprit.projetintegre.dto.response.EventResponse;
import tn.esprit.projetintegre.dto.response.EventRevenueDTO;
import tn.esprit.projetintegre.dto.response.ParticipantResponseDTO;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.enums.EventStatus;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.EventService;
import tn.esprit.projetintegre.services.EventMLService;
import tn.esprit.projetintegre.services.GamificationService;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.BadgeRepository;
import tn.esprit.projetintegre.dto.request.MLPredictionRequest;
import tn.esprit.projetintegre.dto.response.MLPredictionResponse;
import tn.esprit.projetintegre.entities.Badge;
import tn.esprit.projetintegre.repositories.ReservationRepository;
import tn.esprit.projetintegre.entities.Reservation;
import tn.esprit.projetintegre.enums.ReservationStatus;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")

@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {

    private final EventService eventService;
    private final DtoMapper dtoMapper;
    private final EventMLService mlService;
    private final EventRepository eventRepository;
    private final GamificationService gamificationService;
    private final BadgeRepository badgeRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(events.stream().map(this::toEventResponse).toList()));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get events by status")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsByStatus(
            @PathVariable("status") EventStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Event> events = eventService.getEventsByStatus(status, PageRequest.of(page, size));
        Page<EventResponse> response = events.map(this::toEventResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getUpcomingEvents(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<Event> events = eventService.getUpcomingEvents(limit);
        return ResponseEntity.ok(ApiResponse.success(events.stream().map(this::toEventResponse).toList()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search events")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> searchEvents(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Event> events = eventService.searchEvents(keyword, PageRequest.of(page, size));
        Page<EventResponse> response = events.map(this::toEventResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/organizer/{organizerId}")
    @Operation(summary = "Get events by organizer")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsByOrganizer(
            @PathVariable("organizerId") Long organizerId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication) {
        eventService.assertCanAccessOrganizerScope(organizerId, authentication);
        Page<Event> events = eventService.getEventsByOrganizer(organizerId, PageRequest.of(page, size));
        Page<EventResponse> response = events.map(this::toEventResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/site/{siteId}")
    @Operation(summary = "Get events by site")
    public ResponseEntity<ApiResponse<PageResponse<EventResponse>>> getEventsBySite(
            @PathVariable("siteId") Long siteId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Event> events = eventService.getEventsBySite(siteId, PageRequest.of(page, size));
        Page<EventResponse> response = events.map(this::toEventResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Créer un événement")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {
        Event created = eventService.createEvent(
                mapToEvent(request),
                request.getSiteId(),
                request.getOrganizerId(),
                request.getGamificationIds(),
                authentication);
        return ResponseEntity.ok(ApiResponse.success("Événement créé avec succès", toEventResponse(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Mettre à jour un événement")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable("id") Long id,
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {
        Event updated = eventService.updateEvent(id, mapToEvent(request), request.getGamificationIds(), authentication);
        return ResponseEntity
                .ok(ApiResponse.success("Événement mis à jour avec succès", toEventResponse(updated)));
    }

    private Event mapToEvent(EventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .category(request.getCategory())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .maxParticipants(request.getMaxParticipants())
                .price(request.getPrice())
                .isFree(request.getIsFree())
                .isPublic(request.getIsPublic())
                .requiresApproval(request.getRequiresApproval())
                .status(request.getStatus())
                .registrationDeadline(request.getRegistrationDeadline())
                .build();
        event.setImages(request.getImages());
        return event;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Update event status")
    public ResponseEntity<ApiResponse<EventResponse>> updateEventStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") EventStatus status,
            Authentication authentication) {
        Event updated = eventService.updateEventStatus(id, status, authentication);

        if (status == EventStatus.COMPLETED) {
            List<Reservation> reservations = reservationRepository.findByEventIdAndStatusIn(
                    id,
                    List.of(ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED));
            int actualAttendees = reservations.size();

            updated.setActualAttendees(actualAttendees);
            String badgeName = determineBadgeByAttendees(actualAttendees);
            updated.setAwardedBadge(badgeName);

            Optional<Badge> badgeOpt = badgeRepository.findByName(badgeName);
            if (badgeOpt.isPresent()) {
                Badge badge = badgeOpt.get();
                for (Reservation res : reservations) {
                    if (res.getUser() != null) {
                        gamificationService.awardBadgeToUser(res.getUser().getId(), badge.getId(), id);
                    }
                }
            }

            eventRepository.save(updated);
            mlService.sendRetrainingData(updated);
        }

        return ResponseEntity.ok(ApiResponse.success("Event status updated", toEventResponse(updated)));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Publish an event")
    public ResponseEntity<ApiResponse<EventResponse>> publishEvent(
            @PathVariable("id") Long id,
            Authentication authentication) {
        Event published = eventService.publishEvent(id, authentication);
        return ResponseEntity
                .ok(ApiResponse.success("Event published successfully", toEventResponse(published)));
    }

    private EventResponse toEventResponse(Event event) {
        EventResponse response = dtoMapper.toEventResponse(event);
        response.setLikesCount(eventService.getLikesCount(event.getId()));
        response.setDislikesCount(eventService.getDislikesCount(event.getId()));
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Delete/Cancel an event")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable("id") Long id,
            Authentication authentication) {
        eventService.deleteEvent(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Event cancelled", null));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Bulk delete/cancel events")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteEvents(
            @RequestBody List<Long> ids,
            Authentication authentication) {
        eventService.bulkDeleteEvents(ids, authentication);
        return ResponseEntity.ok(ApiResponse.success("Events cancelled", null));
    }

    @GetMapping("/organizer/{organizerId}/stats")
    @Operation(summary = "Get organizer events statistics")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getOrganizerStats(
            @PathVariable("organizerId") Long organizerId,
            Authentication authentication) {
        eventService.assertCanAccessOrganizerScope(organizerId, authentication);
        Long totalViews = eventService.getTotalViewsByOrganizer(organizerId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "totalViews", totalViews != null ? totalViews : 0)));
    }

    @GetMapping("/stats/total-views")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get total views for all events")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getTotalViews() {
        Long totalViews = eventService.getTotalViewsForAllEvents();
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "totalViews", totalViews != null ? totalViews : 0)));
    }

    @GetMapping("/stats/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Get revenue data (all events for admin, own events for organizer)")
    public ResponseEntity<ApiResponse<List<EventRevenueDTO>>> getRevenueData(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getRevenueData(authentication)));
    }

    @GetMapping("/{id}/participants")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get event participants")
    public ResponseEntity<ApiResponse<List<ParticipantResponseDTO>>> getParticipants(
            @PathVariable("id") Long id,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getParticipants(id, authentication)));
    }

    @PostMapping("/predict")
    @Operation(summary = "Prédire la popularité de l'événement (prévisualisation)")
    public ResponseEntity<MLPredictionResponse> predictEvent(@RequestBody MLPredictionRequest request) {
        MLPredictionResponse prediction = mlService.predictPopularity(request);
        return ResponseEntity.ok(prediction);
    }

    @GetMapping("/predict")
    @Operation(summary = "Predict endpoint (POST only)")
    public ResponseEntity<ApiResponse<Void>> predictEventGetNotSupported() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("Use POST /api/events/predict with a JSON body."));
    }

    @PostMapping("/create-ml")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Créer un événement avec prédiction ML intégrée")
    public ResponseEntity<ApiResponse<EventResponse>> createEventWithML(
            @Valid @RequestBody EventRequest request,
            Authentication authentication) {

        // 1. Appeler l'API ML pour la prédiction
        long durationHours = java.time.Duration.between(request.getStartDate(), request.getEndDate()).toHours();
        MLPredictionRequest mlRequest = MLPredictionRequest.builder()
                .category(request.getCategory())
                .event_type(request.getEventType()) // ← AJOUTÉ
                .state(request.getLocation() != null ? request.getLocation() : "Tunis") // ← AJOUTÉ
                .hour(request.getStartDate().getHour())
                .month(request.getStartDate().getMonthValue())
                .day_of_week(request.getStartDate().getDayOfWeek().getValue() - 1)
                .duration_hours((int) Math.max(1, durationHours)) // ← AJOUTÉ
                .price(request.getPrice() != null ? request.getPrice().doubleValue() : 0) // ← AJOUTÉ
                .build();

        MLPredictionResponse prediction = mlService.predictPopularity(mlRequest);

        // 2. Créer l'événement de base
        Event event = mapToEvent(request);
        event.setPredictedAttendees(prediction.getPredicted_attendees());
        event.setPopularity(prediction.getPopularity());
        event.setSuggestedBadge(prediction.getBadge_suggestion());

        // 3. Sauvegarder via le service existant
        Event created = eventService.createEvent(
                event,
                request.getSiteId(),
                request.getOrganizerId(),
                request.getGamificationIds(),
                authentication);

        return ResponseEntity
                .ok(ApiResponse.success("Événement créé avec succès avec prédiction ML", toEventResponse(created)));
    }

    private String determineBadgeByAttendees(int attendees) {
        if (attendees < 5)
            return "Explorer";
        if (attendees < 20)
            return "Connector";
        if (attendees < 50)
            return "Networker";
        return "Community Leader";
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get personalized event recommendations for the authenticated user")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getRecommendations(Authentication authentication) {
        if (authentication == null) {
            // Not authenticated → return upcoming events as fallback
            List<Event> upcoming = eventService.getUpcomingEvents(8);
            return ResponseEntity.ok(ApiResponse.success(upcoming.stream().map(this::toEventResponse).toList()));
        }
        tn.esprit.projetintegre.entities.User user = userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElse(null);
        if (user == null) {
            List<Event> upcoming = eventService.getUpcomingEvents(8);
            return ResponseEntity.ok(ApiResponse.success(upcoming.stream().map(this::toEventResponse).toList()));
        }
        List<Event> recommended = eventService.getRecommendedEventsForUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(recommended.stream().map(this::toEventResponse).toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable("id") Long id) {
        eventService.incrementViewCount(id);
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(toEventResponse(event)));
    }
}
