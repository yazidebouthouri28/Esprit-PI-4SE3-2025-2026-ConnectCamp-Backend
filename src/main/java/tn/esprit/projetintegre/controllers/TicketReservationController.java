package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.response.TicketReservationResponse;
import tn.esprit.projetintegre.services.TicketReservationService;

@RestController
@RequestMapping("/api/ticket-reservations")
@RequiredArgsConstructor
@Tag(name = "Ticket Reservations", description = "Ticket reservation endpoints for events")
@SecurityRequirement(name = "Bearer Authentication")
public class TicketReservationController {

        private final TicketReservationService ticketReservationService;

        @PostMapping("/event")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Reserve tickets for an event")
        public ResponseEntity<ApiResponse<TicketReservationResponse>> reserveEventTickets(
                        @RequestParam Long userId,
                        @RequestParam Long eventId,
                        @RequestParam(defaultValue = "1") int quantity,
                        @RequestParam(required = false) String notes) {
                TicketReservationResponse reservation = ticketReservationService
                                .createEventTicketReservation(userId, eventId, quantity, notes);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Ticket reservation created successfully", reservation));
        }
}
