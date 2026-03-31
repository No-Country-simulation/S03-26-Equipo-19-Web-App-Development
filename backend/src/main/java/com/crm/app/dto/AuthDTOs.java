package com.crm.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDTOs {

    @Schema(description = "Solicitud de login")
    public record LoginRequest(
            @Email @NotBlank
            @Schema(description = "Correo electrónico", example = "admin@crm.com")
            String email,

            @NotBlank
            @Schema(description = "Contraseña", example = "Admin1234!")
            String password
    ) {}

    @Schema(description = "Respuesta de login con token JWT")
    public record LoginResponse(
            @Schema(description = "Token JWT para autenticación", example = "eyJhbGciOiJIUzI1NiIs...")
            String token,

            @Schema(description = "ID del usuario", example = "1")
            Long id,

            @Schema(description = "Correo electrónico del usuario", example = "admin@crm.com")
            String email,

            @Schema(description = "Rol del usuario", example = "ADMIN")
            String role,

            @Schema(description = "Nombre del usuario", example = "Administrador")
            String name
    ) {}

    @Schema(description = "Respuesta de logout")
    public record LogoutResponse(
            @Schema(description = "Mensaje de confirmación", example = "Logout exitoso")
            String message,

            @Schema(description = "Timestamp del logout", example = "2024-01-01T00:00:00")
            String timestamp
    ) {}

    @Schema(description = "Respuesta de validación de token")
    public record TokenValidationResponse(
            @Schema(description = "Token válido", example = "true")
            boolean valid,

            @Schema(description = "Email del usuario", example = "admin@crm.com")
            String email,

            @Schema(description = "Rol del usuario", example = "ADMIN")
            String role,

            @Schema(description = "Nombre del usuario", example = "Administrador")
            String name
    ) {}

    @Schema(description = "Información de perfil de usuario")
    public record UserProfileResponse(
            @Schema(description = "ID del usuario", example = "1")
            Long id,

            @Schema(description = "Nombre del usuario", example = "Administrador")
            String name,

            @Schema(description = "Email del usuario", example = "admin@crm.com")
            String email,

            @Schema(description = "Rol del usuario", example = "ADMIN")
            String role,

            @Schema(description = "Estado activo", example = "true")
            boolean isActive,

            @Schema(description = "Fecha de creación", example = "2024-01-01T00:00:00")
            String createdAt,

            @Schema(description = "Fecha de última actualización", example = "2024-01-01T00:00:00")
            String updatedAt
    ) {}

    @Schema(description = "Credenciales de usuario precargado para el selectbox")
    public record UserCredentials(
            @Schema(description = "Correo electrónico", example = "admin@crm.com")
            String email,

            @Schema(description = "Contraseña", example = "Admin1234!")
            String password,

            @Schema(description = "Rol del usuario", example = "ADMIN")
            String role
    ) {}
}