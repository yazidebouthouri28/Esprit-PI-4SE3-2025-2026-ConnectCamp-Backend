package tn.esprit.projetintegre.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.projetintegre.enums.PaymentStatus;
import tn.esprit.projetintegre.enums.ReservationStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reservationNumber;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    @NotNull(message = "Site is required")
    private Site site;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @NotNull(message = "Check-in date is required")
    private LocalDateTime checkInDate;

    @NotNull(message = "Check-out date is required")
    private LocalDateTime checkOutDate;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least 1 guest is required")
    private Integer numberOfGuests;
    
    @NotNull(message = "Number of nights is required")
    @Min(value = 1, message = "At least 1 night is required")
    private Integer numberOfNights;

    @Column(precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Price per night must be positive")
    private BigDecimal pricePerNight;

    @Column(precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Total price must be positive")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String paymentMethod;
    private String paymentTransactionId;

    @Column(length = 1000)
    private String specialRequests;

    private String guestName;
    private String guestEmail;
    private String guestPhone;

    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (reservationNumber == null) {
            reservationNumber = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
