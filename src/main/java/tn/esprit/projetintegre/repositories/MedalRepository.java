package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Medal;

@Repository
public interface MedalRepository extends JpaRepository<Medal, Long> {
}
