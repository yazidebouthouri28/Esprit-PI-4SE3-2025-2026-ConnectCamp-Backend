package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetintegre.entities.Event;
import tn.esprit.projetintegre.entities.Sponsor;
import tn.esprit.projetintegre.entities.Sponsorship;
import tn.esprit.projetintegre.enums.SponsorTier;
import tn.esprit.projetintegre.exception.DuplicateResourceException;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.EventRepository;
import tn.esprit.projetintegre.repositories.SponsorRepository;
import tn.esprit.projetintegre.repositories.SponsorshipRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;

import tn.esprit.projetintegre.dto.response.SponsorDashboardResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class SponsorService {

    private final SponsorRepository sponsorRepository;
    private final SponsorshipRepository sponsorshipRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Sponsor methods
    public List<Sponsor> getAllSponsors() {
        return sponsorRepository.findAll();
    }

    public Page<Sponsor> getAllSponsors(Pageable pageable) {
        return sponsorRepository.findAll(pageable);
    }

    public Sponsor getSponsorById(Long id) {
        return sponsorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsor not found with id: " + id));
    }

    public List<Sponsor> getActiveSponsors() {
        return sponsorRepository.findByIsActiveTrue();
    }

    public List<Sponsor> searchSponsors(String keyword) {
        return sponsorRepository.searchByKeyword(keyword);
    }

    public List<Sponsor> filterSponsors(SponsorTier tier, Boolean isActive, String location) {
        return sponsorRepository.filterSponsors(tier, isActive, location);
    }

    public Sponsor createSponsor(Sponsor sponsor) {
        if (sponsor.getEmail() != null && sponsorRepository.existsByEmail(sponsor.getEmail())) {
            throw new DuplicateResourceException("Sponsor with this email already exists");
        }
        sponsor.setIsActive(true);
        return sponsorRepository.save(sponsor);
    }

    public Sponsor updateSponsor(Long id, Sponsor sponsorDetails) {
        Sponsor sponsor = getSponsorById(id);
        sponsor.setName(sponsorDetails.getName());
        sponsor.setDescription(sponsorDetails.getDescription());
        sponsor.setLogo(sponsorDetails.getLogo());
        sponsor.setWebsite(sponsorDetails.getWebsite());
        sponsor.setEmail(sponsorDetails.getEmail());
        sponsor.setPhone(sponsorDetails.getPhone());
        sponsor.setAddress(sponsorDetails.getAddress());
        sponsor.setCity(sponsorDetails.getCity());
        sponsor.setCountry(sponsorDetails.getCountry());
        sponsor.setContactPerson(sponsorDetails.getContactPerson());
        sponsor.setContactPosition(sponsorDetails.getContactPosition());
        sponsor.setNotes(sponsorDetails.getNotes());
        sponsor.setTier(sponsorDetails.getTier() != null ? sponsorDetails.getTier() : SponsorTier.BRONZE);
        sponsor.setIsActive(
                sponsorDetails.getIsActive() != null ? sponsorDetails.getIsActive() : sponsor.getIsActive());

        Sponsor updatedSponsor = sponsorRepository.save(sponsor);

        // Sync logo with user avatar
        if (sponsor.getEmail() != null && sponsor.getLogo() != null) {
            userRepository.findByEmail(sponsor.getEmail()).ifPresent(user -> {
                user.setAvatar(sponsor.getLogo());
                userRepository.save(user);
            });
        }

        return updatedSponsor;
    }

    public void deleteSponsor(Long id) {
        Sponsor sponsor = getSponsorById(id);
        sponsor.setIsActive(false);
        sponsorRepository.save(sponsor);
    }

    // Sponsorship methods
    public List<Sponsorship> getAllSponsorships() {
        return sponsorshipRepository.findAll();
    }

    public Page<Sponsorship> getAllSponsorships(Pageable pageable) {
        return sponsorshipRepository.findAll(pageable);
    }

    public Sponsorship getSponsorshipById(Long id) {
        return sponsorshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sponsorship not found with id: " + id));
    }

    public List<Sponsorship> getSponsorshipsBySponsorId(Long sponsorId) {
        return sponsorshipRepository.findBySponsor_Id(sponsorId);
    }

    public List<Sponsorship> getSponsorshipsByEventId(Long eventId) {
        return sponsorshipRepository.findByEvent_Id(eventId);
    }

    public Sponsorship createSponsorship(Sponsorship sponsorship, Long sponsorId, Long eventId) {
        Sponsor sponsor = getSponsorById(sponsorId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        sponsorship.setSponsor(sponsor);
        sponsorship.setEvent(event);
        sponsorship.setIsActive(true);
        sponsorship.setIsPaid(false);
        sponsorship.setStatus("PENDING");
        return sponsorshipRepository.save(sponsorship);
    }

    public Sponsorship updateSponsorship(Long id, Sponsorship sponsorshipDetails) {
        Sponsorship sponsorship = getSponsorshipById(id);
        sponsorship.setSponsorshipType(sponsorshipDetails.getSponsorshipType());
        sponsorship.setSponsorshipLevel(sponsorshipDetails.getSponsorshipLevel());
        sponsorship.setDescription(sponsorshipDetails.getDescription());
        sponsorship.setAmount(sponsorshipDetails.getAmount());
        sponsorship.setCurrency(sponsorshipDetails.getCurrency());
        sponsorship.setStartDate(sponsorshipDetails.getStartDate());
        sponsorship.setEndDate(sponsorshipDetails.getEndDate());
        sponsorship.setBenefits(sponsorshipDetails.getBenefits());
        sponsorship.setDeliverables(sponsorshipDetails.getDeliverables());
        sponsorship.setNotes(sponsorshipDetails.getNotes());
        return sponsorshipRepository.save(sponsorship);
    }

    public Sponsorship markAsPaid(Long id) {
        Sponsorship sponsorship = getSponsorshipById(id);
        sponsorship.setIsPaid(true);
        sponsorship.setPaidAt(LocalDateTime.now());
        sponsorship.setStatus("PAID");
        return sponsorshipRepository.save(sponsorship);
    }

    public Sponsorship updateStatus(Long id, String status) {
        Sponsorship sponsorship = getSponsorshipById(id);
        sponsorship.setStatus(status);
        return sponsorshipRepository.save(sponsorship);
    }

    public Sponsorship acceptSponsorship(Long id) {
        Sponsorship sponsorship = getSponsorshipById(id);
        sponsorship.setStatus("ACCEPTED");
        return sponsorshipRepository.save(sponsorship);
    }

    public Sponsorship declineSponsorship(Long id) {
        Sponsorship sponsorship = getSponsorshipById(id);
        sponsorship.setStatus("DECLINED");
        sponsorship.setIsActive(false);
        return sponsorshipRepository.save(sponsorship);
    }

    public void deleteSponsorship(Long id) {
        Sponsorship sponsorship = getSponsorshipById(id);
        sponsorship.setIsActive(false);
        sponsorshipRepository.save(sponsorship);
    }

    /**
     * Get sponsor performance dashboard data
     * Calculates composite score based on 3 criteria (33% each):
     * - Normalized total amount
     * - Normalized number of distinct events
     * - Normalized tenure (days since sponsor created)
     */
    public List<SponsorDashboardResponse> getSponsorDashboard() {
        List<Sponsor> sponsors = sponsorRepository.findAll();
        List<SponsorDashboardResponse> dashboardData = new ArrayList<>();

        // First pass: calculate all metrics and find max values for normalization
        BigDecimal maxTotalAmount = BigDecimal.ZERO;
        long maxEventsCount = 0;
        long maxTenureDays = 0;

        List<SponsorMetrics> metricsList = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (Sponsor sponsor : sponsors) {
            List<Sponsorship> sponsorships = sponsorshipRepository.findBySponsorIdWithEvent(sponsor.getId());

            BigDecimal totalAmount = BigDecimal.ZERO;
            long totalCount = sponsorships.size();

            // Track distinct events
            Set<Long> distinctEventIds = new HashSet<>();

            for (Sponsorship s : sponsorships) {
                if (s.getAmount() != null) {
                    totalAmount = totalAmount.add(s.getAmount());
                }

                // Get event
                Event event = s.getEvent();
                if (event != null) {
                    distinctEventIds.add(event.getId());
                }
            }

            // Calculate average amount per event
            long distinctEventsCount = distinctEventIds.size();
            double averageAmountPerEvent = distinctEventsCount > 0
                    ? totalAmount.doubleValue() / distinctEventsCount
                    : 0.0;

            // Calculate tenure (days since sponsor created)
            long tenureDays = 0;
            if (sponsor.getCreatedAt() != null) {
                tenureDays = java.time.temporal.ChronoUnit.DAYS.between(sponsor.getCreatedAt(), now);
            }

            // Store metrics for second pass
            metricsList.add(new SponsorMetrics(
                    sponsor, totalAmount, totalCount, distinctEventsCount,
                    averageAmountPerEvent, tenureDays
            ));

            // Track max values for normalization
            if (totalAmount.compareTo(maxTotalAmount) > 0) {
                maxTotalAmount = totalAmount;
            }
            if (distinctEventsCount > maxEventsCount) {
                maxEventsCount = distinctEventsCount;
            }
            if (tenureDays > maxTenureDays) {
                maxTenureDays = tenureDays;
            }
        }

        // Second pass: calculate composite scores (33% each criteria)
        for (SponsorMetrics metrics : metricsList) {
            Sponsor sponsor = metrics.sponsor;

            // Calculate normalized scores (0-100 each)
            double amountScore = maxTotalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? metrics.totalAmount.multiply(BigDecimal.valueOf(100))
                            .divide(maxTotalAmount, 2, java.math.RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            double eventsScore = maxEventsCount > 0
                    ? (metrics.distinctEventsCount * 100.0) / maxEventsCount
                    : 0.0;

            double tenureScore = maxTenureDays > 0
                    ? (metrics.tenureDays * 100.0) / maxTenureDays
                    : 0.0;

            // Composite score: 3 criteria, 33% each
            int compositeScore = (int) Math.round(
                    (amountScore * 0.33) +
                    (eventsScore * 0.33) +
                    (tenureScore * 0.34)
            );

            // Status label based on composite score
            String statusLabel = compositeScore >= 70 ? "High Performer" : "Needs Improvement";

            var response = SponsorDashboardResponse.builder()
                    .sponsorId(sponsor.getId())
                    .sponsorName(sponsor.getName())
                    .tier(sponsor.getTier() != null ? sponsor.getTier().name() : "BRONZE")
                    .isActive(sponsor.getIsActive())
                    .totalAmountSponsored(metrics.totalAmount)
                    .averageAmountPerEvent(metrics.averageAmountPerEvent)
                    .averageEventDurationDays((double) metrics.distinctEventsCount)
                    .totalSponsorships(metrics.totalCount)
                    .distinctEventsCount(metrics.distinctEventsCount)
                    .tenureDays(metrics.tenureDays)
                    .compositeScore(compositeScore)
                    .statusLabel(statusLabel)
                    .build();

            dashboardData.add(response);
        }

        // Sort by composite score descending
        dashboardData.sort(Comparator.comparingInt(SponsorDashboardResponse::getCompositeScore).reversed());

        return dashboardData;
    }

    // Helper class to store intermediate metrics
    @AllArgsConstructor
    private static class SponsorMetrics {
        Sponsor sponsor;
        BigDecimal totalAmount;
        long totalCount;
        long distinctEventsCount;
        double averageAmountPerEvent;
        long tenureDays;
    }
}
