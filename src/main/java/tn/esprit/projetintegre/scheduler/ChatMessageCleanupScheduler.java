package tn.esprit.projetintegre.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.repositories.ChatMessageRepository;
import tn.esprit.projetintegre.repositories.SchedulerLogRepository;
import tn.esprit.projetintegre.entities.SchedulerLog;
import java.time.LocalDateTime;

@Component
public class ChatMessageCleanupScheduler {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SchedulerLogRepository schedulerLogRepository;

    // Runs 10 seconds after boot, then every 2 minutes
    @Scheduled(initialDelay = 5000, fixedRate = 10000)
    @Transactional
    public void deleteOldMessages() {
        System.out.println("[SCHEDULER] ChatMessageCleanupScheduler starting...");
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
        int deletedCount = chatMessageRepository.deleteOldMessages(threshold);
        
        String details = "Deleted " + deletedCount + " old chat messages (> 1 year).";
        System.out.println("[SCHEDULER] " + details);

        schedulerLogRepository.save(SchedulerLog.builder()
                .schedulerName("ChatMessageCleanupScheduler")
                .details(details)
                .build());
    }
}
