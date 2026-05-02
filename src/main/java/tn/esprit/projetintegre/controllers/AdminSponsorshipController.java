package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.dto.request.SponsorshipRequest;
import tn.esprit.projetintegre.dto.response.SponsorshipResponse;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Sponsor;
import tn.esprit.projetintegre.entities.Sponsorship;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.SponsorRepository;
import tn.esprit.projetintegre.repositories.SponsorshipRepository;
import tn.esprit.projetintegre.services.SponsorshipEmailService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/sponsorships")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
@Tag(name = "Admin Sponsorships", description = "Admin sponsorship management APIs")
public class AdminSponsorshipController {

    private final SponsorRepository sponsorRepository;
    private final EventRepository eventRepository;
    private final SponsorshipRepository sponsorshipRepository;
    private final SponsorshipEmailService sponsorshipEmailService;
    private final DtoMapper dtoMapper;

    /**
     * Assign an event to a sponsor and send email notification with PDF
     */
    @PostMapping("/assign")
    @Operation(summary = "Assign event to sponsor and send request email")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> assignEventToSponsor(
            @RequestParam("sponsorId") Long sponsorId,
            @RequestParam("eventId") Long eventId,
            @Valid @RequestBody SponsorshipRequest request) {

        Sponsor sponsor = sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Create sponsorship
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

        // Send email with PDF
        sponsorshipEmailService.sendSponsorshipRequestEmail(saved);

        return ResponseEntity.ok(ApiResponse.success(
                "Sponsorship assigned successfully. Request email sent to sponsor.",
                dtoMapper.toSponsorshipResponse(saved)));
    }

