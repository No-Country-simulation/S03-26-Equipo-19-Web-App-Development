package com.crm.app.exception;

import com.crm.app.exception.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 4xx - Errores del Cliente ====================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("🔍 Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        log.warn("🔒 Unauthorized access: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("📀 Duplicate resource: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
        log.warn("📋 Business rule violation: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(ConversationClosedException.class)
    public ResponseEntity<ErrorResponse> handleConversationClosed(ConversationClosedException ex) {
        log.warn("💬 Conversation closed: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPhoneNumber(InvalidPhoneNumberException ex) {
        log.warn("📞 Invalid phone number: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("originalNumber", ex.getOriginalNumber());
        details.put("reason", ex.getReason());
        return build(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException ex) {
        log.error("🔑 Token expired: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("service", ex.getService());
        details.put("action", "Genera un nuevo token en Meta Developers y actualiza la variable de entorno WHATSAPP_API_TOKEN");
        return build(HttpStatus.UNAUTHORIZED, details);
    }

    @ExceptionHandler(WebhookVerificationException.class)
    public ResponseEntity<ErrorResponse> handleWebhookVerification(WebhookVerificationException ex) {
        log.error("🌐 Webhook verification failed: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("🔐 Bad credentials: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Credenciales inválidas. Verifica tu email y contraseña.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("👤 Username not found: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Credenciales inválidas. Verifica tu email y contraseña.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        log.warn("🚫 Access denied: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "valor inválido",
                        (existing, replacement) -> existing
                ));
        log.warn("✅ Validation errors: {}", errors);
        return build(HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parámetro '%s' con valor '%s' no es válido. Tipo esperado: %s",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido");
        log.warn("🔄 Type mismatch: {}", message);
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("📄 Malformed JSON: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "JSON mal formado o valor de campo inválido en la solicitud");
    }

    // ==================== 5xx - Errores del Servidor ====================

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException ex) {
        log.error("🌍 External service error - Service: {}, ProviderId: {}, Message: {}",
                ex.getService(), ex.getProviderId(), ex.getMessage(), ex);

        Map<String, String> details = new HashMap<>();
        details.put("service", ex.getService());
        if (ex.getProviderId() != null) {
            details.put("providerId", ex.getProviderId());
        }

        if ("WhatsApp Cloud API".equals(ex.getService()) && ex.getMessage().contains("131030")) {
            details.put("solution", "Agrega el número de teléfono a la lista de destinatarios de prueba en Meta Developers");
        }
        if ("WhatsApp Cloud API".equals(ex.getService()) && ex.getMessage().contains("401")) {
            details.put("solution", "El token ha expirado. Genera un nuevo token en Meta Developers y actualiza la variable WHATSAPP_API_TOKEN");
        }

        return build(HttpStatus.SERVICE_UNAVAILABLE, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("💥 Unexpected error occurred: {}", ex.getMessage(), ex);
        Map<String, String> details = new HashMap<>();
        details.put("error", ex.getClass().getSimpleName());
        details.put("message", ex.getMessage());
        details.put("suggestion", "Revisa los logs para más detalles. Si el problema persiste, contacta al administrador.");
        return build(HttpStatus.INTERNAL_SERVER_ERROR, details);
    }

    // ==================== Método Auxiliar ====================

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Object message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message instanceof String ? (String) message : null)
                .validationErrors(message instanceof Map ? (Map<String, String>) message : null)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}