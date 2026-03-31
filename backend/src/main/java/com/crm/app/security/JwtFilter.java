package com.crm.app.security;

import com.crm.app.model.User;
import com.crm.app.repository.UserRepository;  // ✅ Agregar import
import com.crm.app.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug("🔵 JwtFilter - {} {}", method, path);

        if (isPublicEndpoint(path)) {
            log.debug("✅ Endpoint público, saltando validación JWT: {}", path);
            chain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("⚠️ Token no proporcionado para endpoint protegido: {}", path);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token de autenticación no proporcionado. Por favor, inicia sesión.");
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            if (jwt.isBlank()) {
                log.warn("⚠️ Token vacío para endpoint: {}", path);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Token de autenticación vacío.");
                return;
            }

            final String email = jwtUtil.extractEmail(jwt);
            log.debug("📧 Email extraído del token: {}", email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isValid(jwt, userDetails)) {
                    User user = userRepository.findByEmail(email).orElse(null);

                    if (user != null) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        user,  // ← Aquí va el objeto User
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("✅ Usuario autenticado: {} (ID: {})", email, user.getId());
                    } else {
                        log.warn("⚠️ Usuario no encontrado en BD: {}", email);
                        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                "Usuario no encontrado.");
                        return;
                    }
                } else {
                    log.warn("⚠️ Token inválido para usuario: {}", email);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Token inválido o expirado.");
                    return;
                }
            }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.error("🔑 Token expirado para path: {} - Expirado en: {}", path, e.getClaims().getExpiration());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token expirado. Por favor, inicia sesión nuevamente.");

        } catch (SignatureException e) {
            log.error("🔐 Firma de token inválida para path: {}", path);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Firma de token inválida. Posible token manipulado.");

        } catch (MalformedJwtException e) {
            log.error("📝 Token mal formado para path: {}", path);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Formato de token inválido.");

        } catch (UsernameNotFoundException e) {
            log.error("👤 Usuario no encontrado: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Usuario no encontrado. Verifica tus credenciales.");

        } catch (Exception e) {
            log.error("💥 Error inesperado en JwtFilter para path {}: {}", path, e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error interno al validar autenticación.");
        }
    }

    private boolean isPublicEndpoint(String path) {
        String[] publicPatterns = {
                "/api/v1/auth/",
                "/api/v1/webhooks/",
                "/v3/api-docs",
                "/swagger-ui/",
                "/swagger-ui.html",
                "/swagger-resources/",
                "/webjars/"
        };

        for (String pattern : publicPatterns) {
            if (path.startsWith(pattern)) {
                log.debug("✅ Path público: {}", path);
                return true;
            }
        }

        if (path.equals("/") || path.isEmpty()) {
            return true;
        }

        log.debug("🔒 Path privado: {}", path);
        return false;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\"}",
                java.time.LocalDateTime.now(),
                status,
                status == 401 ? "Unauthorized" : "Internal Server Error",
                message
        );

        response.getWriter().write(jsonResponse);
    }
}