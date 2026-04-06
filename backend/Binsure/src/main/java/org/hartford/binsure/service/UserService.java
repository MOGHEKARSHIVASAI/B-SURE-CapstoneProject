package org.hartford.binsure.service;

import org.hartford.binsure.dto.CreateUserRequest;
import org.hartford.binsure.dto.UserDTO;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.enums.UserRole;
import org.hartford.binsure.exception.DuplicateResourceException;
import org.hartford.binsure.exception.InvalidOperationException;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (request.getRole() == UserRole.CUSTOMER) {
            throw new InvalidOperationException(
                    "Admin cannot create CUSTOMER accounts. Use POST /api/v1/auth/register instead.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setActive(true);

        return mapToDTO(userRepository.save(user));
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        return mapToDTO(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id)));
    }

    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        if (request.getRole() != null)
            user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return mapToDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        if (!user.isActive()) {
            throw new InvalidOperationException("User with ID " + id + " is already deactivated.");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    public UserDTO activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        if (user.isActive()) {
            throw new InvalidOperationException("User with ID " + id + " is already active.");
        }
        user.setActive(true);
        return mapToDTO(userRepository.save(user));
    }

    public UserDTO resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new InvalidOperationException("New password must be at least 6 characters.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return mapToDTO(userRepository.save(user));
    }

    private UserDTO mapToDTO(User user) {
        Long businessId = (user.getBusinesses() != null && !user.getBusinesses().isEmpty())
                ? user.getBusinesses().get(0).getId()
                : null;
        return UserDTO.builder()
                .id(user.getId())
                .businessId(businessId)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
