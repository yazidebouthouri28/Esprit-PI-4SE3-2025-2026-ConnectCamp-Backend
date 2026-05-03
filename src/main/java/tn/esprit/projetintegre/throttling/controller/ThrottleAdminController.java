package tn.esprit.projetintegre.throttling.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.throttling.dto.ThrottleStatusDTO;
import tn.esprit.projetintegre.throttling.entity.UserBanRecord;
import tn.esprit.projetintegre.throttling.service.AbuseScoreService;
import tn.esprit.projetintegre.throttling.service.AdaptiveThresholdService;
import tn.esprit.projetintegre.throttling.service.BanService;
import tn.esprit.projetintegre.throttling.service.SlidingWindowService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin controller for throttling and ban management.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/throttle")
@PreAuthorize("hasRole('ADMIN')")
public class ThrottleAdminController {
    
    @Autowired
    private SlidingWindowService slidingWindowService;
    
    @Autowired
    private AdaptiveThresholdService adaptiveThresholdService;
    
    @Autowired
    private AbuseScoreService abuseScoreService;
    
    @Autowired
    private BanService banService;
    
    /**
     * Get throttle status for a specific user.
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<ThrottleStatusDTO> getThrottleStatus(@PathVariable Long userId) {
        int windowSeconds = 60; // Default window
        int currentCount = slidingWindowService.getCurrentCount(userId, windowSeconds);
        int threshold = adaptiveThresholdService.getThreshold(userId);
        int abuseScore = abuseScoreService.getAbuseScore(userId);
        
        // Determine status
        String status;
        if (currentCount > threshold) {
            status = "BLOCKED";
        } else if (currentCount > threshold * 0.8) {
            status = "WARNING";
        } else {
            status = "NORMAL";
        }
        
        // Get ban info if applicable
        String banType = null;
        LocalDateTime banExpiresAt = null;
        String banReason = null;
        
        if (banService.isBanned(userId)) {
            banType = banService.getBanType(userId);
            // Get ban details from database
            UserBanRecord userBan = banService.getAllBannedUsers().stream()
                .filter(ban -> ban.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
            
            if (userBan != null) {
                banExpiresAt = userBan.getExpiresAt();
                banReason = userBan.getReason();
            }
        }
        
        ThrottleStatusDTO statusDTO = ThrottleStatusDTO.builder()
                .userId(userId)
                .currentCount(currentCount)
                .threshold(threshold)
                .abuseScore(abuseScore)
                .status(status)
                .banType(banType)
                .banExpiresAt(banExpiresAt)
                .banReason(banReason)
                .windowResetAt(LocalDateTime.now().plusSeconds(windowSeconds))
                .build();
        
        return ResponseEntity.ok(statusDTO);
    }
    
    /**
     * Unban a user.
     */
    @PostMapping("/unban/{userId}")
    public ResponseEntity<String> unbanUser(@PathVariable Long userId) {
        banService.unban(userId);
        log.info("Admin unbanned user {}", userId);
        
        return ResponseEntity.ok("User " + userId + " has been unbanned");
    }
    
    /**
     * Reset user's throttle settings (score and threshold).
     */
    @PostMapping("/reset/{userId}")
    public ResponseEntity<String> resetUser(@PathVariable Long userId) {
        abuseScoreService.resetAbuseScore(userId);
        adaptiveThresholdService.resetThreshold(userId);
        slidingWindowService.clearWindow(userId);
        
        log.info("Admin reset throttle settings for user {}", userId);
        
        return ResponseEntity.ok("Throttle settings reset for user " + userId);
    }
    
    /**
     * Get all currently banned users.
     */
    @GetMapping("/banned")
    public ResponseEntity<List<ThrottleStatusDTO>> getBannedUsers() {
        List<UserBanRecord> bannedUsers = banService.getAllBannedUsers();
        
        List<ThrottleStatusDTO> result = bannedUsers.stream()
                .map(ban -> {
                    int abuseScore = abuseScoreService.getAbuseScore(ban.getUserId());
                    int threshold = adaptiveThresholdService.getThreshold(ban.getUserId());
                    
                    return ThrottleStatusDTO.builder()
                            .userId(ban.getUserId())
                            .currentCount(0) // Not relevant for banned users
                            .threshold(threshold)
                            .abuseScore(abuseScore)
                            .status("BANNED")
                            .banType(ban.getType().name())
                            .banExpiresAt(ban.getExpiresAt())
                            .banReason(ban.getReason())
                            .build();
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Set custom threshold for a user
     */
  @PostMapping("/threshold/{userId}/{threshold}")
  public ResponseEntity<String> setThreshold(@PathVariable Long userId, @PathVariable int threshold) {
    int tempThreshold = threshold;
    adaptiveThresholdService.setThreshold(userId, tempThreshold);
    return ResponseEntity.ok("Threshold set to " + tempThreshold + " for user " + userId);
  }

  /**
   * Reduce abuse score for a user
   */
  @PostMapping("/reduce-score/{userId}/{reduction}")
  public ResponseEntity<String> reduceAbuseScore(@PathVariable Long userId, @PathVariable int reduction) {
    int tempReduction = reduction;
    int newScore = abuseScoreService.reduceAbuseScore(userId, tempReduction);
    return ResponseEntity.ok("Abuse score reduced to " + newScore + " for user " + userId);
  }
    
    /**
     * Get system statistics (admin only).
     */
    @GetMapping("/stats")
    public ResponseEntity<String> getSystemStats() {
        List<UserBanRecord> bannedUsers = banService.getAllBannedUsers();
        long softBans = bannedUsers.stream()
                .filter(ban -> ban.getType() == UserBanRecord.BanType.SOFT)
                .count();
        long hardBans = bannedUsers.stream()
                .filter(ban -> ban.getType() == UserBanRecord.BanType.HARD)
                .count();
        
        String stats = String.format(
                "System Stats:\n" +
                "- Total Banned Users: %d\n" +
                "- Soft Bans: %d\n" +
                "- Hard Bans: %d\n" +
                "- Default Threshold: %d\n" +
                "- Window Size: %d seconds",
                bannedUsers.size(), softBans, hardBans,
                adaptiveThresholdService.getThreshold(0L), // Default threshold
                60 // Window seconds
        );
        
        return ResponseEntity.ok(stats);
    }
}
