package org.hartford.binsure.service;

import org.hartford.binsure.entity.Notification;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.NotificationType;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.NotificationRepository;
import org.hartford.binsure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 * Tests notification creation and delivery for various application events.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User customer;
    private User underwriter;
    private User admin;
    private Notification notification;

    @BeforeEach
    void setUp() {
        // Setup customer
        customer = new User();
        customer.setId(1L);
        customer.setEmail("customer@example.com");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setRole(UserRole.CUSTOMER);

        // Setup underwriter
        underwriter = new User();
        underwriter.setId(2L);
        underwriter.setEmail("underwriter@example.com");
        underwriter.setFirstName("Alice");
        underwriter.setLastName("Smith");
        underwriter.setRole(UserRole.UNDERWRITER);

        // Setup admin
        admin = new User();
        admin.setId(3L);
        admin.setEmail("admin@example.com");
        admin.setRole(UserRole.ADMIN);

        // Setup notification
        notification = new Notification();
        notification.setId(1L);
        notification.setRecipient(customer);
        notification.setTriggeredBy(underwriter);
        notification.setType(NotificationType.APPLICATION_APPROVED);
        notification.setTitle("Application Approved");
        notification.setMessage("Your application has been approved");
        notification.setReferenceId(1L);
        notification.setRead(false);
    }

    @Test
    void testNotifyApplicationSubmitted() {
        // Arrange
        List<User> admins = Arrays.asList(admin);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(admins);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyApplicationSubmitted(customer, 1L);

        // Assert
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyUnderwriterAssigned() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyUnderwriterAssigned(underwriter, admin, 1L);

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyCustomerApplicationDecision_Approved() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyCustomerApplicationDecision(customer, underwriter, 1L, "APPROVED");

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyCustomerApplicationDecision_Rejected() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyCustomerApplicationDecision(customer, underwriter, 1L, "REJECTED");

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyAdminCustomerAccepted() {
        // Arrange
        List<User> admins = Arrays.asList(admin);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(admins);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyAdminCustomerAccepted(customer, 1L);

        // Assert
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyCustomerPolicyIssued() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyCustomerPolicyIssued(customer, admin, 1L, "POL-123456");

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyClaimSubmitted() {
        // Arrange
        List<User> admins = Arrays.asList(admin);
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(admins);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyClaimSubmitted(customer, 1L);

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyOfficerClaimAssigned() {
        // Arrange
        User claimsOfficer = new User();
        claimsOfficer.setId(4L);
        claimsOfficer.setRole(UserRole.CLAIMS_OFFICER);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyOfficerClaimAssigned(claimsOfficer, admin, 1L);

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyCustomerClaimApproved() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyCustomerClaimApproved(customer, underwriter, 1L, "50000");

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyCustomerClaimRejected() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyCustomerClaimRejected(customer, underwriter, 1L, "Insufficient documentation");

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyCustomerClaimSettled() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.notifyCustomerClaimSettled(customer, underwriter, 1L, "45000");

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}

