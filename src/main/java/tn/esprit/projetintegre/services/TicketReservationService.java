package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.dto.response.TicketReservationResponse;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.TicketReservation;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.ReservationStatus;
import tn.esprit.projetintegre.enums.EventStatus;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.TicketReservationRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TicketReservationService {

    private final TicketReservationRepository ticketReservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketReservationResponse createEventTicketReservation(Long userId,
                                                                 Long eventId,
                                                                 int quantity,
                                                                 String notes) {
        if (quantity <= 0) {
            throw new IllegalStateException("Quantity must be at least 1");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() == EventStatus.COMPLETED) {
            throw new IllegalStateException("Reservations are closed for completed events");
        }

        if (event.getMaxParticipants() != null &&
                event.getCurrentParticipants() + quantity > event.getMaxParticipants()) {
            throw new IllegalStateException("Event does not have enough slots available");
        }

        BigDecimal unitPrice = Boolean.TRUE.equals(event.getIsFree())
                ? BigDecimal.ZERO
                : (event.getPrice() != null ? event.getPrice() : BigDecimal.ZERO);
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

        TicketReservation reservation = TicketReservation.builder()
                .user(user)
                .event(event)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .status(ReservationStatus.CONFIRMED)
                .notes((notes == null || notes.isBlank()) ? null : notes)
                .build();

        reservation = ticketReservationRepository.save(reservation);

        // Avoid triggering bean-validation on legacy events when only participant count changes.
        // (Event has an @AssertTrue on registrationDeadline/startDate, which can block reservations.)
        eventRepository.incrementCurrentParticipants(event.getId(), quantity);

        return toResponse(reservation);
    }

    private TicketReservationResponse toResponse(TicketReservation reservation) {
        return TicketReservationResponse.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .userId(reservation.getUser() != null ? reservation.getUser().getId() : null)
                .eventId(reservation.getEvent() != null ? reservation.getEvent().getId() : null)
                .quantity(reservation.getQuantity())
                .unitPrice(reservation.getUnitPrice())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
