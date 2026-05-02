package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.UserMedal;

import java.util.List;

@Repository
public interface UserMedalRepository extends JpaRepository<UserMedal, Long> {
    List<UserMedal> findByUserId(Long userId);
}
