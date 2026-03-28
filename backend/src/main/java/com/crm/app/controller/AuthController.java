package com.crm.app.controller;

import com.crm.app.dto.AuthDTOs;
import com.crm.app.security.JwtUtil;
import com.crm.app.service.impl.UserDetailsServiceImpl;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
                    description = "Datos de entrada inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas"
            )
    })
    public ResponseEntity<AuthDTOs.LoginResponse> login(
            @Valid @RequestBody AuthDTOs.LoginRequest request) {

        // Log para debug
        log.info("📥 Login request: email={}, password={}",
                request.email(), request.password() != null ? "****" : "null");

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