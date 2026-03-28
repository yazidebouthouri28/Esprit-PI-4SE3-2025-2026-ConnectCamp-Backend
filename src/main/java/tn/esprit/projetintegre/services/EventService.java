package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Organizer;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.EventStatus;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.OrganizerRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final OrganizerRepository organizerRepository;
    private final EventRepository eventRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;

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
    public Event createEvent(Event event, Long siteId, Long organizerId, String authenticatedUsername) {
        if (siteId != null) {
            Site site = siteRepository.findById(siteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found"));
            event.setSite(site);
        }

        Organizer organizer = resolveOrganizer(organizerId, authenticatedUsername);
        event.setOrganizer(organizer);
        if (event.getStatus() == null) {
            event.setStatus(EventStatus.DRAFT);
        }

        return eventRepository.save(event);
    }

    private Organizer resolveOrganizer(Long organizerId, String authenticatedUsername) {
        // If frontend passes an organizerId but it's invalid/not found, fall back to authenticated user.
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

    @Transactional
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);

        if (eventDetails.getTitle() != null) event.setTitle(eventDetails.getTitle());
        if (eventDetails.getDescription() != null) event.setDescription(eventDetails.getDescription());
        if (eventDetails.getEventType() != null) event.setEventType(eventDetails.getEventType());
        if (eventDetails.getCategory() != null) event.setCategory(eventDetails.getCategory());
        if (eventDetails.getStartDate() != null) event.setStartDate(eventDetails.getStartDate());
        if (eventDetails.getEndDate() != null) event.setEndDate(eventDetails.getEndDate());
        if (eventDetails.getMaxParticipants() != null) event.setMaxParticipants(eventDetails.getMaxParticipants());
        if (eventDetails.getPrice() != null) event.setPrice(eventDetails.getPrice());
        if (eventDetails.getIsFree() != null) event.setIsFree(eventDetails.getIsFree());
        if (eventDetails.getImages() != null) event.setImages(eventDetails.getImages());
        if (eventDetails.getThumbnail() != null) event.setThumbnail(eventDetails.getThumbnail());
        if (eventDetails.getLocation() != null) event.setLocation(eventDetails.getLocation());

        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEventStatus(Long id, EventStatus status) {
        Event event = getEventById(id);
        event.setStatus(status);
        return eventRepository.save(event);
    }

    @Transactional
    public Event publishEvent(Long id) {
        Event event = getEventById(id);
        if (event.getStartDate() == null || event.getEndDate() == null) {
            throw new IllegalStateException("Event dates must be set before publishing");
        }
        event.setStatus(EventStatus.PUBLISHED);
        return eventRepository.save(event);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        Event event = getEventById(id);
        event.setViewCount(event.getViewCount() + 1);
        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = getEventById(id);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }
}
