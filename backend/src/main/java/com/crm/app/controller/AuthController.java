package com.crm.app.controller;

import com.crm.app.dto.AuthDTOs;
import com.crm.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios (Admin y Vendedores)")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con email y contraseña. Devuelve un token JWT con información del usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\"token\": \"eyJhbGciOiJIUzI1NiIs...\", \"id\": 1, \"email\": \"admin@crm.com\", \"role\": \"ADMIN\", \"name\": \"Administrador\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o usuario inactivo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Bad Request",
                                    value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Tu cuenta está deshabilitada\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Email o contraseña incorrectos\"}"
                            )
                    )
            )
    })
    public ResponseEntity<AuthDTOs.LoginResponse> login(
            @Valid @RequestBody AuthDTOs.LoginRequest request) {
            AuthDTOs.LoginResponse response = authService.login(request.email(), request.password());
            return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida la sesión actual (el cliente debe eliminar el token localmente)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\"message\": \"Logout exitoso\", \"timestamp\": \"2024-01-01T00:00:00\"}"
                            )
                    )
            )
    })
    public ResponseEntity<AuthDTOs.LogoutResponse> logout() {
        log.info("🚪 Logout request recibido");
        return ResponseEntity.ok(new AuthDTOs.LogoutResponse(
                "Logout exitoso",
                LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/validate")
    @Operation(
            summary = "Validar token JWT",
            description = "Valida un token JWT y retorna información del usuario asociado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token válido",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Valid Token",
                                    value = "{\"timestamp\": \"2026-03-30T21:04:43.8838432\", \"status\": 200, \"valid\": true, \"email\": \"admin@crm.com\", \"role\": \"ADMIN\", \"name\": \"Administrador\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o no proporcionado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Token",
                                    value = "{\"timestamp\": \"2026-03-30T21:04:43.8838432\", \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Token inválido o expirado. Por favor, inicia sesión nuevamente.\"}"
                            )
                    )
            )
    })
    public ResponseEntity<AuthDTOs.TokenValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.info("🔍 Validación de token request");

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        AuthDTOs.TokenValidationResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @Operation(
            summary = "Obtener perfil del usuario autenticado",
            description = "Retorna información detallada del usuario autenticado (requiere token JWT)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                {
                                    "id": 1,
                                    "name": "Administrador",
                                    "email": "admin@crm.com",
                                    "role": "ADMIN",
                                    "isActive": true,
                                    "createdAt": "2024-01-01T00:00:00",
                                    "updatedAt": "2024-01-01T00:00:00"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token no proporcionado, vacío, inválido o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Token no proporcionado",
                                            description = "El header Authorization está ausente o no tiene formato Bearer",
                                            value = """
                                        {
                                            "timestamp": "2026-03-30T21:04:43.8838432",
                                            "status": 401,
                                            "error": "Unauthorized",
                                            "message": "Token de autenticación no proporcionado. Por favor, inicia sesión."
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Token vacío",
                                            description = "El token está presente pero es una cadena vacía",
                                            value = """
                                        {
                                            "timestamp": "2026-03-30T21:04:43.8838432",
                                            "status": 401,
                                            "error": "Unauthorized",
                                            "message": "Token de autenticación vacío."
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "Token inválido o expirado",
                                            description = "El token tiene formato incorrecto, firma inválida o expiró",
                                            value = """
                                        {
                                            "timestamp": "2026-03-30T21:04:43.8838432",
                                            "status": 401,
                                            "error": "Unauthorized",
                                            "message": "Token inválido o expirado. Por favor, inicia sesión nuevamente."
                                        }
                                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado - El email del token no corresponde a ningún usuario en la base de datos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "User Not Found",
                                    description = "El usuario asociado al token ya no existe en el sistema",
                                    value = """
                                {
                                    "timestamp": "2026-03-30T21:04:43.8838432",
                                    "status": 404,
                                    "error": "Not Found",
                                    "message": "Usuario no encontrado"
                                }
                                """
                            )
                    )
            )
    })
    public ResponseEntity<AuthDTOs.UserProfileResponse> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.info("👤 Solicitud de perfil de usuario");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token no proporcionado");
        }

        String token = authHeader.substring(7);
        AuthDTOs.TokenValidationResponse validation = authService.validateToken(token);

        if (!validation.valid() || validation.email() == null) {
            throw new BadCredentialsException("Token inválido");
        }

        AuthDTOs.UserProfileResponse profile = authService.getUserProfile(validation.email());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/available-users")
    @Operation(
            summary = "Obtener usuarios precargados",
            description = "Devuelve la lista de usuarios disponibles para login (útil para desarrollo y testing)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                    [
                                        {"email": "admin@crm.com", "password": "Admin1234!", "role": "ADMIN"},
                                        {"email": "alice@crm.com", "password": "Sales001!", "role": "SALESPERSON"},
                                        {"email": "bob@crm.com", "password": "Sales002!", "role": "SALESPERSON"}
                                    ]
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<List<AuthDTOs.UserCredentials>> getAvailableUsers() {
        return ResponseEntity.ok(authService.getAvailableUsers());
    }
}