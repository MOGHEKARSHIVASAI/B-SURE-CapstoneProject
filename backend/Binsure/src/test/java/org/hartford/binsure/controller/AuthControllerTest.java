package org.hartford.binsure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.binsure.dto.AuthLoginRequest;
import org.hartford.binsure.dto.AuthResponse;
import org.hartford.binsure.dto.RegisterRequest;
import org.hartford.binsure.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc Controller tests for AuthController.
 * Tests HTTP endpoints without loading full Spring context.
 * Uses MockMvc to simulate HTTP requests and verify responses.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthLoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private AuthResponse authResponse;

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

        // Setup auth response
        authResponse = new AuthResponse();
        authResponse.setAccessToken("jwt-token-12345");
        authResponse.setTokenType("Bearer");
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        when(authService.login(any(AuthLoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-12345"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authService, times(1)).login(any(AuthLoginRequest.class));
    }

    @Test
    void testLogin_InvalidRequest() throws Exception {
        // Arrange
        AuthLoginRequest invalidRequest = new AuthLoginRequest();
        invalidRequest.setEmail(""); // Empty email
        invalidRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        AuthResponse registerResponse = new AuthResponse();
        registerResponse.setAccessToken("jwt-token-new");
        registerResponse.setTokenType("Bearer");

        when(authService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-new"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testRegister_MissingRequiredFields() throws Exception {
        // Arrange
        RegisterRequest incompleteRequest = new RegisterRequest();
        incompleteRequest.setEmail("test@example.com");
        // Missing firstName, lastName, password, etc.

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogout_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}

