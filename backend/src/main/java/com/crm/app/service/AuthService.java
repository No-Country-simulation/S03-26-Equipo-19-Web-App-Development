package com.crm.app.service;

import com.crm.app.dto.AuthDTOs;
import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.UnauthorizedAccessException;
import com.crm.app.model.User;
import com.crm.app.repository.UserRepository;
import com.crm.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * Autentica un usuario y genera un token JWT
     */
    public AuthDTOs.LoginResponse login(String email, String password) {
        log.info("📥 Intentando login para email: {}", email);

        try {
            // Autenticar credenciales
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            log.debug("✅ Autenticación exitosa para: {}", email);

        } catch (BadCredentialsException e) {
            log.warn("❌ Credenciales inválidas para: {}", email);
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        // Cargar detalles del usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Buscar usuario completo en BD
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email: " + email));

        // Verificar que el usuario esté activo
        if (!user.isActive()) {
            log.warn("⚠️ Intento de login con usuario inactivo: {}", email);
            throw new BusinessRuleViolationException(
                    "Usuario inactivo",
                    "Tu cuenta está deshabilitada. Contacta al administrador."
            );
        }

        // Generar token con nombre
        String token = jwtUtil.generateToken(userDetails, user.getName());

        // Extraer rol
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");

        log.info("✅ Login exitoso: email={}, role={}, id={}", email, role, user.getId());

        return new AuthDTOs.LoginResponse(
                token,
                user.getId(),
                email,
                role,
                user.getName()
        );
    }

    /**
     * Valida un token JWT y retorna información del usuario
     */
    public AuthDTOs.TokenValidationResponse validateToken(String token) {
        log.debug("🔍 Validando token JWT");

        if (token == null || token.isBlank()) {
            return new AuthDTOs.TokenValidationResponse(false, null, null, null);
        }

        try {
            String email = jwtUtil.extractEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.isValid(token, userDetails)) {
                User user = userRepository.findByEmail(email).orElse(null);
                String role = userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .orElse("UNKNOWN");

                return new AuthDTOs.TokenValidationResponse(
                        true,
                        email,
                        role,
                        user != null ? user.getName() : null
                );
            }
        } catch (Exception e) {
            log.warn("⚠️ Token inválido: {}", e.getMessage());
        }

        return new AuthDTOs.TokenValidationResponse(false, null, null, null);
    }

    /**
     * Obtiene el perfil completo de un usuario por email
     */
    public AuthDTOs.UserProfileResponse getUserProfile(String email) {
        log.debug("👤 Obteniendo perfil de usuario: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email: " + email));

        return new AuthDTOs.UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
        );
    }

    /**
     * Obtiene la lista de usuarios precargados (para desarrollo)
     */
    public List<AuthDTOs.UserCredentials> getAvailableUsers() {
        log.debug("📋 Obteniendo lista de usuarios precargados");

        return List.of(
                new AuthDTOs.UserCredentials("admin@crm.com", "Admin1234!", "ADMIN"),
                new AuthDTOs.UserCredentials("alice@crm.com", "Sales001!", "SALESPERSON"),
                new AuthDTOs.UserCredentials("bob@crm.com", "Sales002!", "SALESPERSON"),
                new AuthDTOs.UserCredentials("carol@crm.com", "Sales003!", "SALESPERSON"),
                new AuthDTOs.UserCredentials("david@crm.com", "Sales004!", "SALESPERSON"),
                new AuthDTOs.UserCredentials("emma@crm.com", "Sales005!", "SALESPERSON")
        );
    }
}