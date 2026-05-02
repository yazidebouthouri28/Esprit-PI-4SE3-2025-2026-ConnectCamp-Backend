package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.response.BadgeResponse;
import tn.esprit.projetintegre.dto.response.MedalResponse;
import tn.esprit.projetintegre.entities.Badge;
import tn.esprit.projetintegre.entities.Medal;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.GamificationService;

import java.util.List;

/**
 * Public read-only aliases for older clients that call {@code /api/badges} and {@code /api/medals}
 * instead of {@code /api/gamifications/...}.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Legacy gamification", description = "Backward-compatible badge/medal listing paths")
public class LegacyGamificationPublicController {

    private final GamificationService gamificationService;
    private final DtoMapper dtoMapper;

    @GetMapping("/api/badges")
    @Operation(summary = "Legacy: list all badges (alias of GET /api/gamifications/badges)")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> legacyListBadges() {
        List<Badge> badges = gamificationService.getAllBadges();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toBadgeResponseList(badges)));
    }

    @GetMapping("/api/badges/{id}")
    @Operation(summary = "Legacy: get badge by id")
    public ResponseEntity<ApiResponse<BadgeResponse>> legacyGetBadge(@PathVariable Long id) {
        Badge badge = gamificationService.getBadgeById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toBadgeResponse(badge)));
    }

    @GetMapping("/api/medals")
    @Operation(summary = "Legacy: list all medals (alias of GET /api/gamifications/medals)")
    public ResponseEntity<ApiResponse<List<MedalResponse>>> legacyListMedals() {
        List<Medal> medals = gamificationService.getAllMedals();
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toMedalResponseList(medals)));
    }

    @GetMapping("/api/medals/{id}")
    @Operation(summary = "Legacy: get medal by id")
    public ResponseEntity<ApiResponse<MedalResponse>> legacyGetMedal(@PathVariable Long id) {
        Medal medal = gamificationService.getMedalById(id);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toMedalResponse(medal)));
    }
}
