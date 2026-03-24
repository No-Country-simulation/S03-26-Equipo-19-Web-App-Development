package com.crm.app.dto;

import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class UserDTOs {

    @Schema(description = "Respuesta con datos del usuario")
    public record UserResponse(
            @Schema(description = "ID del usuario", example = "1")
            Long id,

            @Schema(description = "Nombre completo", example = "Alice Johnson")
            String name,

            @Schema(description = "Correo electrónico", example = "alice@crm.com")
            String email,

            @Schema(description = "Rol del usuario", example = "SELLER", allowableValues = {"ADMIN", "SELLER"})
            Role role,

            @Schema(description = "Estado activo/inactivo", example = "true")
            boolean active,

            @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
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

    @Schema(description = "Solicitud para crear un nuevo vendedor")
    public record CreateSalespersonRequest(
            @NotBlank(message = "Name is required")
            @Schema(description = "Nombre completo", example = "New Seller", required = true)
            String name,

            @Email(message = "Invalid email address")
            @NotBlank(message = "Email is required")
            @Schema(description = "Correo electrónico", example = "newseller@crm.com", required = true)
            String email,

            @NotBlank
            @Size(min = 8, message = "Password must be at least 8 characters")
            @Schema(
                    description = "Contraseña (mínimo 8 caracteres)",
                    example = "Sales001!",
                    required = true,
                    minLength = 8
            )
            String password
    ) {}

    @Schema(description = "Solicitud para actualizar un vendedor")
    public record UpdateSalespersonRequest(
            @Schema(description = "Nombre completo", example = "Alice Updated")
            String name,

            @Size(min = 8, message = "Password must be at least 8 characters")
            @Schema(
                    description = "Contraseña (opcional, dejar null para mantener la actual)",
                    example = "NewPass123!",
                    minLength = 8
            )
            String password
    ) {}
}