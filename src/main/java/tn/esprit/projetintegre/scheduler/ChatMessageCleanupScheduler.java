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

    // Staggered: 16 seconds
    @Scheduled(initialDelay = 5000, fixedRate = 16000)
    @Transactional
    public void deleteOldMessages() {
        System.out.println("[SCHEDULER] ChatMessageCleanupScheduler starting...");
        try {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
            int deletedCount = chatMessageRepository.deleteOldMessages(threshold);
            
            String details = "Cleaned up " + deletedCount + " old chat messages.";
            System.out.println("[SCHEDULER] " + details);

            schedulerLogRepository.save(SchedulerLog.builder()
                    .schedulerName("ChatMessageCleanupScheduler")
                    .details(details)
                    .build());
        } catch (Exception e) {
            System.err.println("[SCHEDULER ERROR] ChatMessageCleanupScheduler failed: " + e.getMessage());
            schedulerLogRepository.save(SchedulerLog.builder()
                    .schedulerName("ChatMessageCleanupScheduler")
                    .details("ERROR: " + e.getMessage())
                    .build());
        }
    }
}
