package org.hartford.binsure.service;

import org.hartford.binsure.entity.Notification;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.NotificationType;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.NotificationRepository;
import org.hartford.binsure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // ─── Core internal send ──────────────────────────────────────────────────────

    private void send(User recipient, User triggeredBy, NotificationType type,
                      String title, String message, Long referenceId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setTriggeredBy(triggeredBy);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReferenceId(referenceId);
        n.setRead(false);
        notificationRepository.save(n);
    }

    private void sendToAllAdmins(User triggeredBy, NotificationType type,
                                  String title, String message, Long referenceId) {
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        for (User admin : admins) {
            send(admin, triggeredBy, type, title, message, referenceId);
        }
    }

    // ─── Application Flow ────────────────────────────────────────────────────────

    public void notifyApplicationSubmitted(User customer, Long applicationId) {
        sendToAllAdmins(customer, NotificationType.APPLICATION_SUBMITTED,
                "New Policy Application Submitted",
                "Customer " + fullName(customer) + " submitted a new policy application. ID: " + applicationId,
                applicationId);
    }

    public void notifyUnderwriterAssigned(User underwriter, User admin, Long applicationId) {
        send(underwriter, admin, NotificationType.APPLICATION_ASSIGNED_TO_UNDERWRITER,
                "Policy Application Assigned to You",
                "You have been assigned to review policy application ID: " + applicationId + ". Please provide your decision.",
                applicationId);
    }

    public void notifyCustomerApplicationDecision(User customer, User underwriter,
                                                   Long applicationId, String decision) {
        NotificationType type = decision.equalsIgnoreCase("APPROVED")
                ? NotificationType.APPLICATION_APPROVED
                : NotificationType.APPLICATION_REJECTED;
        send(customer, underwriter, type,
                "Decision on Your Policy Application",
                "Your policy application (ID: " + applicationId + ") has been " + decision + " by the underwriter.",
                applicationId);
    }

    public void notifyAdminCustomerAccepted(User customer, Long applicationId) {
        sendToAllAdmins(customer, NotificationType.CUSTOMER_ACCEPTED_DECISION,
                "Customer Accepted Underwriting Decision",
                "Customer " + fullName(customer) + " ACCEPTED the underwriting decision for application ID: "
                        + applicationId + ". Please issue the policy.",
                applicationId);
    }

    public void notifyAdminCustomerRejected(User customer, Long applicationId) {
        sendToAllAdmins(customer, NotificationType.CUSTOMER_REJECTED_DECISION,
                "Customer Rejected Underwriting Decision",
                "Customer " + fullName(customer) + " REJECTED the underwriting decision for application ID: "
                        + applicationId + ".",
                applicationId);
    }

    public void notifyCustomerPolicyIssued(User customer, User admin,
                                            Long applicationId, String policyNumber) {
        send(customer, admin, NotificationType.POLICY_ISSUED,
                "Your Insurance Policy Has Been Issued",
                "Congratulations! Your policy has been issued. Policy Number: " + policyNumber
                        + ". Application ID: " + applicationId,
                applicationId);
    }

    // ─── Claim Flow ──────────────────────────────────────────────────────────────

    public void notifyClaimSubmitted(User customer, Long claimId) {
        sendToAllAdmins(customer, NotificationType.CLAIM_SUBMITTED,
                "New Insurance Claim Filed",
                "Customer " + fullName(customer) + " filed a new claim. Claim ID: " + claimId,
                claimId);
    }

    public void notifyOfficerClaimAssigned(User officer, User admin, Long claimId) {
        send(officer, admin, NotificationType.CLAIM_ASSIGNED_TO_OFFICER,
                "Claim Assigned to You",
                "You have been assigned to process claim ID: " + claimId + ". Please review and take action.",
                claimId);
    }

    public void notifyCustomerClaimInvestigation(User customer, User officer, Long claimId) {
        send(customer, officer, NotificationType.CLAIM_UNDER_INVESTIGATION,
                "Your Claim Is Under Investigation",
                "Your claim (ID: " + claimId + ") is now under investigation by our claims team.",
                claimId);
    }

    public void notifyCustomerClaimApproved(User customer, User officer,
                                             Long claimId, String approvedAmount) {
        send(customer, officer, NotificationType.CLAIM_APPROVED,
                "Your Claim Has Been Approved",
                "Your claim (ID: " + claimId + ") has been approved for ₹" + approvedAmount + ".",
                claimId);
    }

    public void notifyCustomerClaimRejected(User customer, User officer,
                                             Long claimId, String reason) {
        send(customer, officer, NotificationType.CLAIM_REJECTED,
                "Your Claim Has Been Rejected",
                "Your claim (ID: " + claimId + ") was rejected. Reason: " + reason,
                claimId);
    }

    public void notifyCustomerClaimSettled(User customer, User officer,
                                            Long claimId, String settledAmount) {
        send(customer, officer, NotificationType.CLAIM_SETTLED,
                "Your Claim Has Been Settled",
                "Your claim (ID: " + claimId + ") has been settled. Amount: ₹" + settledAmount + ".",
                claimId);
    }

    public void notifyAdminClaimAppealed(User customer, Long claimId) {
        sendToAllAdmins(customer, NotificationType.CLAIM_APPEALED,
                "Claim Appeal Filed",
                "Customer " + fullName(customer) + " appealed their rejected claim (ID: " + claimId + ").",
                claimId);
    }

    // ─── Payment Flow ────────────────────────────────────────────────────────────

    public void notifyAdminPaymentReceived(User customer, Long applicationId, String amount) {
        sendToAllAdmins(customer, NotificationType.PAYMENT_RECEIVED,
                "Premium Payment Received",
                "Customer " + fullName(customer) + " made a premium payment of ₹" + amount
                        + " for application ID: " + applicationId,
                applicationId);
    }

    // ─── Read / Fetch ─────────────────────────────────────────────────────────────

    public List<Notification> getMyNotifications(Long userId) {
        validateUser(userId);
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getMyUnreadNotifications(Long userId) {
        validateUser(userId);
        return notificationRepository.findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        validateUser(userId);
        return notificationRepository.countByRecipient_IdAndIsReadFalse(userId);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "ID", notificationId));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        validateUser(userId);
        List<Notification> unread = notificationRepository
                .findByRecipient_IdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unread);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private void validateUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}

