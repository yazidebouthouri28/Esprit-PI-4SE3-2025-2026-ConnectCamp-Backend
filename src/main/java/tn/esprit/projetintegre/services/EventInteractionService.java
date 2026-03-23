package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.EventInteraction;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.EventInteractionRepository;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventInteractionService {

    private final EventInteractionRepository interactionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public void handleLike(Long eventId, Long userId) {
        handleInteraction(eventId, userId, true);
    }

    public void handleDislike(Long eventId, Long userId) {
        handleInteraction(eventId, userId, false);
    }

    public Map<String, Boolean> getUserReaction(Long eventId, Long userId) {
        Optional<EventInteraction> existing = interactionRepository.findByUser_IdAndEvent_Id(userId, eventId);
        if (existing.isEmpty()) {
            return Map.of("liked", false, "disliked", false);
        }
        EventInteraction interaction = existing.get();
        boolean liked = Boolean.TRUE.equals(interaction.getLiked());
        boolean disliked = Boolean.TRUE.equals(interaction.getDisliked());
        return Map.of("liked", liked, "disliked", disliked);
    }

    private void handleInteraction(Long eventId, Long userId, boolean isLike) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Optional<EventInteraction> existingInteraction = interactionRepository.findByUser_IdAndEvent_Id(userId,
                eventId);
        log.info("INTERACTION START: user={}, event={}, isLike={}, exists={}",
                userId, eventId, isLike, existingInteraction.isPresent());

        if (existingInteraction.isPresent()) {
            EventInteraction interaction = existingInteraction.get();
            boolean currentIsLike = Boolean.TRUE.equals(interaction.getLiked());
            boolean currentIsDislike = Boolean.TRUE.equals(interaction.getDisliked());

            log.info("INTERACTION CHECK: user={}, event={}, isLike(request)={}, currentLiked={}, currentIsDislike={}",
                    userId, eventId, isLike, currentIsLike, currentIsDislike);

            if (currentIsLike == isLike && currentIsDislike == !isLike) {
                log.info("INTERACTION TOGGLE-OFF (DELETE): user {} on event {}", userId, eventId);
                interactionRepository.delete(interaction);
            } else {
                log.info("INTERACTION TOGGLE (UPDATE): set like={} dislike={} for user {} on event {}",
                        isLike, !isLike, userId, eventId);
                interaction.setLiked(isLike);
                interaction.setDisliked(!isLike);
                interactionRepository.save(interaction);
            }
        } else {
            log.info("INTERACTION CREATE: like={} dislike={} for user {} on event {}", isLike, !isLike, userId,
                    eventId);
            EventInteraction interaction = EventInteraction.builder()
                    .event(event)
                    .user(user)
                    .liked(isLike)
                    .disliked(!isLike)
                    .build();
            interactionRepository.save(interaction);
        }

        // Force flush to ensure counts are accurate in the same transaction
        interactionRepository.flush();

        // Update counters
        updateCounters(event);
    }

    private void updateCounters(Event event) {
        long likes = interactionRepository.countByEvent_IdAndLiked(event.getId(), true);
        long dislikes = interactionRepository.countByEvent_IdAndLiked(event.getId(), false);

        log.info("INTERACTION COUNTERS: event={}, likes={}, dislikes={}", event.getId(), likes, dislikes);

        event.setLikesCount((int) likes);
        event.setDislikesCount((int) dislikes);

        // Custom rating logic:
        // - Only likes (X=0, Y>0) -> 5 stars
        // - Only dislikes (Y=0, X>0) -> 0 stars
        // - Otherwise -> (|Y-X|)/2 stars (Rounded UP)
        if (likes > 0 && dislikes == 0) {
            event.setRating(BigDecimal.valueOf(5));
        } else if (likes == 0 && dislikes > 0) {
            event.setRating(BigDecimal.valueOf(0));
        } else if (likes > 0 && dislikes > 0) {
            long diff = Math.abs(likes - dislikes);
            // Support half stars (0.5, 1.5, etc.)
            BigDecimal rating = BigDecimal.valueOf(diff).divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
            // Cap at 5 stars
            if (rating.compareTo(BigDecimal.valueOf(5)) > 0) {
                rating = BigDecimal.valueOf(5);
            }
            event.setRating(rating);
        } else {
            event.setRating(null);
        }

        log.info("RATING FINAL: event={}, likes={}, dislikes={}, rating={}",
                event.getId(), likes, dislikes, event.getRating());

        eventRepository.save(event);
    }
}
