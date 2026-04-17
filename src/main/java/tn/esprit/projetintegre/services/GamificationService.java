package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.*;
import tn.esprit.projetintegre.repositories.*;

import java.util.List;

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

        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .event(event)
                .build();
        return userBadgeRepository.save(userBadge);
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
