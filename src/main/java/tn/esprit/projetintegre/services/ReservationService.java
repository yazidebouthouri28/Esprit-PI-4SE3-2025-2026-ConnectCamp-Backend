package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Reservation;
import tn.esprit.projetintegre.entities.Site;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.enums.ReservationStatus;
import tn.esprit.projetintegre.enums.Role;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.entities.ChatRoom;
import tn.esprit.projetintegre.enums.ChatRoomType;
import tn.esprit.projetintegre.dto.ChatRoomDTO;
import tn.esprit.projetintegre.repositories.ChatRoomRepository;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.ReservationRepository;
import tn.esprit.projetintegre.repositories.SiteRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SiteRepository siteRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Page<Reservation> getReservationsByUser(Long userId, Pageable pageable) {
        return reservationRepository.findByUserId(userId, pageable);
    }

    public Page<Reservation> getReservationsBySite(Long siteId, Pageable pageable) {
        return reservationRepository.findBySiteId(siteId, pageable);
    }

    public Page<Reservation> getReservationsByStatus(ReservationStatus status, Pageable pageable) {
        return reservationRepository.findByStatus(status, pageable);
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
    }

    public Reservation getReservationByNumber(String reservationNumber) {
        return reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    @Transactional
    public Reservation createSiteReservation(Long userId, Long siteId, LocalDateTime checkIn,
            LocalDateTime checkOut, int numberOfGuests,
            String guestName, String guestEmail, String guestPhone,
            String specialRequests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        // Check availability
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(siteId, checkIn, checkOut);
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Site is not available for the selected dates");
        }

        long nights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        BigDecimal totalPrice = site.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = Reservation.builder()
                .user(user)
                .site(site)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .numberOfGuests(numberOfGuests)
                .numberOfNights((int) nights)
                .pricePerNight(site.getPricePerNight())
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .guestName(guestName)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .specialRequests(specialRequests)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation createEventReservation(Long userId, Long eventId, String guestName,
            String guestEmail, String guestPhone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (user.getRole() == Role.SPONSOR) {
            throw new IllegalStateException("Sponsors cannot reserve tickets. You must be assigned by an admin");
        }

        if (event.getMaxParticipants() != null &&
                event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new IllegalStateException("Event is fully booked");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .event(event)
                .checkInDate(event.getEndDate())
                .checkOutDate(event.getEndDate())
                .numberOfGuests(1)
                .totalPrice(event.getIsFree() ? BigDecimal.ZERO : event.getPrice())
                .status(ReservationStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.COMPLETED)
                .guestName(guestName)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .build();

        reservation = reservationRepository.save(reservation);

        // Update event participant count
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);

        // Auto-create or auto-join event chat room
        ChatRoom room = chatRoomRepository.findByRelatedEntity("EVENT", event.getId()).orElse(null);
        if (room == null) {
            ChatRoomDTO.CreateRequest createReq = new ChatRoomDTO.CreateRequest();
            createReq.setName(event.getTitle() + " group chat");
            createReq.setDescription("Official group chat for " + event.getTitle());
            createReq.setType(ChatRoomType.EVENT);
            createReq.setRelatedEntityId(event.getId());
            createReq.setRelatedEntityType("EVENT");
            createReq.setAllowJoin(true);
            createReq.setIsPublic(true);

            Long creatorId = event.getOrganizer() != null && event.getOrganizer().getUser() != null 
                    ? event.getOrganizer().getUser().getId() : user.getId();
            try {
                ChatRoomDTO.Response response = chatRoomService.createRoom(creatorId, createReq);
                room = chatRoomRepository.findById(response.getId()).orElse(null);
            } catch (Exception e) {
                // Ignore room creation error
            }
        }

        if (room != null) {
            try {
                if (!room.getCreator().getId().equals(user.getId())) {
                    chatRoomService.addMember(room.getId(), room.getCreator().getId(), user.getId());
                }
            } catch (Exception e) {
                // user might already be a member
            }
        }

        return reservation;
    }

    @Transactional
    public Reservation confirmReservation(Long id) {
        Reservation reservation = getReservationById(id);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());
        
        // Auto-join event chat room if applicable
        if (reservation.getEvent() != null) {
            chatRoomRepository.findByRelatedEntity("EVENT", reservation.getEvent().getId())
                .ifPresent(room -> {
                    try {
                        chatRoomService.addMember(room.getId(), room.getCreator().getId(), reservation.getUser().getId());
                    } catch (Exception e) {
                        // user might already be a member
                    }
                });
        }
        
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long id, String reason) {
        Reservation reservation = getReservationById(id);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancellationReason(reason);
        reservation.setCancelledAt(LocalDateTime.now());

        // If event reservation, decrease participant count
        if (reservation.getEvent() != null) {
            Event event = reservation.getEvent();
            event.setCurrentParticipants(event.getCurrentParticipants() - 1);
            eventRepository.save(event);
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updatePaymentStatus(Long id, PaymentStatus status, String transactionId) {
        Reservation reservation = getReservationById(id);
        reservation.setPaymentStatus(status);
        reservation.setPaymentTransactionId(transactionId);
        if (status == PaymentStatus.COMPLETED) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setConfirmedAt(LocalDateTime.now());
            
            if (reservation.getEvent() != null) {
                chatRoomRepository.findByRelatedEntity("EVENT", reservation.getEvent().getId())
                    .ifPresent(room -> {
                        try {
                            chatRoomService.addMember(room.getId(), room.getCreator().getId(), reservation.getUser().getId());
                        } catch (Exception e) {
                            // user might already be a member
                        }
                    });
            }
        }
        return reservationRepository.save(reservation);
    }
}
