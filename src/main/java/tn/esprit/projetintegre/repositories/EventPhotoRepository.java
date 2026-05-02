package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.projetintegre.entities.EventPhoto;

public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {
    void deleteByEventId(Long eventId);
}
