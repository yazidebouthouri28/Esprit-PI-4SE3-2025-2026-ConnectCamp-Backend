package tn.esprit.projetintegre.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.enums.EventStatus;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.services.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventCompletionScheduler {

    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 120000, initialDelay = 30000)
    @Transactional
    public void checkCompletedEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = eventRepository.findAll();

        List<Event> overdueEvents = events.stream()
                .filter(event -> event.getEndDate() != null && event.getEndDate().isBefore(now))
                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                .toList();

        for (Event event : overdueEvents) {
            event.setStatus(EventStatus.COMPLETED);
            eventRepository.save(event);
        }

        List<Event> completedEvents = eventRepository.findAll().stream()
                .filter(event -> event.getStatus() == EventStatus.COMPLETED)
                .toList();

        for (Event event : completedEvents) {
            if (event.getOrganizer() == null || event.getOrganizer().getUser() == null) {
                continue;
            }

            notificationService.createNotificationIfAbsent(
                    event.getOrganizer().getUser().getId(),
                    "Event completed: " + event.getTitle(),
                    "Your event is completed. You can now award badges to participants.",
                    "EVENT_COMPLETED",
                    "/organizer/events?action=award-badges&id=" + event.getId(),
                    "EVENT",
                    event.getId());
        }
    }
}
