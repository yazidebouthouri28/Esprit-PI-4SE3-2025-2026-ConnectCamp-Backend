package tn.esprit.projetintegre.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.projetintegre.entities.Notification;
import tn.esprit.projetintegre.entities.User;
import tn.esprit.projetintegre.exception.ResourceNotFoundException;
import tn.esprit.projetintegre.repositories.NotificationRepository;
import tn.esprit.projetintegre.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Page<Notification> getNotificationsByUser(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void assertCanAccessUserNotifications(Long userId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }

        User currentUser = resolveAuthenticatedUser(authentication);
        if (!currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own notifications");
        }
    }

    public void assertCanAccessNotification(Long notificationId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }

        User currentUser = resolveAuthenticatedUser(authentication);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (notification.getUser() == null || !currentUser.getId().equals(notification.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own notifications");
        }
    }

    @Transactional
    public Notification createNotification(Long userId, String title, String message, 
                                           String type, String actionUrl) {
        return createNotification(userId, title, message, type, actionUrl, null, null);
    }

    @Transactional
    public Notification createNotification(Long userId, String title, String message,
                                           String type, String actionUrl,
                                           String referenceType, Long referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .actionUrl(actionUrl)
                .isRead(false)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNotificationIfAbsent(Long userId, String title, String message,
                                                   String type, String actionUrl,
                                                   String referenceType, Long referenceId) {
        if (referenceType != null
                && referenceId != null
                && notificationRepository.existsByUserIdAndTypeAndReferenceTypeAndReferenceId(
                        userId, type, referenceType, referenceId)) {
            return null;
        }

        return createNotification(userId, title, message, type, actionUrl, referenceType, referenceId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    private User resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found"));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority == null ? "" : authority.toUpperCase())
                .anyMatch(authority -> authority.contains("ADMIN"));
    }
}