    /**
     * Get all sponsorships with status filtering
     */
    @GetMapping
    @Operation(summary = "Get all sponsorships with optional status filter")
    public ResponseEntity<ApiResponse<PageResponse<SponsorshipResponse>>> getAllSponsorships(
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable) {

        Page<Sponsorship> page;
        if (status != null && !status.isEmpty()) {
            page = sponsorshipRepository.findByStatus(status, pageable);
        } else {
            page = sponsorshipRepository.findAllWithDetails(pageable);
        }

        Page<SponsorshipResponse> response = page.map(dtoMapper::toSponsorshipResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    /**
     * Get sponsorships by specific status
     */
    @GetMapping("/by-status/{status}")
    @Operation(summary = "Get sponsorships by status")
    public ResponseEntity<ApiResponse<List<SponsorshipResponse>>> getSponsorshipsByStatus(
            @PathVariable("status") String status) {

        List<Sponsorship> sponsorships = sponsorshipRepository.findByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(
                dtoMapper.toSponsorshipResponseList(sponsorships)));
    }

    /**
     * Get pending sponsorships (awaiting sponsor response)
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending sponsorships awaiting sponsor response")
    public ResponseEntity<ApiResponse<List<SponsorshipResponse>>> getPendingSponsorships() {
        List<Sponsorship> sponsorships = sponsorshipRepository.findByStatus("PENDING");
        return ResponseEntity.ok(ApiResponse.success(
                dtoMapper.toSponsorshipResponseList(sponsorships)));
    }

    /**
     * Get accepted sponsorships
     */
    @GetMapping("/accepted")
    @Operation(summary = "Get accepted sponsorships")
    public ResponseEntity<ApiResponse<List<SponsorshipResponse>>> getAcceptedSponsorships() {
        List<Sponsorship> sponsorships = sponsorshipRepository.findByStatus("ACCEPTED");
        return ResponseEntity.ok(ApiResponse.success(
                dtoMapper.toSponsorshipResponseList(sponsorships)));
    }

    /**
     * Get declined sponsorships
     */
    @GetMapping("/declined")
    @Operation(summary = "Get declined sponsorships")
    public ResponseEntity<ApiResponse<List<SponsorshipResponse>>> getDeclinedSponsorships() {
        List<Sponsorship> sponsorships = sponsorshipRepository.findByStatus("DECLINED");
        return ResponseEntity.ok(ApiResponse.success(
                dtoMapper.toSponsorshipResponseList(sponsorships)));
    }

    /**
     * Get available events for sponsorship assignment
     */
    @GetMapping("/available-events")
    @Operation(summary = "Get available events for sponsorship assignment")
    public ResponseEntity<ApiResponse<List<tn.esprit.projetintegre.dto.response.EventResponse>>> getAvailableEvents() {
        List<Event> events = eventRepository.findAllWithDetails();
        List<tn.esprit.projetintegre.dto.response.EventResponse> eventResponses = events.stream()
                .map(dtoMapper::toEventResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(eventResponses));
    }

    /**
     * Get available sponsors for event assignment
     */
    @GetMapping("/available-sponsors")
    @Operation(summary = "Get available sponsors for event assignment")
    public ResponseEntity<ApiResponse<List<tn.esprit.projetintegre.dto.response.SponsorResponse>>> getAvailableSponsors() {
        List<Sponsor> sponsors = sponsorRepository.findByIsActiveTrue();
        List<tn.esprit.projetintegre.dto.response.SponsorResponse> sponsorResponses = sponsors.stream()
                .map(dtoMapper::toSponsorResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(sponsorResponses));
    }

    /**
     * Get possible sponsorship matches for an organizer
     */
    @GetMapping("/possible-matches")
    @Operation(summary = "Get possible sponsorship matches for organizer's events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<List<tn.esprit.projetintegre.dto.response.SponsorshipMatchResponse>>> getPossibleMatches(
            @RequestParam(value = "organizerId", required = false) Long organizerId) {
        
        // Get all active sponsors
        List<Sponsor> sponsors = sponsorRepository.findByIsActiveTrue();
        
        // Get events - if organizerId is provided, filter by organizer
        List<Event> events;
        if (organizerId != null) {
            events = eventRepository.findByOrganizerId(organizerId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else {
            events = eventRepository.findAllWithDetails();
        }
        
        // Generate possible matches based on simple logic
        // In a real implementation, this would use more sophisticated matching algorithms
        List<tn.esprit.projetintegre.dto.response.SponsorshipMatchResponse> matches = new java.util.ArrayList<>();
        
        for (Event event : events) {
            for (Sponsor sponsor : sponsors) {
                // Simple matching logic: match based on sponsor tier and event type
                int matchScore = calculateMatchScore(event, sponsor);
                if (matchScore >= 50) { // Only include matches with 50% or higher
                    matches.add(tn.esprit.projetintegre.dto.response.SponsorshipMatchResponse.builder()
                            .eventTitle(event.getTitle())
                            .eventId(event.getId())
                            .sponsorName(sponsor.getName())
                            .sponsorId(sponsor.getId())
                            .sponsorTier(sponsor.getTier() != null ? sponsor.getTier().name() : null)
                            .category(event.getCategory() != null ? event.getCategory() : "General")
                            .estimatedValue(calculateEstimatedValue(sponsor.getTier() != null ? sponsor.getTier().name() : null))
                            .matchScore(matchScore)
                            .build());
                }
            }
        }
        
        // Sort by match score descending
        matches.sort((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore()));
        
        // Limit to top 20 matches
        if (matches.size() > 20) {
            matches = matches.subList(0, 20);
        }
        
        return ResponseEntity.ok(ApiResponse.success(matches));
    }
    
    private int calculateMatchScore(Event event, Sponsor sponsor) {
        int score = 50; // Base score
        
        // Increase score based on sponsor tier
        if (sponsor.getTier() != null) {
            String tierName = sponsor.getTier().name();
            switch (tierName) {
                case "PLATINUM":
                case "DIAMOND":
                    score += 30;
                    break;
                case "GOLD":
                    score += 25;
                    break;
                case "SILVER":
                    score += 20;
                    break;
                case "BRONZE":
                    score += 15;
                    break;
                default:
                    score += 10;
            }
        }
        
        // Add some randomness to simulate real matching
        score += (int) (Math.random() * 10);
        
        return Math.min(score, 100);
    }
    
    private String calculateEstimatedValue(String tier) {
        if (tier == null) return "1,000 TND";
        switch (tier) {
            case "PLATINUM":
            case "DIAMOND":
                return "10,000+ TND";
            case "GOLD":
                return "5,000 TND";
            case "SILVER":
                return "2,500 TND";
            case "BRONZE":
                return "1,000 TND";
            default:
                return "1,000 TND";
        }
    }

    /**
     * Update sponsorship after sponsor accepts (send confirmation email)
     */
    @PostMapping("/{id}/confirm-acceptance")
    @Operation(summary = "Confirm sponsorship acceptance and send confirmation email")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> confirmAcceptance(@PathVariable("id") Long id) {
        Sponsorship sponsorship = sponsorshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sponsorship not found"));

        sponsorship.setStatus("ACCEPTED");
        Sponsorship updated = sponsorshipRepository.save(sponsorship);

        // Send acceptance confirmation email with PDF
        sponsorshipEmailService.sendSponsorshipAcceptedEmail(updated);

        return ResponseEntity.ok(ApiResponse.success(
                "Sponsorship accepted. Confirmation email sent to sponsor.",
                dtoMapper.toSponsorshipResponse(updated)));
    }

    /**
     * Update sponsorship after sponsor declines
     */
    @PostMapping("/{id}/confirm-declined")
    @Operation(summary = "Confirm sponsorship declined")
    public ResponseEntity<ApiResponse<SponsorshipResponse>> confirmDeclined(@PathVariable("id") Long id) {
        Sponsorship sponsorship = sponsorshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sponsorship not found"));

        sponsorship.setStatus("DECLINED");
        sponsorship.setIsActive(false);
        Sponsorship updated = sponsorshipRepository.save(sponsorship);

        // Send declined notification email
        sponsorshipEmailService.sendSponsorshipDeclinedEmail(updated);

        return ResponseEntity.ok(ApiResponse.success(
                "Sponsorship declined. Notification sent to sponsor.",
                dtoMapper.toSponsorshipResponse(updated)));
    }

    /**
     * Get sponsorship statistics for dashboard
     */
    @GetMapping("/stats")
    @Operation(summary = "Get sponsorship statistics")
    public ResponseEntity<ApiResponse<SponsorshipStats>> getSponsorshipStats() {
        long pending = sponsorshipRepository.countByStatus("PENDING");
        long accepted = sponsorshipRepository.countByStatus("ACCEPTED");
        long declined = sponsorshipRepository.countByStatus("DECLINED");
        long total = sponsorshipRepository.count();

        // Calculate total amount for accepted sponsorships
        BigDecimal totalAmount = sponsorshipRepository.findByStatus("ACCEPTED").stream()
                .map(Sponsorship::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SponsorshipStats stats = SponsorshipStats.builder()
                .total(total)
                .pending(pending)
                .accepted(accepted)
                .declined(declined)
                .totalAcceptedAmount(totalAmount)
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // Inner class for stats
    @lombok.Data
    @lombok.Builder
    public static class SponsorshipStats {
        private long total;
        private long pending;
        private long accepted;
        private long declined;
        private BigDecimal totalAcceptedAmount;
    }
}
