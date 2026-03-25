package com.crm.app.controller;

import com.crm.app.dto.AuthDTOs;
import com.crm.app.security.JwtUtil;
import com.crm.app.service.impl.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios (Admin y Vendedores)")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con email y contraseña. Devuelve un token JWT."
    )
    @RequestBody(
            description = "Credenciales de usuario",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Admin",
                                    description = "Credenciales de administrador",
                                    value = "{\"email\": \"admin@crm.com\", \"password\": \"Admin1234!\"}"
                            ),
                            @ExampleObject(
                                    name = "Alice (Salesperson)",
                                    description = "Credenciales de vendedora Alice",
                                    value = "{\"email\": \"alice@crm.com\", \"password\": \"Sales001!\"}"
                            ),
                            @ExampleObject(
                                    name = "Bob (Salesperson)",
                                    description = "Credenciales de vendedor Bob",
                                    value = "{\"email\": \"bob@crm.com\", \"password\": \"Sales002!\"}"
                            ),
                            @ExampleObject(
                                    name = "Carol (Salesperson)",
                                    description = "Credenciales de vendedora Carol",
                                    value = "{\"email\": \"carol@crm.com\", \"password\": \"Sales003!\"}"
                            ),
                            @ExampleObject(
                                    name = "David (Salesperson)",
                                    description = "Credenciales de vendedor David",
                                    value = "{\"email\": \"david@crm.com\", \"password\": \"Sales004!\"}"
                            ),
                            @ExampleObject(
                                    name = "Emma (Salesperson)",
                                    description = "Credenciales de vendedora Emma",
                                    value = "{\"email\": \"emma@crm.com\", \"password\": \"Sales005!\"}"
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "{\"token\": \"eyJhbGciOiJIUzI1NiIs...\", \"email\": \"admin@crm.com\", \"role\": \"ADMIN\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Bad Request",
                                    value = "{\"email\": \"El email no puede estar vacío\", \"password\": \"La contraseña es obligatoria\"}"
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
                                    value = "{\"error\": \"Credenciales inválidas\", \"message\": \"Email o contraseña incorrectos\"}"
                            )
                    )
            )
    })
    public ResponseEntity<AuthDTOs.LoginResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtUtil.generateToken(userDetails);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        return ResponseEntity.ok(new AuthDTOs.LoginResponse(token, request.email(), role));
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
        List<AuthDTOs.UserCredentials> users = List.of(
                new AuthDTOs.UserCredentials("admin@crm.com", "Admin1234!", "ADMIN"),
                new AuthDTOs.UserCredentials("alice@crm.com", "Sales001!", "SALESPERSON"),
                new AuthDTOs.UserCredentials("bob@crm.com", "Sales002!", "SALESPERSON"),
                new AuthDTOs.UserCredentials("carol@crm.com", "Sales003!", "SALESPERSON"),
                new AuthDTOs.UserCredentials("david@crm.com", "Sales004!", "SALESPERSON"),
                new AuthDTOs.UserCredentials("emma@crm.com", "Sales005!", "SALESPERSON")
        );
        return ResponseEntity.ok(users);
    }
}