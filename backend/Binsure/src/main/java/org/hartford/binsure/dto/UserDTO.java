package org.hartford.binsure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hartford.binsure.enums.UserRole;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id; // users.id — use this for /users/{id}/profile and JWT-based calls
    private Long businessId; // primary business id for customers
    private String email;
    private UserRole role;
    private String firstName;
    private String lastName;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
