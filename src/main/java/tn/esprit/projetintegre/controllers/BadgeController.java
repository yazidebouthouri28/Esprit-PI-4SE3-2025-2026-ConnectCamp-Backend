package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.entities.Badge;
import tn.esprit.projetintegre.services.BadgeService;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
@Tag(name = "Badges", description = "Badge management & event assignment")
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    @Operation(summary = "Get all badges")
    public List<Badge> getAll() {
        return badgeService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get badge by ID")
    public ResponseEntity<Badge> getById(@PathVariable Long id) {
        return badgeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new badge (Admin only)")
    public Badge create(@RequestBody Badge badge) {
        return badgeService.create(badge);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update badge (Admin only)")
    public ResponseEntity<Badge> update(@PathVariable Long id, @RequestBody Badge badgeDetails) {
        try {
            return ResponseEntity.ok(badgeService.update(id, badgeDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete badge (Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        badgeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{badgeId}/assign/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Assign a badge to an event")
    public ResponseEntity<ApiResponse<Void>> assignToEvent(@PathVariable Long badgeId,
            @PathVariable Long eventId) {
        badgeService.assignBadgeToEvent(badgeId, eventId);
        return ResponseEntity.ok(ApiResponse.success("Badge assigned to event", null));
    }

    @DeleteMapping("/{badgeId}/unassign/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Remove a badge from an event")
    public ResponseEntity<ApiResponse<Void>> unassignFromEvent(@PathVariable Long badgeId,
            @PathVariable Long eventId) {
        badgeService.removeBadgeFromEvent(badgeId, eventId);
        return ResponseEntity.ok(ApiResponse.success("Badge removed from event", null));
    }
}
