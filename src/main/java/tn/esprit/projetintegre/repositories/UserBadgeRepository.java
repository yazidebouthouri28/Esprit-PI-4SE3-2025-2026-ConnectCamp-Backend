package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.UserBadge;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.entities.Badge;
import tn.esprit.projetintegre.entities.Event;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserId(Long userId);

    List<UserBadge> findByEventId(Long eventId);

    boolean existsByUserAndBadgeAndEvent(User user, Badge badge, Event event);

    void deleteByEventId(Long eventId);
}
