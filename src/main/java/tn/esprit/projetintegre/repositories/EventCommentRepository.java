package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.EventComment;

import java.util.List;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long> {
    List<EventComment> findByEventId(Long eventId);

    List<EventComment> findByUserId(Long userId);
}
