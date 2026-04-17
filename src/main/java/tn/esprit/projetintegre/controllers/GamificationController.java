package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.request.BadgeRequest;
import tn.esprit.projetintegre.dto.request.MedalRequest;
import tn.esprit.projetintegre.dto.response.BadgeResponse;
import tn.esprit.projetintegre.dto.response.MedalResponse;
import tn.esprit.projetintegre.entities.Badge;
import tn.esprit.projetintegre.entities.Medal;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.GamificationService;

import java.util.List;

@RestController
@RequestMapping("/api/gamifications")
@RequiredArgsConstructor
@Tag(name = "Gamification", description = "Gamification & Badges management")
@SecurityRequirement(name = "Bearer Authentication")
public class GamificationController {

    private final GamificationService gamificationService;
    private final DtoMapper dtoMapper;

    // --- Medal Endpoints ---

    @GetMapping("/medals")
    @Operation(summary = "Get all medals")
    public ResponseEntity<ApiResponse<List<MedalResponse>>> getAllMedals() {
        List<Medal> medals = gamificationService.getAllMedals();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toMedalResponseList(medals)));
    }

    @GetMapping("/medals/{id}")
    @Operation(summary = "Get medal by ID")
    public ResponseEntity<ApiResponse<MedalResponse>> getMedalById(@PathVariable Long id) {
        Medal medal = gamificationService.getMedalById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toMedalResponse(medal)));
    }

    @PostMapping("/medals")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new medal")
    public ResponseEntity<ApiResponse<MedalResponse>> createMedal(@Valid @RequestBody MedalRequest request) {
        Medal medal = Medal.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .type(request.getType())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Medal created successfully",
                dtoMapper.toMedalResponse(gamificationService.createMedal(medal))));
    }

    @PutMapping("/medals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update medal")
    public ResponseEntity<ApiResponse<MedalResponse>> updateMedal(
            @PathVariable Long id,
            @Valid @RequestBody MedalRequest request) {
        Medal medal = Medal.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .type(request.getType())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Medal updated successfully",
                dtoMapper.toMedalResponse(gamificationService.updateMedal(id, medal))));
    }

    @DeleteMapping("/medals/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete medal")
    public ResponseEntity<ApiResponse<Void>> deleteMedal(@PathVariable Long id) {
        gamificationService.deleteMedal(id);
        return ResponseEntity.ok(ApiResponse.success("Medal deleted successfully", null));
    }

    // --- Badge Endpoints ---

    @GetMapping("/badges")
    @Operation(summary = "Get all badges")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getAllBadges() {
        List<Badge> badges = gamificationService.getAllBadges();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toBadgeResponseList(badges)));
    }

    @GetMapping("/badges/{id}")
    @Operation(summary = "Get badge by ID")
    public ResponseEntity<ApiResponse<BadgeResponse>> getBadgeById(@PathVariable Long id) {
        Badge badge = gamificationService.getBadgeById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toBadgeResponse(badge)));
    }

    @PostMapping("/badges")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Create new badge")
    public ResponseEntity<ApiResponse<BadgeResponse>> createBadge(@Valid @RequestBody BadgeRequest request) {
        Badge badge = Badge.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Badge created successfully",
                dtoMapper.toBadgeResponse(gamificationService.createBadge(badge, request.getMedalId()))));
    }

    @PutMapping("/badges/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Update badge")
    public ResponseEntity<ApiResponse<BadgeResponse>> updateBadge(
            @PathVariable Long id,
            @Valid @RequestBody BadgeRequest request) {
        Badge badge = Badge.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Badge updated successfully",
                dtoMapper.toBadgeResponse(gamificationService.updateBadge(id, badge, request.getMedalId()))));
    }

    @DeleteMapping("/badges/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Delete badge")
    public ResponseEntity<ApiResponse<Void>> deleteBadge(@PathVariable Long id) {
        gamificationService.deleteBadge(id);
        return ResponseEntity.ok(ApiResponse.success("Badge deleted successfully", null));
    }
}
