package com.crm.app.service;

import com.crm.app.exception.ExternalServiceException;
import com.crm.app.exception.TokenExpiredException;
import com.crm.app.util.PhoneNumberNormalizer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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
    private final PhoneNumberNormalizer phoneNormalizer;

    @Value("${whatsapp.api.url:https://graph.facebook.com/v22.0}")
    private String apiUrl;

    @Value("${whatsapp.api.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.api.token:}")
    private String apiToken;

    public WhatsAppService(RestTemplate restTemplate, PhoneNumberNormalizer phoneNormalizer) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.phoneNormalizer = phoneNormalizer;
    }

    /**
     * Envía un mensaje de texto por WhatsApp
     */
    public String sendMessage(String toPhoneNumber, String message) {
        // Normalizar el número antes de cualquier procesamiento
        String normalizedNumber = phoneNormalizer.normalize(toPhoneNumber);
        log.info("📱 Enviando mensaje WhatsApp - Original: {}, Normalizado: {}", toPhoneNumber, normalizedNumber);

        if (!isConfigured()) {
            log.error("❌ WhatsApp no está configurado. Verifica WHATSAPP_API_TOKEN y WHATSAPP_PHONE_NUMBER_ID");
            throw new ExternalServiceException("WhatsApp Cloud API",
                    "Servicio no configurado. Verifica las variables de entorno en Render");
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

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("🔑 Token expirado o inválido. Código: {}, Mensaje: {}", e.getStatusCode(), e.getMessage());
            throw new TokenExpiredException("WhatsApp Cloud API");

        } catch (HttpClientErrorException.BadRequest e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("❌ Error 400 - Bad Request: {}", responseBody);

            if (responseBody.contains("131030")) {
                throw new ExternalServiceException("WhatsApp Cloud API", null,
                        "El número " + normalizedNumber + " no está en la lista de destinatarios permitidos. Agrega el número en Meta Developers -> API Setup -> Números de destinatarios de prueba");
            }
            throw new ExternalServiceException("WhatsApp Cloud API", e.getMessage(), e);

        } catch (HttpClientErrorException e) {
            log.error("❌ Error HTTP {}: {}", e.getStatusCode(), e.getMessage());
            throw new ExternalServiceException("WhatsApp Cloud API", e.getMessage(), e);

        } catch (RestClientException e) {
            log.error("❌ Error de comunicación al enviar mensaje WhatsApp a {}: {}", normalizedNumber, e.getMessage(), e);
            throw new ExternalServiceException("WhatsApp Cloud API", "Error de comunicación con WhatsApp: " + e.getMessage(), e);
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
        String normalizedNumber = phoneNormalizer.normalize(toPhoneNumber);

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

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("🔑 Token expirado o inválido");
            throw new TokenExpiredException("WhatsApp Cloud API");

        } catch (RestClientException e) {
            log.error("❌ Error al enviar plantilla WhatsApp: {}", e.getMessage(), e);
            throw new ExternalServiceException("WhatsApp Cloud API", e.getMessage(), e);
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
                            "parameters", variables.values().stream()
                                    .map(s -> Map.of("type", "text", "text", s))
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