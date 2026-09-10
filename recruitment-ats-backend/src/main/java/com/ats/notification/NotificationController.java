package com.ats.notification;

import com.ats.common.ApiResponse;
import com.ats.user.User;
import com.ats.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Transactional
public class NotificationController {

    private final NotificationRepository repository;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> currentUser(Authentication authentication) {
        return ApiResponse.ok("Notifications loaded",
            repository.findByUserIdOrderByCreatedAtDesc(authenticatedUser(authentication).getId())
                    .stream().map(NotificationResponse::from).toList());
    }

    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> unread(Authentication authentication) {
        return ApiResponse.ok("Unread notifications loaded",
                repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        authenticatedUser(authentication).getId()).stream()
                        .map(NotificationResponse::from).toList());
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<NotificationResponse>> byUser(
            @PathVariable Long userId,
            Authentication authentication) {
        verifyOwner(userId, authentication);
        return ApiResponse.ok("Notifications loaded",
            repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from).toList());
    }

    @RequestMapping(value = "/{id}/read", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ApiResponse<NotificationResponse> markRead(
            @PathVariable Long id,
            Authentication authentication) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        verifyOwner(n.getUser().getId(), authentication);
        n.setRead(true);
        return ApiResponse.ok("Notification marked as read", NotificationResponse.from(repository.save(n)));
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(Authentication authentication) {
        User user = authenticatedUser(authentication);
        repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId())
                .forEach(notification -> notification.setRead(true));
        repository.flush();
        return ApiResponse.ok("Notifications marked as read", null);
    }

    private User authenticatedUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private void verifyOwner(Long userId, Authentication authentication) {
        User user = authenticatedUser(authentication);
        if (!user.getId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own notifications");
        }
    }
}
