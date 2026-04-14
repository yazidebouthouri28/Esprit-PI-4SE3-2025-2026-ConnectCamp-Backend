package tn.esprit.projetintegre.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.repositories.SchedulerLogRepository;
import tn.esprit.projetintegre.entities.SchedulerLog;
import tn.esprit.projetintegre.repositories.SponsorRepository;
import java.time.LocalDateTime;

@Component
public class SponsorTierUpgradeScheduler {

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private SchedulerLogRepository schedulerLogRepository;

    // Runs 10 seconds after boot, then every 2 minutes
    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    @Transactional
    public void upgradeSponsorTiers() {
        System.out.println("[SCHEDULER] IncompleteSponsorScheduler starting...");
        LocalDateTime now = LocalDateTime.now();

        int toSilver = sponsorRepository.upgradeBronzeToSilver(now.minusMinutes(1));
        int toGold = sponsorRepository.upgradeSilverToGold(now.minusMinutes(2));

        String details = "Upgraded " + toSilver + " to SILVER, " + toGold + " to GOLD.";
        System.out.println("[SCHEDULER] " + details);

        schedulerLogRepository.save(SchedulerLog.builder()
                .schedulerName("SponsorTierUpgradeScheduler")
                .details(details)
                .build());
    }
}