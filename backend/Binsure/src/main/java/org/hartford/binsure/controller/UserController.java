package org.hartford.binsure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hartford.binsure.dto.CreateUserRequest;
import org.hartford.binsure.dto.ResetPasswordRequest;
import org.hartford.binsure.dto.UserDTO;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "2. User Management", description = "Admin manages underwriters and claims officers")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    @Autowired
    private UserService userService;

    // POST /api/v1/users
    // ADMIN creates UNDERWRITER or CLAIMS_OFFICER
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user (ADMIN only)", description = "Admin creates an UNDERWRITER or CLAIMS_OFFICER. Role must NOT be CUSTOMER.")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    // GET /api/v1/users
    // ADMIN gets all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (ADMIN only)", description = "Returns all users in the system")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/v1/users/{id}
    // ADMIN gets any user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (ADMIN only)", description = "Fetch any user record by their user ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET /api/v1/users/{id}/profile
    // Any authenticated user gets their own profile by user ID
    @GetMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get own user profile", description = "Any logged-in user fetches their own profile. Pass YOUR user ID from the users table.")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET /api/v1/users/role/{role}
    // ADMIN filters users by role
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role (ADMIN only)", description = "Filter users by role: ADMIN, UNDERWRITER, CLAIMS_OFFICER, CUSTOMER")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable("role") UserRole role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // PUT /api/v1/users/{id}
    // ADMIN updates any user
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user (ADMIN only)", description = "Update a user's name, email, phone or role")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // DELETE /api/v1/users/{id}
    // ADMIN soft-deletes (deactivates) a user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user (ADMIN only)", description = "Soft-delete — sets isActive=false. User cannot login afterwards.")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully. ID: " + id));
    }

    // PUT /api/v1/users/{id}/activate
    // ADMIN re-activates a deactivated user
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate user (ADMIN only)", description = "Re-enables a previously deactivated user account.")
    public ResponseEntity<UserDTO> activateUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    // PUT /api/v1/users/{id}/reset-password
    // ADMIN resets any user's password (useful when UW/officer forgets password)
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset user password (ADMIN only)", description = "Admin sets a new password for any user. Body: { \"newPassword\": \"newpass123\" }")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully for user ID: " + id));
    }
}
