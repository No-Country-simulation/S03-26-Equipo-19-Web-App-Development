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

            @Schema(description = "Nombre del usuario", example = "Manuel Manrique")
            String name
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