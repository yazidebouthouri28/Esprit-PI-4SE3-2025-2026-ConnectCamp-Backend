package tn.esprit.projetintegre.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.repositories.SchedulerLogRepository;
import tn.esprit.projetintegre.entities.SchedulerLog;
import tn.esprit.projetintegre.repositories.SponsorRepository;
import java.time.LocalDateTime;

import tn.esprit.projetintegre.enums.SponsorTier;

@Component
public class SponsorTierUpgradeScheduler {

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private SchedulerLogRepository schedulerLogRepository;

    // Staggered: 12 seconds
    @Scheduled(initialDelay = 5000, fixedRate = 12000)
    @Transactional
    public void upgradeSponsorTiers() {
        System.out.println("[SCHEDULER] SponsorTierUpgradeScheduler starting...");
        try {
            LocalDateTime now = LocalDateTime.now();

            int toSilver = sponsorRepository.upgradeTier(SponsorTier.BRONZE, SponsorTier.SILVER, now.minusMinutes(1));
            int toGold = sponsorRepository.upgradeTier(SponsorTier.SILVER, SponsorTier.GOLD, now.minusMinutes(2));

            String details = "Upgraded " + toSilver + " to SILVER, " + toGold + " to GOLD.";
            System.out.println("[SCHEDULER] " + details);

            schedulerLogRepository.save(SchedulerLog.builder()
                    .schedulerName("SponsorTierUpgradeScheduler")
                    .details(details)
                    .build());
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] SponsorTierUpgradeScheduler failed: " + e.getMessage());
            schedulerLogRepository.save(SchedulerLog.builder()
                    .schedulerName("SponsorTierUpgradeScheduler")
                    .details("ERROR: " + e.getMessage())
                    .build());
        }
    }
}