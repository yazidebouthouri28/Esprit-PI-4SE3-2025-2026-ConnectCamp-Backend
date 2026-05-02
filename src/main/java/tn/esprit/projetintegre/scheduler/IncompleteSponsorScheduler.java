package tn.esprit.projetintegre.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.repositories.SchedulerLogRepository;
import tn.esprit.projetintegre.repositories.SponsorRepository;
import tn.esprit.projetintegre.entities.SchedulerLog;
import java.time.LocalDateTime;

@Component
public class IncompleteSponsorScheduler {

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private SchedulerLogRepository schedulerLogRepository;

    // Staggered: 14 seconds
    @Scheduled(initialDelay = 5000, fixedRate = 14000)
    @Transactional
    public void suspendIncompleteSponsors() {
        System.out.println("[SCHEDULER] IncompleteSponsorScheduler starting...");
        try {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
            int suspendedCount = sponsorRepository.suspendIncompleteSponsors(threshold);
            
            String details = "Suspended " + suspendedCount + " incomplete sponsor profiles.";
            System.out.println("[SCHEDULER] " + details);

            schedulerLogRepository.save(SchedulerLog.builder()
                    .schedulerName("IncompleteSponsorScheduler")
                    .details(details)
                    .build());
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] IncompleteSponsorScheduler failed: " + e.getMessage());
            schedulerLogRepository.save(SchedulerLog.builder()
                    .schedulerName("IncompleteSponsorScheduler")
                    .details("ERROR: " + e.getMessage())
                    .build());
        }
    }
}
