package org.hartford.binsure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.binsure.dto.CreateUserRequest;
import org.hartford.binsure.dto.UserDTO;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockbean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc Controller tests for UserController.
 * Tests user management endpoints with role-based authorization.
 * Uses @WithMockUser to simulate authenticated requests.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO userDTO;
    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup user DTO
        userDTO = UserDTO.builder()
                .id(1L)
                .email("underwriter@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .phone("9876543210")
                .role(UserRole.UNDERWRITER)
                .isActive(true)
                .build();

        // Setup create request
        createRequest = new CreateUserRequest();
        createRequest.setEmail("underwriter@example.com");
        createRequest.setFirstName("Alice");
        createRequest.setLastName("Smith");
        createRequest.setPassword("password123");
        createRequest.setPhone("9876543210");
        createRequest.setRole(UserRole.UNDERWRITER);
    }

    @Test
    void testCreateUser_AsAdmin_Success() throws Exception {
        // Arrange
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf())
                .with(request -> {
                    request.setUserPrincipal(() -> "admin@example.com");
                    return request;
                }))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("underwriter@example.com"))
                .andExpect(jsonPath("$.role").value("UNDERWRITER"));

        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    void testCreateUser_Unauthorized() throws Exception {
        // Act & Assert - No authentication provided
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers_Success() throws Exception {
        // Arrange
        UserDTO user2 = UserDTO.builder()
                .id(2L)
                .email("officer@example.com")
                .firstName("Bob")
                .lastName("Johnson")
                .role(UserRole.CLAIMS_OFFICER)
                .build();

        List<UserDTO> userList = Arrays.asList(userDTO, user2);
        when(userService.getAllUsers()).thenReturn(userList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$").isArray());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_Success() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Alice"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_NotFound() throws Exception {
        // Arrange
        when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("User", "ID", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetUserProfile_AsCustomer() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(userDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUsersByRole_Success() throws Exception {
        // Arrange
        List<UserDTO> underwriters = Arrays.asList(userDTO);
        when(userService.getUsersByRole(UserRole.UNDERWRITER)).thenReturn(underwriters);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/role/UNDERWRITER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("UNDERWRITER"))
                .andExpect(jsonPath("$").isArray());

        verify(userService, times(1)).getUsersByRole(UserRole.UNDERWRITER);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_Success() throws Exception {
        // Arrange
        UserDTO updatedDTO = UserDTO.builder()
                .id(1L)
                .email("alice.updated@example.com")
                .firstName("Alicia")
                .lastName("Johnson")
                .role(UserRole.UNDERWRITER)
                .build();

        CreateUserRequest updateRequest = new CreateUserRequest();
        updateRequest.setEmail("alice.updated@example.com");
        updateRequest.setFirstName("Alicia");
        updateRequest.setLastName("Johnson");

        when(userService.updateUser(eq(1L), any(CreateUserRequest.class)))
                .thenReturn(updatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alicia"))
                .andExpect(jsonPath("$.email").value("alice.updated@example.com"));

        verify(userService, times(1)).updateUser(eq(1L), any(CreateUserRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateUser_Success() throws Exception {
        // Arrange
        UserDTO reactivatedDTO = UserDTO.builder()
                .id(1L)
                .email("underwriter@example.com")
                .firstName("Alice")
                .isActive(true)
                .role(UserRole.UNDERWRITER)
                .build();

        when(userService.activateUser(1L)).thenReturn(reactivatedDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).activateUser(1L);
    }
}

