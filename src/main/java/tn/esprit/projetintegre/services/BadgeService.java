package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.projetintegre.entities.Badge;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.repositories.BadgeRepository;
import tn.esprit.projetintegre.repositories.EventRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final EventRepository eventRepository;

    public List<Badge> getAll() {
        return badgeRepository.findAll();
    }

    public Optional<Badge> getById(Long id) {
        return badgeRepository.findById(id);
    }

    public Badge create(Badge badge) {
        return badgeRepository.save(badge);
    }

    public Badge update(Long id, Badge badgeDetails) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge not found with id: " + id));
        badge.setName(badgeDetails.getName());
        badge.setIcon(badgeDetails.getIcon());
        badge.setMedal(badgeDetails.getMedal());
        return badgeRepository.save(badge);
    }

    public void delete(Long id) {
        badgeRepository.deleteById(id);
    }

    public void assignBadgeToEvent(Long badgeId, Long eventId) {
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new RuntimeException("Badge not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.getBadges().add(badge);
        eventRepository.save(event);
    }

    public void removeBadgeFromEvent(Long badgeId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.getBadges().removeIf(b -> b.getId().equals(badgeId));
        eventRepository.save(event);
    }
}
