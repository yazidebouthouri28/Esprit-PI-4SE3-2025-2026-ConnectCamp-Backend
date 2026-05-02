package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.SponsorshipRequest;
import tn.esprit.projetintegre.dto.response.EventResponse;
import tn.esprit.projetintegre.dto.response.SponsorshipResponse;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Sponsor;
import tn.esprit.projetintegre.entities.Sponsorship;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.repositories.ChatRoomRepository;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.SponsorRepository;
import tn.esprit.projetintegre.repositories.SponsorshipRepository;
import tn.esprit.projetintegre.services.ChatRoomService;
import tn.esprit.projetintegre.services.SponsorshipEmailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sponsor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SPONSOR') or hasRole('ADMIN')")
@Tag(name = "Sponsor Events", description = "Sponsor interface for viewing and requesting event sponsorships")
public class SponsorEventController {

    private final EventRepository eventRepository;
    private final SponsorRepository sponsorRepository;
    private final SponsorshipRepository sponsorshipRepository;
    private final tn.esprit.projetintegre.repositories.UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;
    private final DtoMapper dtoMapper;
    private final SponsorshipEmailService sponsorshipEmailService;

    /**
     * Get current sponsor for authenticated user
     */
    @GetMapping("/current")
    @Operation(summary = "Get current sponsor for authenticated user")
    public ResponseEntity<ApiResponse<Sponsor>> getCurrentSponsor(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User not authenticated"));
            }
            
            Object principal = authentication.getPrincipal();
            System.out.println("DEBUG - Authentication principal class: " + principal.getClass().getName());
            
            User user = null;
            String username = null;
            
