package org.hartford.binsure.service;

import org.hartford.binsure.dto.CreateUserRequest;
import org.hartford.binsure.dto.UserDTO;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.DuplicateResourceException;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pure Mockito unit tests for UserService.
 * No Spring context loaded — fast, isolated, tests only business logic.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ──────────────────────────────────────────────────────────────────────
    // HELPER — builds a User entity
    // ──────────────────────────────────────────────────────────────────────
    private User buildUser(Long id, String email, UserRole role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordHash("$2a$10$hashedValue");
        user.setRole(role);
        user.setActive(active);
        user.setBusinesses(new ArrayList<>());
        return user;
    }

    private CreateUserRequest buildRequest(String email, UserRole role) {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail(email);
        req.setPassword("password123");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPhone("9876543210");
        req.setRole(role);
        return req;
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 1 — createUser : UNDERWRITER → success
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void createUser_AsUnderwriter_Success() {
        CreateUserRequest request = buildRequest("uw@binsure.com", UserRole.UNDERWRITER);

        when(userRepository.findByEmail("uw@binsure.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(5L);
            u.setBusinesses(new ArrayList<>());
            return u;
        });

        UserDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("uw@binsure.com", result.getEmail());
        assertEquals(UserRole.UNDERWRITER, result.getRole());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 2 — createUser : duplicate email → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void createUser_DuplicateEmail_ThrowsDuplicateResource() {
        CreateUserRequest request = buildRequest("taken@binsure.com", UserRole.UNDERWRITER);

        when(userRepository.findByEmail("taken@binsure.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(request));

        verify(userRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 3 — createUser : CUSTOMER role → Admin not allowed → exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void createUser_CustomerRole_ThrowsInvalidOperation() {
        CreateUserRequest request = buildRequest("cust@binsure.com", UserRole.CUSTOMER);

        when(userRepository.findByEmail("cust@binsure.com")).thenReturn(Optional.empty());

        InvalidOperationException ex = assertThrows(
                InvalidOperationException.class,
                () -> userService.createUser(request)
        );
        assertTrue(ex.getMessage().contains("CUSTOMER"));
        verify(userRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 4 — getUserById : user exists → returns DTO
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getUserById_WhenExists_ReturnsUserDTO() {
        User user = buildUser(1L, "admin@binsure.com", UserRole.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin@binsure.com", result.getEmail());
        assertEquals(UserRole.ADMIN, result.getRole());
        assertTrue(result.isActive());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 5 — getUserById : not found → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getUserById_WhenNotFound_ThrowsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(99L));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 6 — getAllUsers : returns list of DTOs
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getAllUsers_ReturnsAllUsersAsDTOs() {
        List<User> users = List.of(
                buildUser(1L, "a@b.com", UserRole.ADMIN, true),
                buildUser(2L, "c@d.com", UserRole.CUSTOMER, true)
        );
        when(userRepository.findAll()).thenReturn(users);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("a@b.com", result.get(0).getEmail());
        assertEquals("c@d.com", result.get(1).getEmail());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 7 — getUsersByRole : filters correctly
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void getUsersByRole_ReturnsMatchingUsers() {
        List<User> underwriters = List.of(
                buildUser(5L, "uw1@b.com", UserRole.UNDERWRITER, true),
                buildUser(6L, "uw2@b.com", UserRole.UNDERWRITER, true)
        );
        when(userRepository.findByRole(UserRole.UNDERWRITER)).thenReturn(underwriters);

        List<UserDTO> result = userService.getUsersByRole(UserRole.UNDERWRITER);

        assertEquals(2, result.size());
        result.forEach(u -> assertEquals(UserRole.UNDERWRITER, u.getRole()));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 8 — deleteUser : active user → deactivates
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void deleteUser_WhenActive_SetsInactive() {
        User user = buildUser(1L, "del@b.com", UserRole.CLAIMS_OFFICER, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.deleteUser(1L);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 9 — deleteUser : already deactivated → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void deleteUser_WhenAlreadyInactive_ThrowsInvalidOperation() {
        User user = buildUser(1L, "del@b.com", UserRole.CLAIMS_OFFICER, false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class,
                () -> userService.deleteUser(1L));
        verify(userRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 10 — activateUser : inactive → activates
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void activateUser_WhenInactive_SetsActive() {
        User user = buildUser(1L, "act@b.com", UserRole.UNDERWRITER, false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserDTO result = userService.activateUser(1L);

        assertTrue(result.isActive());
        verify(userRepository).save(user);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 11 — activateUser : already active → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void activateUser_WhenAlreadyActive_ThrowsInvalidOperation() {
        User user = buildUser(1L, "act@b.com", UserRole.UNDERWRITER, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class,
                () -> userService.activateUser(1L));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 12 — resetPassword : short password → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void resetPassword_WhenTooShort_ThrowsInvalidOperation() {
        assertThrows(InvalidOperationException.class,
                () -> userService.resetPassword(1L, "abc"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 13 — resetPassword : null password → throws exception
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void resetPassword_WhenNull_ThrowsInvalidOperation() {
        assertThrows(InvalidOperationException.class,
                () -> userService.resetPassword(1L, null));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 14 — resetPassword : valid password → updates hash
    // ══════════════════════════════════════════════════════════════════════
    @Test
    void resetPassword_WhenValid_EncodesAndSaves() {
        User user = buildUser(1L, "user@b.com", UserRole.ADMIN, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$newHash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserDTO result = userService.resetPassword(1L, "newPass123");

        assertNotNull(result);
        assertEquals("$2a$10$newHash", user.getPasswordHash());
        verify(passwordEncoder).encode("newPass123");
        verify(userRepository).save(user);
    }
}

