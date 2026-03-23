package com.crm.app.dto;

import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * All DTOs for the User domain as Java records.
 * The User entity is never exposed directly from any controller.
 */
public class UserDTOs {

    /** What the API returns — never includes passwordHash */
    public record UserResponse(
            Long id,
            String name,
            String email,
            Role role,
            boolean active,
            LocalDateTime createdAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.isActive(),
                    user.getCreatedAt()
            );
        }
    }

    /** Payload to create a new salesperson (Admin only) */
    public record CreateSalespersonRequest(
            @NotBlank(message = "Name is required")
            String name,

            @Email(message = "Invalid email address")
            @NotBlank(message = "Email is required")
            String email,

            @NotBlank
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password
    ) {}

    /** Payload to update a salesperson's name or password */
    public record UpdateSalespersonRequest(
            String name,

            @Size(min = 8, message = "Password must be at least 8 characters")
            String password
    ) {}
}