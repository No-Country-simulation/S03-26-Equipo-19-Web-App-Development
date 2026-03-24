package com.crm.app.controller;

import com.crm.app.security.JwtUtil;
import com.crm.app.service.impl.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
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

            @Schema(description = "Correo electrónico del usuario", example = "admin@crm.com")
            String email,

            @Schema(description = "Rol del usuario", example = "ADMIN")
            String role
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

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con email y contraseña. Devuelve un token JWT para acceder a los endpoints protegidos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Admin Login",
                                            value = "{\"token\":\"eyJhbGciOiJIUzI1NiIs...\",\"email\":\"admin@crm.com\",\"role\":\"ADMIN\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Seller Login",
                                            value = "{\"token\":\"eyJhbGciOiJIUzI1NiIs...\",\"email\":\"alice@crm.com\",\"role\":\"SELLER\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content
            )
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtUtil.generateToken(userDetails);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        return ResponseEntity.ok(new LoginResponse(token, request.email(), role));
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
                                    value = """
                    [
                        {"email": "admin@crm.com", "password": "Admin1234!", "role": "ADMIN"},
                        {"email": "alice@crm.com", "password": "Sales001!", "role": "SELLER"},
                        {"email": "bob@crm.com", "password": "Sales002!", "role": "SELLER"},
                        {"email": "carol@crm.com", "password": "Sales003!", "role": "SELLER"},
                        {"email": "david@crm.com", "password": "Sales004!", "role": "SELLER"},
                        {"email": "emma@crm.com", "password": "Sales005!", "role": "SELLER"}
                    ]
                    """
                            )
                    )
            )
    })
    public ResponseEntity<List<UserCredentials>> getAvailableUsers() {
        List<UserCredentials> users = List.of(
                new UserCredentials("admin@crm.com", "Admin1234!", "ADMIN"),
                new UserCredentials("alice@crm.com", "Sales001!", "SELLER"),
                new UserCredentials("bob@crm.com", "Sales002!", "SELLER"),
                new UserCredentials("carol@crm.com", "Sales003!", "SELLER"),
                new UserCredentials("david@crm.com", "Sales004!", "SELLER"),
                new UserCredentials("emma@crm.com", "Sales005!", "SELLER")
        );
        return ResponseEntity.ok(users);
    }
}