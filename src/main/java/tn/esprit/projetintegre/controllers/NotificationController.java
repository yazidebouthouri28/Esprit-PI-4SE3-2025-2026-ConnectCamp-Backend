package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.dto.response.NotificationResponse;
import tn.esprit.projetintegre.entities.Notification;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;
    private final DtoMapper dtoMapper;

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get notifications by user")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotificationsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        notificationService.assertCanAccessUserNotifications(userId, authentication);
        Page<Notification> notifications = notificationService.getNotificationsByUser(userId, PageRequest.of(page, size));
        Page<NotificationResponse> response = notifications.map(dtoMapper::toNotificationResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(response)));
    }

    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @PathVariable Long userId,
            Authentication authentication) {
        notificationService.assertCanAccessUserNotifications(userId, authentication);
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(dtoMapper.toNotificationResponseList(notifications)));
    }

    @GetMapping("/user/{userId}/unread/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @PathVariable Long userId,
            Authentication authentication) {
        notificationService.assertCanAccessUserNotifications(userId, authentication);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String actionUrl) {
        Notification notification = notificationService.createNotification(userId, title, message, type, actionUrl);
        return ResponseEntity.ok(ApiResponse.success("Notification created", dtoMapper.toNotificationResponse(notification)));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        notificationService.assertCanAccessNotification(id, authentication);
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", dtoMapper.toNotificationResponse(notification)));
    }

    @PostMapping("/user/{userId}/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @PathVariable Long userId,
            Authentication authentication) {
        notificationService.assertCanAccessUserNotifications(userId, authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        notificationService.assertCanAccessNotification(id, authentication);
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }
}
