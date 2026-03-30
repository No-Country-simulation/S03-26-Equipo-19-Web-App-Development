package com.crm.app.service;

import com.crm.app.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WhatsAppService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${whatsapp.api.url:https://graph.facebook.com/v22.0}")
    private String apiUrl;

    @Value("${whatsapp.api.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.api.token:}")
    private String apiToken;

    public WhatsAppService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Normaliza números de teléfono para el formato que espera WhatsApp.
     * Dado que la BD ya almacena el formato correcto, esta función es solo por seguridad.
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null) return null;
        // Eliminar todo excepto dígitos
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        log.debug("Número normalizado (solo dígitos): {} -> {}", phone, digitsOnly);
        return digitsOnly;
    }

    /**
     * Envía un mensaje de texto por WhatsApp
     */
    public String sendMessage(String toPhoneNumber, String message) {
        // Normalizar el número antes de cualquier procesamiento
        String normalizedNumber = normalizePhoneNumber(toPhoneNumber);

        log.info("📱 Enviando mensaje WhatsApp - Original: {}, Normalizado: {}", toPhoneNumber, normalizedNumber);

        if (!isConfigured()) {
            log.warn("⚠️ WhatsApp no está configurado. Mensaje no enviado: {}", message);
            return "simulated-" + System.currentTimeMillis();
        }

        String url = apiUrl + "/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = buildTextMessageBody(normalizedNumber, message);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("📤 Request URL: {}", url);
            log.debug("📤 Request Body: {}", requestBody);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String messageId = extractMessageId(response.getBody());
                log.info("✅ Mensaje WhatsApp enviado exitosamente a: {}, providerId: {}", normalizedNumber, messageId);
                return messageId;
            } else {
                log.error("❌ Error HTTP {} al enviar mensaje a: {}", response.getStatusCode(), normalizedNumber);
                throw new ExternalServiceException(
                        "WhatsApp Cloud API",
                        "Error al enviar mensaje. Status: " + response.getStatusCode()
                );
            }

        } catch (RestClientException e) {
            log.error("❌ Error de comunicación al enviar mensaje WhatsApp a {}: {}", normalizedNumber, e.getMessage(), e);

            // Extraer información adicional del error si es posible
            String errorDetails = e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("131030")) {
                log.error("🔍 Error 131030: El número {} no está en la lista de destinatarios permitidos. Verifica en Meta Developers -> API Setup -> Agregar número de destinatario", normalizedNumber);
            }

            throw new ExternalServiceException(
                    "WhatsApp Cloud API",
                    "Error de comunicación con WhatsApp: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Construye el cuerpo de la solicitud para mensaje de texto
     */
    private Map<String, Object> buildTextMessageBody(String toPhoneNumber, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", toPhoneNumber);
        body.put("type", "text");

        Map<String, String> text = new HashMap<>();
        text.put("preview_url", "false");
        text.put("body", message);
        body.put("text", text);

        return body;
    }

    /**
     * Envía un mensaje de texto con plantilla (mensaje predefinido)
     */
    public String sendTemplateMessage(String toPhoneNumber, String templateName, Map<String, String> variables) {
        String normalizedNumber = normalizePhoneNumber(toPhoneNumber);

        if (!isConfigured()) {
            log.warn("⚠️ WhatsApp no está configurado. Plantilla no enviada: {}", templateName);
            return "simulated-" + System.currentTimeMillis();
        }

        log.info("📱 Enviando plantilla WhatsApp a: {}, template: {}", normalizedNumber, templateName);

        String url = apiUrl + "/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = buildTemplateMessageBody(normalizedNumber, templateName, variables);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String messageId = extractMessageId(response.getBody());
                log.info("✅ Plantilla WhatsApp enviada exitosamente a: {}, providerId: {}", normalizedNumber, messageId);
                return messageId;
            } else {
                throw new ExternalServiceException(
                        "WhatsApp Cloud API",
                        "Error al enviar plantilla. Status: " + response.getStatusCode()
                );
            }

        } catch (RestClientException e) {
            log.error("❌ Error al enviar plantilla WhatsApp: {}", e.getMessage(), e);
            throw new ExternalServiceException(
                    "WhatsApp Cloud API",
                    "Error de comunicación con WhatsApp: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Construye el cuerpo de la solicitud para mensaje con plantilla
     */
    private Map<String, Object> buildTemplateMessageBody(String toPhoneNumber, String templateName,
                                                         Map<String, String> variables) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", toPhoneNumber);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", "es"));

        if (variables != null && !variables.isEmpty()) {
            List<Map<String, Object>> components = List.of(
                    Map.of(
                            "type", "body",
                            "parameters", variables.entrySet().stream()
                                    .map(entry -> Map.of("type", "text", "text", entry.getValue()))
                                    .toList()
                    )
            );
            template.put("components", components);
        }

        body.put("template", template);

        return body;
    }

    /**
     * Marca un mensaje como leído (para enviar notificación de lectura)
     */
    public void markAsRead(String messageId) {
        if (!isConfigured()) {
            log.debug("WhatsApp no configurado, no se marca como leído");
            return;
        }

        log.info("📖 Marcando mensaje WhatsApp como leído: {}", messageId);

        String url = apiUrl + "/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("status", "read");
        body.put("message_id", messageId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("⚠️ No se pudo marcar como leído el mensaje: {}", messageId);
            }
        } catch (RestClientException e) {
            log.warn("⚠️ Error al marcar mensaje como leído: {}", e.getMessage());
        }
    }

    /**
     * Extrae el messageId de la respuesta de WhatsApp
     */
    private String extractMessageId(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messages = root.get("messages");
            if (messages != null && messages.isArray() && !messages.isEmpty()) {
                return messages.get(0).get("id").asText();
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer messageId de la respuesta: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Verifica si el token de WhatsApp está configurado
     */
    public boolean isConfigured() {
        return apiToken != null && !apiToken.isEmpty()
                && phoneNumberId != null && !phoneNumberId.isEmpty();
    }
}