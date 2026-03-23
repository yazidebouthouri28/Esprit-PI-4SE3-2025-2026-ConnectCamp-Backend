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
    public Event createEvent(Event event, Long siteId, Long organizerId) {
        if (siteId != null) {
            Site site = siteRepository.findById(siteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found"));
            event.setSite(site);
        }

        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));
        event.setOrganizer(organizer);

        if (event.getStatus() == null) {
            event.setStatus(EventStatus.DRAFT);
        }

        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);

        if (eventDetails.getName() != null)
            event.setName(eventDetails.getName());
        if (eventDetails.getDescription() != null)
            event.setDescription(eventDetails.getDescription());
        if (eventDetails.getEventType() != null)
            event.setEventType(eventDetails.getEventType());
        if (eventDetails.getCategory() != null)
            event.setCategory(eventDetails.getCategory());
        if (eventDetails.getPicture() != null)
            event.setPicture(eventDetails.getPicture());
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
        if (event.getEndDate() == null) {
            throw new IllegalStateException("Event end date must be set before publishing");
        }
        event.setStatus(EventStatus.PUBLISHED);
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = getEventById(id);
        eventRepository.delete(event);
    }
}
