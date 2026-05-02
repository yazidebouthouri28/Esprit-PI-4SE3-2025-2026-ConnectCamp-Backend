package tn.esprit.projetintegre.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.SchedulerLog;

@Repository
public interface SchedulerLogRepository extends JpaRepository<SchedulerLog, Long> {
}
