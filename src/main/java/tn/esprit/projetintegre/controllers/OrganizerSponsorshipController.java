package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.SponsorshipRequest;
import tn.esprit.projetintegre.dto.response.SponsorshipResponse;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Sponsor;
import tn.esprit.projetintegre.entities.Sponsorship;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.OrganizerRepository;
import tn.esprit.projetintegre.repositories.SponsorRepository;
import tn.esprit.projetintegre.repositories.SponsorshipRepository;
import tn.esprit.projetintegre.repositories.UserRepository;
import tn.esprit.projetintegre.services.SponsorshipEmailService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizer/sponsorships")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
@Tag(name = "Organizer Sponsorships", description = "Organizer sponsorship management APIs")
public class OrganizerSponsorshipController {

    private final SponsorshipRepository sponsorshipRepository;
    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;
    private final SponsorRepository sponsorRepository;
    private final DtoMapper dtoMapper;
    private final SponsorshipEmailService sponsorshipEmailService;

    /**
     * Get all sponsorships for events organized by this organizer
     */
    @GetMapping("/organizer/{organizerId}")
    @Operation(summary = "Get all sponsorships for organizer's events")
    public ResponseEntity<ApiResponse<List<SponsorshipResponse>>> getSponsorshipsForOrganizer(
            @PathVariable Long organizerId,
            @RequestParam(value = "status", required = false) String status,
            Authentication authentication) {

        // Resolve the current user from authentication
        User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }

        // Verify the authenticated user is the organizer
        if (!isAuthorizedOrganizer(currentUser, organizerId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You can only view sponsorships for your own events"));
        }

        // Get all events organized by this organizer
        List<Event> events = eventRepository.findByOrganizerId(organizerId, org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        if (events.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(java.util.Collections.emptyList()));
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        // Get all sponsorships for these events using repository method that fetches details
        List<Sponsorship> sponsorships = sponsorshipRepository.findAllWithDetails(
                org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .filter(s -> eventIds.contains(s.getEvent().getId()))
                .filter(s -> status == null || status.isEmpty() || status.equals(s.getStatus()))
                .collect(Collectors.toList());

        List<SponsorshipResponse> response = dtoMapper.toSponsorshipResponseList(sponsorships);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Assign/invite a sponsor to an event (organizer-initiated)
     */
    @PostMapping("/assign")
    @Operation(summary = "Invite a sponsor to sponsor an event")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> assignSponsorship(
            @RequestParam("sponsorId") Long sponsorId,
            @RequestParam("eventId") Long eventId,
            @Valid @RequestBody SponsorshipRequest request,
            Authentication authentication) {

        User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }

        // Verify the event belongs to this organizer
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        if (!isEventOrganizedByUser(eventId, currentUser.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You can only invite sponsors to your own events"));
        }

        Sponsor sponsor = sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));

        // Create sponsorship with PENDING status (waiting for sponsor acceptance)
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
                .status("PENDING")
                .isActive(true)
                .isPaid(false)
                .build();

        Sponsorship saved = sponsorshipRepository.save(sponsorship);

        // Send email invitation to sponsor
        try {
            sponsorshipEmailService.sendSponsorshipRequestEmail(saved);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Sponsorship invitation sent successfully. Request email sent to sponsor.",
                dtoMapper.toSponsorshipResponse(saved)));
    }

    /**
     * Approve a sponsorship request (from sponsor)
     */
    @PostMapping("/{sponsorshipId}/approve")
    @Operation(summary = "Approve a sponsorship request from a sponsor")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> approveSponsorshipRequest(
            @PathVariable Long sponsorshipId,
            Authentication authentication) {

        User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }
        
        Sponsorship sponsorship = sponsorshipRepository.findByIdWithDetails(sponsorshipId)
                .orElseThrow(() -> new RuntimeException("Sponsorship not found"));

        // Verify this sponsorship is for an event organized by this user
        Event event = sponsorship.getEvent();
        if (!isEventOrganizedByUser(event.getId(), currentUser.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You can only approve sponsorships for your own events"));
        }

        // Verify the status is REQUESTED
        if (!"REQUESTED".equals(sponsorship.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only REQUESTED sponsorships can be approved"));
        }

        // Change status to ACCEPTED
        sponsorship.setStatus("ACCEPTED");
        Sponsorship saved = sponsorshipRepository.save(sponsorship);

        // Reload with full details to avoid Hibernate proxy initialization issues
        Sponsorship reloaded = sponsorshipRepository.findByIdWithDetails(saved.getId())
                .orElse(saved);

        // Send email notification to sponsor
        try {
            // Send invitation email with PDF attachment
            sponsorshipEmailService.sendSponsorshipRequestEmail(reloaded);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("Sponsorship approved and invitation sent to sponsor",
                dtoMapper.toSponsorshipResponse(reloaded)));
    }

    /**
     * Reject a sponsorship request (from sponsor)
     */
    @PostMapping("/{sponsorshipId}/reject")
    @Operation(summary = "Reject a sponsorship request from a sponsor")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> rejectSponsorshipRequest(
            @PathVariable Long sponsorshipId,
            Authentication authentication) {

        User currentUser = resolveCurrentUser(authentication);
        if (currentUser == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated"));
        }
        
        Sponsorship sponsorship = sponsorshipRepository.findByIdWithDetails(sponsorshipId)
                .orElseThrow(() -> new RuntimeException("Sponsorship not found"));

        // Verify this sponsorship is for an event organized by this user
        Event event = sponsorship.getEvent();
        if (!isEventOrganizedByUser(event.getId(), currentUser.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You can only reject sponsorships for your own events"));
        }

        // Verify the status is REQUESTED
        if (!"REQUESTED".equals(sponsorship.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only REQUESTED sponsorships can be rejected"));
        }

        // Change status to DECLINED
        sponsorship.setStatus("DECLINED");
        Sponsorship saved = sponsorshipRepository.save(sponsorship);

        // Reload with full details to avoid Hibernate proxy initialization issues
        Sponsorship reloaded = sponsorshipRepository.findByIdWithDetails(saved.getId())
                .orElse(saved);

        // Send email notification to sponsor
        try {
            sponsorshipEmailService.sendSponsorshipDeclinedEmail(reloaded);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("Sponsorship request rejected",
                dtoMapper.toSponsorshipResponse(reloaded)));
    }

    /**
     * Helper method to check if user is authorized as the organizer
     */
    private boolean isAuthorizedOrganizer(User user, Long organizerId) {
        return organizerRepository.findById(organizerId)
                .map(org -> org.getUser() != null && org.getUser().getId().equals(user.getId()))
                .orElse(false);
    }

    /**
     * Helper method to check if event is organized by this user
     */
    private boolean isEventOrganizedByUser(Long eventId, Long userId) {
        return eventRepository.findById(eventId)
                .map(event -> event.getOrganizer() != null && 
                              event.getOrganizer().getUser() != null && 
                              event.getOrganizer().getUser().getId().equals(userId))
                .orElse(false);
    }

    /**
     * Resolve current user from authentication (handles both User entity and Spring Security UserDetails)
     */
    private User resolveCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User securityUser = 
                (org.springframework.security.core.userdetails.User) principal;
            return userRepository.findByUsername(securityUser.getUsername()).orElse(null);
        } else if (principal instanceof String) {
            return userRepository.findByUsername((String) principal).orElse(null);
        }

        return null;
    }
}
