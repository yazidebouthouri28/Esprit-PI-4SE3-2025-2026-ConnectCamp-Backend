package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.UserMedal;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMedalRepository extends JpaRepository<UserMedal, Long> {
    List<UserMedal> findByUserId(Long userId);

    Optional<UserMedal> findByUserIdAndMedalId(Long userId, Long medalId);
}