            if (principal instanceof User) {
                // Our custom User entity is the principal
                user = (User) principal;
                System.out.println("DEBUG - Using custom User entity directly");
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                // Spring Security's UserDetails - need to look up our User entity
                org.springframework.security.core.userdetails.User securityUser = 
                    (org.springframework.security.core.userdetails.User) principal;
                username = securityUser.getUsername();
                System.out.println("DEBUG - Looking up user by username: " + username);
                user = userRepository.findByUsername(username).orElse(null);
            } else if (principal instanceof String) {
                // Username as string
                username = (String) principal;
                System.out.println("DEBUG - Principal is username string: " + username);
                user = userRepository.findByUsername(username).orElse(null);
            }
            
            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("User not found in database"));
            }
            
            System.out.println("DEBUG - User ID: " + user.getId() + ", Email: " + user.getEmail());
            
            // Try to find sponsor by userId first
            Sponsor sponsor = sponsorRepository.findByUserId(user.getId()).orElse(null);
            System.out.println("DEBUG - Sponsor by userId: " + (sponsor != null ? sponsor.getId() : "not found"));
            
            if (sponsor == null) {
                // Fallback to email
                sponsor = sponsorRepository.findByEmail(user.getEmail()).orElse(null);
                System.out.println("DEBUG - Sponsor by email: " + (sponsor != null ? sponsor.getId() : "not found"));
            }
            
            if (sponsor == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No sponsor found for this user. userId=" + user.getId() + ", email=" + user.getEmail()));
            }
            
            return ResponseEntity.ok(ApiResponse.success(sponsor));
        } catch (Exception e) {
            System.err.println("ERROR in getCurrentSponsor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiResponse.error("Internal error: " + e.getMessage()));
        }
    }

    /**
     * Get all available events that sponsors can request to sponsor
     */
    @GetMapping("/available-events")
    @Operation(summary = "Get all available events for sponsorship")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAvailableEvents() {
        // Get events that are upcoming (start date in the future)
        List<Event> events = eventRepository.findAll().stream()
                .filter(event -> event.getStartDate() != null &&
                        event.getStartDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        List<EventResponse> response = events.stream()
                .map(dtoMapper::toEventResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get my sponsorships (sponsor requests)
     */
    @GetMapping("/my-sponsorships")
    @Operation(summary = "Get sponsorships for the authenticated sponsor")
    public ResponseEntity<ApiResponse<List<SponsorshipResponse>>> getMySponsorships(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "sponsorId", required = false) Long sponsorId,
            Authentication authentication) {

        Long actualSponsorId = sponsorId;
        
        // If sponsorId not provided, try to get it from authenticated user
        if (actualSponsorId == null && authentication != null) {
            User user = (User) authentication.getPrincipal();
            // Try to find sponsor by userId (direct link)
            Sponsor sponsor = sponsorRepository.findByUserId(user.getId()).orElse(null);
            if (sponsor != null) {
                actualSponsorId = sponsor.getId();
            } else {
                // Fallback to email matching
                sponsor = sponsorRepository.findByEmail(user.getEmail()).orElse(null);
                if (sponsor != null) {
                    actualSponsorId = sponsor.getId();
                } else {
                    // Last fallback: use user's ID
                    actualSponsorId = user.getId();
                }
            }
        }

        if (actualSponsorId == null) {
            throw new RuntimeException("Unable to determine sponsor ID from authenticated user");
        }

        List<Sponsorship> sponsorships;
        if (status != null && !status.isEmpty()) {
            sponsorships = sponsorshipRepository.findBySponsor_IdAndStatus(actualSponsorId, status);
        } else {
            sponsorships = sponsorshipRepository.findBySponsor_Id(actualSponsorId);
        }

        List<SponsorshipResponse> response = dtoMapper.toSponsorshipResponseList(sponsorships);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Request to sponsor an event (Sponsor initiates)
     */
    @PostMapping("/request-sponsorship")
    @Operation(summary = "Request to sponsor an event")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> requestSponsorship(
            @RequestParam("sponsorId") Long sponsorId,
            @RequestParam("eventId") Long eventId,
            @Valid @RequestBody SponsorshipRequest request) {

        Sponsor sponsor = sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check if sponsorship already exists
        boolean exists = sponsorshipRepository.findBySponsor_Id(sponsorId).stream()
                .anyMatch(s -> s.getEvent().getId().equals(eventId) && s.getIsActive());
        if (exists) {
            throw new RuntimeException("Sponsorship request already exists for this event");
        }

        // Create sponsorship request - use REQUESTED status so it appears on admin side
        Sponsorship sponsorship = Sponsorship.builder()
                .sponsor(sponsor)
                .event(event)
                .sponsorshipType(request.getSponsorshipType())
                .sponsorshipLevel(request.getSponsorshipLevel())
                .description(request.getDescription())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .benefits(request.getBenefits())
                .deliverables(request.getDeliverables())
                .notes(request.getNotes())
                .status("REQUESTED") // Sponsor initiated - needs admin approval
                .isActive(true)
                .isPaid(false)
                .build();

        Sponsorship saved = sponsorshipRepository.save(sponsorship);

        return ResponseEntity.ok(ApiResponse.success(
                "Sponsorship request submitted successfully. Waiting for admin approval.",
                dtoMapper.toSponsorshipResponse(saved)));
    }

    /**
     * Accept a sponsorship invitation (assigned by admin)
     */
    @PostMapping("/sponsorships/{id}/accept")
    @Operation(summary = "Accept a sponsorship invitation")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> acceptSponsorshipInvitation(
            @PathVariable("id") Long id) {
        try {
            // Fetch sponsorship with eager loading of sponsor and event
            Sponsorship sponsorship = sponsorshipRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new RuntimeException("Sponsorship not found"));

            if (!"PENDING".equals(sponsorship.getStatus())) {
                throw new RuntimeException("Only pending sponsorships can be accepted");
            }

            sponsorship.setStatus("ACCEPTED");
            Sponsorship saved = sponsorshipRepository.save(sponsorship);

            // Reload with details for email and response
            final Sponsorship updated = sponsorshipRepository.findByIdWithDetails(saved.getId())
                    .orElse(saved);

            // Auto-join event group chat
            try {
                if (updated.getEvent() != null) {
                    chatRoomRepository.findByRelatedEntity("EVENT", updated.getEvent().getId())
                        .ifPresent(room -> {
                            try {
                                // Get the sponsor's user ID to add to chat
                                Long sponsorUserId = updated.getSponsor() != null
                                    ? updated.getSponsor().getUserId()
                                    : null;
                                if (sponsorUserId != null && !room.getCreator().getId().equals(sponsorUserId)) {
                                    chatRoomService.addMember(room.getId(), room.getCreator().getId(), sponsorUserId);
                                    System.out.println("Sponsor added to event chat room: " + room.getId());
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to add sponsor to chat room: " + e.getMessage());
                                // Continue even if chat join fails
                            }
                        });
                }
            } catch (Exception e) {
                System.err.println("Error auto-joining group chat: " + e.getMessage());
                // Continue even if chat join fails
            }

            // Send acceptance confirmation email with PDF
            try {
                sponsorshipEmailService.sendSponsorshipAcceptedEmail(updated);
            } catch (Exception e) {
                System.err.println("Failed to send acceptance email: " + e.getMessage());
                // Continue even if email fails
            }

            return ResponseEntity.ok(ApiResponse.success(
                    "Sponsorship accepted successfully. You have been added to the event group chat. Confirmation email with receipt sent.",
                    dtoMapper.toSponsorshipResponse(updated)));
        } catch (Exception e) {
            System.err.println("Error accepting sponsorship: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to accept sponsorship: " + e.getMessage(), e);
        }
    }

    /**
     * Decline a sponsorship invitation (assigned by admin)
     */
    @PostMapping("/sponsorships/{id}/decline")
    @Operation(summary = "Decline a sponsorship invitation")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> declineSponsorshipInvitation(
            @PathVariable("id") Long id,
            @RequestParam(value = "reason", required = false) String reason) {
        try {
            Sponsorship sponsorship = sponsorshipRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new RuntimeException("Sponsorship not found"));

            if (!"PENDING".equals(sponsorship.getStatus())) {
                throw new RuntimeException("Only pending sponsorships can be declined");
            }

            sponsorship.setStatus("DECLINED");
            sponsorship.setIsActive(false);
            if (reason != null) {
                sponsorship.setNotes(sponsorship.getNotes() + "\nDecline reason: " + reason);
            }

            Sponsorship updated = sponsorshipRepository.save(sponsorship);

            // Reload with details
            updated = sponsorshipRepository.findByIdWithDetails(updated.getId())
                    .orElse(updated);

            // Send declined notification email
            try {
                sponsorshipEmailService.sendSponsorshipDeclinedEmail(updated);
            } catch (Exception e) {
                System.err.println("Failed to send decline email: " + e.getMessage());
                // Continue even if email fails
            }

            return ResponseEntity.ok(ApiResponse.success(
                    "Sponsorship declined. Notification sent.",
                    dtoMapper.toSponsorshipResponse(updated)));
        } catch (Exception e) {
            System.err.println("Error declining sponsorship: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to decline sponsorship: " + e.getMessage(), e);
        }
    }

    /**
     * Get pending sponsorship invitations for a sponsor
     */
    @GetMapping("/invitations")
    @Operation(summary = "Get pending sponsorship invitations for a sponsor")
    public ResponseEntity<ApiResponse<List<SponsorshipResponse>>> getPendingInvitations(
            @RequestParam(value = "sponsorId", required = false) Long sponsorId,
            Authentication authentication) {

        Long actualSponsorId = sponsorId;
        
        // If sponsorId not provided, try to get it from authenticated user
        if (actualSponsorId == null && authentication != null) {
            User user = (User) authentication.getPrincipal();
            Sponsor sponsor = sponsorRepository.findByUserId(user.getId()).orElse(null);
            if (sponsor != null) {
                actualSponsorId = sponsor.getId();
            } else {
                sponsor = sponsorRepository.findByEmail(user.getEmail()).orElse(null);
                if (sponsor != null) {
                    actualSponsorId = sponsor.getId();
                } else {
                    actualSponsorId = user.getId();
                }
            }
        }

        if (actualSponsorId == null) {
            throw new RuntimeException("Unable to determine sponsor ID from authenticated user");
        }

        List<Sponsorship> invitations = sponsorshipRepository.findBySponsor_IdAndStatus(actualSponsorId, "PENDING");
        return ResponseEntity.ok(ApiResponse.success(
                dtoMapper.toSponsorshipResponseList(invitations)));
    }

    /**
     * Get sponsor dashboard statistics
     */
    @GetMapping("/dashboard-stats")
    @Operation(summary = "Get sponsor dashboard statistics")
    public ResponseEntity<ApiResponse<SponsorDashboardStats>> getDashboardStats(
            @RequestParam(value = "sponsorId", required = false) Long sponsorId,
            Authentication authentication) {

        Long actualSponsorId = sponsorId;
        
        // If sponsorId not provided, try to get it from authenticated user
        if (actualSponsorId == null && authentication != null) {
            User user = (User) authentication.getPrincipal();
            Sponsor sponsor = sponsorRepository.findByUserId(user.getId()).orElse(null);
            if (sponsor != null) {
                actualSponsorId = sponsor.getId();
            } else {
                sponsor = sponsorRepository.findByEmail(user.getEmail()).orElse(null);
                if (sponsor != null) {
                    actualSponsorId = sponsor.getId();
                } else {
                    actualSponsorId = user.getId();
                }
            }
        }

        if (actualSponsorId == null) {
            throw new RuntimeException("Unable to determine sponsor ID from authenticated user");
        }

        List<Sponsorship> allSponsorships = sponsorshipRepository.findBySponsor_Id(actualSponsorId);

        long pending = allSponsorships.stream().filter(s -> "PENDING".equals(s.getStatus())).count();
        long accepted = allSponsorships.stream().filter(s -> "ACCEPTED".equals(s.getStatus())).count();
        long declined = allSponsorships.stream().filter(s -> "DECLINED".equals(s.getStatus())).count();
        long totalActive = allSponsorships.stream().filter(Sponsorship::getIsActive).count();

        BigDecimal totalAmount = allSponsorships.stream()
                .filter(s -> "ACCEPTED".equals(s.getStatus()) || "PAID".equals(s.getStatus()))
                .map(Sponsorship::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SponsorDashboardStats stats = SponsorDashboardStats.builder()
                .totalSponsorships(allSponsorships.size())
                .pendingInvitations(pending)
                .acceptedSponsorships(accepted)
                .declinedSponsorships(declined)
                .activeSponsorships(totalActive)
                .totalAmountCommitted(totalAmount)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Download sponsorship agreement PDF
     */
    @GetMapping("/sponsorships/{id}/agreement-pdf")
    @Operation(summary = "Download sponsorship agreement PDF")
    public ResponseEntity<byte[]> downloadSponsorshipPDF(@PathVariable("id") Long id) {
        try {
            Sponsorship sponsorship = sponsorshipRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new RuntimeException("Sponsorship not found"));

            // Verify sponsorship is in accepted or paid status
            if (!"ACCEPTED".equals(sponsorship.getStatus()) && !"PAID".equals(sponsorship.getStatus())) {
                return ResponseEntity.badRequest()
                        .body("Sponsorship must be accepted before downloading agreement".getBytes());
            }

            byte[] pdfBytes = sponsorshipEmailService.generateSponsorshipReceipt(sponsorship);

            String filename = String.format("sponsorship-agreement-%d-%s.pdf",
                    sponsorship.getId(),
                    sponsorship.getSponsor().getName().replaceAll("[^a-zA-Z0-9]", "_"));

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(pdfBytes);

        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }

    // Inner classes
    @lombok.Data
    @lombok.Builder
    public static class SponsorDashboardStats {
        private long totalSponsorships;
        private long pendingInvitations;
        private long acceptedSponsorships;
        private long declinedSponsorships;
        private long activeSponsorships;
        private BigDecimal totalAmountCommitted;
    }
}
