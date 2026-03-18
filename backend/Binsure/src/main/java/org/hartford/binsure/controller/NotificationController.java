package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.entity.Notification;
import org.hartford.binsure.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.hartford.binsure.security.SecurityUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "10. Notifications", description = "In-app notification management")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all notifications for user")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications(securityUtils.getCurrentUserId()));
    }

    @GetMapping("/user/unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notifications for user")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications(securityUtils.getCurrentUserId()));
    }

    @GetMapping("/user/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread notification count (badge)")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity
                .ok(Map.of("unreadCount", notificationService.getUnreadCount(securityUtils.getCurrentUserId())));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Notification> markAsRead(@PathVariable("notificationId") Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @PutMapping("/user/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        Long userId = securityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read for user ID: " + userId));
    }
}
