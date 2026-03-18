package org.hartford.binsure.service;

import org.hartford.binsure.dto.AuthLoginRequest;
import org.hartford.binsure.dto.AuthResponse;
import org.hartford.binsure.dto.RegisterRequest;
import org.hartford.binsure.entity.Business;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.DuplicateResourceException;
import org.hartford.binsure.exception.InvalidCredentialsException;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.repository.BusinessRepository;
import org.hartford.binsure.repository.UserRepository;
import org.hartford.binsure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests JWT authentication, user registration, and login flows.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private AuthLoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        // Setup login request
        loginRequest = new AuthLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("9876543210");
        registerRequest.setCompanyName("Test Company");
        registerRequest.setCompanyRegNumber("REG123");
        registerRequest.setIndustryType("Technology");
        registerRequest.setAnnualRevenue("1000000");
        registerRequest.setNumEmployees(50);
        registerRequest.setAddressLine1("123 Street");
        registerRequest.setCity("Mumbai");
        registerRequest.setState("Maharashtra");
        registerRequest.setPostalCode("400001");
        registerRequest.setCountry("India");
        registerRequest.setTaxId("TAX123");

        // Setup user
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPasswordHash("hashedPassword");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(jwtTokenProvider.generateToken("test@example.com"))
                .thenReturn("jwt-token-12345");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-12345", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, times(1)).generateToken("test@example.com");
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setEmail("newuser@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setPasswordHash("hashedPassword123");
        savedUser.setRole(UserRole.CUSTOMER);
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(businessRepository.save(any(Business.class))).thenReturn(new Business());
        when(jwtTokenProvider.generateToken("newuser@example.com")).thenReturn("jwt-token-new");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-new", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(businessRepository, times(1)).save(any(Business.class));
    }

    @Test
    void testRegister_DuplicateEmail() {
        // Arrange
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
    }

    @Test
    void testRegister_MissingCompanyName() {
        // Arrange
        registerRequest.setCompanyName(null);
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> authService.register(registerRequest));
    }

    @Test
    void testRegister_EmptyCompanyName() {
        // Arrange
        registerRequest.setCompanyName("  ");
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidOperationException.class, () -> authService.register(registerRequest));
    }
}

