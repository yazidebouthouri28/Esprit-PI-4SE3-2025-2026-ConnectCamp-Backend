package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.*;
import tn.esprit.projetintegre.enums.ReservationStatus;
import tn.esprit.projetintegre.repositories.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GamificationService {

    private final BadgeRepository badgeRepository;
    private final MedalRepository medalRepository;
    private final BadgeRuleRepository badgeRuleRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserMedalRepository userMedalRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    private final EventService eventService;

    // --- Medal Methods ---
    public List<Medal> getAllMedals() {
        return medalRepository.findAll();
    }

    public Medal getMedalById(Long id) {
        return medalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medal not found"));
    }

    public Medal createMedal(Medal medal) {
        return medalRepository.save(medal);
    }

    public Medal updateMedal(Long id, Medal medalDetails) {
        Medal medal = getMedalById(id);
        medal.setName(medalDetails.getName());
        medal.setIcon(medalDetails.getIcon());
        medal.setType(medalDetails.getType());
        return medalRepository.save(medal);
    }

    public void deleteMedal(Long id) {
        medalRepository.deleteById(id);
    }

    // --- Badge Methods ---
    public List<Badge> getAllBadges() {
        List<Badge> badges = badgeRepository.findAll();
        // Initialize lazy associations before leaving transactional boundary.
        badges.forEach(this::initializeBadgeAssociations);
        return badges;
    }

    public Badge getBadgeById(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge not found"));
        initializeBadgeAssociations(badge);
        return badge;
    }

    public Badge createBadge(Badge badge, Long medalId) {
        if (medalId != null) {
            Medal medal = getMedalById(medalId);
            badge.setMedal(medal);
        }
        return badgeRepository.save(badge);
    }

    public Badge updateBadge(Long id, Badge badgeDetails, Long medalId) {
        Badge badge = getBadgeById(id);
        badge.setName(badgeDetails.getName());
        badge.setIcon(badgeDetails.getIcon());
        if (medalId != null) {
            Medal medal = getMedalById(medalId);
            badge.setMedal(medal);
        }
        return badgeRepository.save(badge);
    }

    public void deleteBadge(Long id) {
        badgeRepository.deleteById(id);
    }

    private void initializeBadgeAssociations(Badge badge) {
        if (badge == null) {
            return;
        }
        if (badge.getMedal() != null) {
            badge.getMedal().getName();
        }
        if (badge.getRules() != null) {
            badge.getRules().size();
        }
    }

    // --- Badge Rule Methods ---
    public BadgeRule addRuleToBadge(Long badgeId, BadgeRule rule) {
        Badge badge = getBadgeById(badgeId);
        rule.setBadge(badge);
        return badgeRuleRepository.save(rule);
    }

    // --- Assignment Methods ---
    public UserBadge awardBadgeToUser(Long userId, Long badgeId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Badge badge = getBadgeById(badgeId);
        Event event = eventId != null ? eventRepository.findById(eventId).orElse(null) : null;

        // Avoid duplicates
        if (userBadgeRepository.existsByUserAndBadgeAndEvent(user, badge, event)) {
            return userBadgeRepository.findByUserAndBadgeAndEvent(user, badge, event);
        }

        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .event(event)
                .earnedAt(java.time.LocalDateTime.now())
                .build();
        UserBadge saved = userBadgeRepository.save(userBadge);

        String eventContext = event != null && event.getTitle() != null
                ? " pour l'evenement \"" + event.getTitle() + "\""
                : "";
        notificationService.createNotificationIfAbsent(
                user.getId(),
                "Nouveau badge gagne",
                "Vous avez gagne le badge \"" + badge.getName() + "\"" + eventContext + ".",
                "BADGE_AWARDED",
                "/profile",
                "USER_BADGE",
                saved.getId());

        return saved;
    }

    public void awardBulkBadges(List<Long> userIds, Long badgeId, Long eventId, Authentication authentication) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("No participants selected");
        }
        if (badgeId == null) {
            throw new IllegalArgumentException("Badge is required");
        }
        if (eventId == null) {
            throw new IllegalArgumentException("Event is required");
        }

        Event event = eventService.getManageableEvent(eventId, authentication);
        Badge badge = getBadgeById(badgeId);

        boolean badgeBelongsToEvent = event.getBadges() != null
                && event.getBadges().stream().anyMatch(eventBadge -> Objects.equals(eventBadge.getId(), badge.getId()));
        if (!badgeBelongsToEvent) {
            throw new IllegalStateException("Selected badge is not linked to this event");
        }

        Set<Long> participantUserIds = reservationRepository.findByEventIdAndStatusIn(
                eventId,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED))
                .stream()
                .map(Reservation::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toSet());

        List<Long> invalidUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(userId -> !participantUserIds.contains(userId))
                .toList();
        if (!invalidUserIds.isEmpty()) {
            throw new IllegalStateException("Some selected users are not participants of this event");
        }

        for (Long userId : userIds.stream().filter(Objects::nonNull).distinct().toList()) {
            awardBadgeToUser(userId, badgeId, eventId);
        }
    }

    public UserMedal awardMedalToUser(Long userId, Long medalId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Medal medal = getMedalById(medalId);
        Event event = eventId != null ? eventRepository.findById(eventId).orElse(null) : null;

        UserMedal userMedal = UserMedal.builder()
                .user(user)
                .medal(medal)
                .event(event)
                .build();
        return userMedalRepository.save(userMedal);
    }
}
