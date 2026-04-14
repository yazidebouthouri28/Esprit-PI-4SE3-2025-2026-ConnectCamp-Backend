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

    // Runs 10 seconds after boot, then every 2 minutes
    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    @Transactional
    public void suspendIncompleteSponsors() {
        System.out.println("[SCHEDULER] IncompleteSponsorScheduler starting...");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
        int suspendedCount = sponsorRepository.suspendIncompleteSponsors(threshold);
        
        String details = "Suspended " + suspendedCount + " incomplete sponsor profiles.";
        System.out.println("[SCHEDULER] " + details);

        schedulerLogRepository.save(SchedulerLog.builder()
                .schedulerName("IncompleteSponsorScheduler")
                .details(details)
                .build());
    }
}
