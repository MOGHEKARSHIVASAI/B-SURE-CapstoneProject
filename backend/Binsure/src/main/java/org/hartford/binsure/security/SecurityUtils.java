package org.hartford.binsure.security;

import org.hartford.binsure.entity.User;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility to extract the currently authenticated user from the JWT
 * SecurityContext.
 * Use this in controllers/services for /me and /my endpoints so clients don't
 * need to pass their own ID in the URL.
 */
@Component
public class SecurityUtils {

    @Autowired
    private UserRepository userRepository;

    /**
     * Returns the email of the currently authenticated user.
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found in security context");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        throw new RuntimeException(
                "Cannot extract user email from security context. Principal type: " + principal.getClass().getName());
    }

    /**
     * Returns the full User entity of the currently authenticated user.
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Returns the ID of the currently authenticated user.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        
        // Spring Security roles usually have 'ROLE_' prefix if using hasRole in PreAuthorize
        // But the UserRole enum in this app might not. Let's check both or match exactly what's in Authority.
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName) || a.getAuthority().equals(roleName));
    }

    public boolean isAdmin() { return hasRole("ADMIN"); }
    public boolean isUnderwriter() { return hasRole("UNDERWRITER"); }
    public boolean isOfficer() { return hasRole("CLAIMS_OFFICER"); }
    public boolean isCustomer() { return hasRole("CUSTOMER"); }
}
