package com.crm.app.controller;

import com.crm.app.security.JwtUtil;
import com.crm.app.service.impl.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record LoginResponse(
            String token,
            String email,
            String role
    ) {}

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Delegates credential verification to Spring Security
        // Throws BadCredentialsException on failure → handled by GlobalExceptionHandler → 401
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
}