package org.hartford.binsure.controller;

import org.hartford.binsure.entity.Notification;
import org.hartford.binsure.enums.NotificationType;
import org.hartford.binsure.security.SecurityUtils;
import org.hartford.binsure.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pure Mockito unit tests for NotificationController.
 * No Spring context, no MockMvc — just direct method calls with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private NotificationController controller;

    // ──────────────────────────────────────────────────────────────────────
    // HELPER — builds a Notification entity
    // ──────────────────────────────────────────────────────────────────────
    private Notification buildNotification(Long id, String title, String message, boolean isRead) {
        Notification n = new Notification();
        n.setId(id);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(NotificationType.APPLICATION_SUBMITTED);
        n.setRead(isRead);
        n.setReferenceId(100L);
        return n;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 1 — getMyNotifications : returns 200 with list
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getMyNotifications_Returns200WithNotificationList() {
        when(securityUtils.getCurrentUserId()).thenReturn(5L);
        when(notificationService.getMyNotifications(5L)).thenReturn(List.of(
                buildNotification(1L, "App Submitted", "Your application was submitted", false),
                buildNotification(2L, "App Approved", "Your application was approved", true)
        ));

        ResponseEntity<List<Notification>> response = controller.getMyNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("App Submitted", response.getBody().get(0).getTitle());
        assertEquals("App Approved", response.getBody().get(1).getTitle());

        verify(securityUtils).getCurrentUserId();
        verify(notificationService).getMyNotifications(5L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 2 — getMyNotifications : no notifications → returns empty list
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getMyNotifications_WhenNone_Returns200WithEmptyList() {
        when(securityUtils.getCurrentUserId()).thenReturn(5L);
        when(notificationService.getMyNotifications(5L)).thenReturn(List.of());

        ResponseEntity<List<Notification>> response = controller.getMyNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 3 — getUnreadNotifications : returns only unread
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getUnreadNotifications_ReturnsOnlyUnread() {
        when(securityUtils.getCurrentUserId()).thenReturn(5L);
        Notification unread = buildNotification(1L, "New Claim", "A claim was filed", false);
        when(notificationService.getMyUnreadNotifications(5L)).thenReturn(List.of(unread));

        ResponseEntity<List<Notification>> response = controller.getUnreadNotifications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().get(0).isRead());
        assertEquals("New Claim", response.getBody().get(0).getTitle());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 4 — getUnreadCount : returns count in map
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getUnreadCount_Returns200WithCount() {
        when(securityUtils.getCurrentUserId()).thenReturn(5L);
        when(notificationService.getUnreadCount(5L)).thenReturn(7L);

        ResponseEntity<Map<String, Long>> response = controller.getUnreadCount();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(7L, response.getBody().get("unreadCount"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 5 — getUnreadCount : zero unread → returns 0
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getUnreadCount_WhenZero_Returns0() {
        when(securityUtils.getCurrentUserId()).thenReturn(5L);
        when(notificationService.getUnreadCount(5L)).thenReturn(0L);

        ResponseEntity<Map<String, Long>> response = controller.getUnreadCount();

        assertEquals(0L, response.getBody().get("unreadCount"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 6 — markAsRead : returns 200 with updated notification
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void markAsRead_Returns200WithReadNotification() {
        Notification readNotification = buildNotification(1L, "Claim Settled", "Your claim was settled", true);
        when(notificationService.markAsRead(1L)).thenReturn(readNotification);

        ResponseEntity<Notification> response = controller.markAsRead(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isRead());
        assertEquals("Claim Settled", response.getBody().getTitle());

        verify(notificationService).markAsRead(1L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 7 — markAllAsRead : returns 200 with success message
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void markAllAsRead_Returns200WithMessage() {
        when(securityUtils.getCurrentUserId()).thenReturn(7L);
        doNothing().when(notificationService).markAllAsRead(7L);

        ResponseEntity<Map<String, String>> response = controller.markAllAsRead();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("All notifications marked as read for user ID: 7",
                response.getBody().get("message"));

        verify(notificationService).markAllAsRead(7L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 8 — verify securityUtils is always called for user endpoints
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void allUserEndpoints_CallSecurityUtilsForUserId() {
        when(securityUtils.getCurrentUserId()).thenReturn(10L);
        when(notificationService.getMyNotifications(10L)).thenReturn(List.of());
        when(notificationService.getMyUnreadNotifications(10L)).thenReturn(List.of());
        when(notificationService.getUnreadCount(10L)).thenReturn(0L);

        controller.getMyNotifications();
        controller.getUnreadNotifications();
        controller.getUnreadCount();

        // securityUtils.getCurrentUserId() should be called 3 times (once per method)
        verify(securityUtils, times(3)).getCurrentUserId();
    }
}

