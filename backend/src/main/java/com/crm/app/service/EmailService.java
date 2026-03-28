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
public class EmailService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${brevo.api.url:https://api.brevo.com/v3}")
    private String apiUrl;

    @Value("${brevo.api.key:}")
    private String apiKey;

    @Value("${brevo.sender.email:}")
    private String senderEmail;

    @Value("${brevo.sender.name:CRM System}")
    private String senderName;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Envía un email simple
     */
    public String sendMessage(String toEmail, String message, String recipientName) {
        if (!isConfigured()) {
            log.warn("Brevo no está configurado. Email no enviado: para={}, mensaje={}", toEmail, message);
            // Para desarrollo, simular envío
            return "simulated-" + System.currentTimeMillis();
        }

        log.info("📧 Enviando email a: {}", toEmail);

        String url = apiUrl + "/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = buildEmailBody(toEmail, message, recipientName, null);

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
                log.info("✅ Email enviado exitosamente. providerId: {}", messageId);
                return messageId;
            } else {
                throw new ExternalServiceException(
                        "Brevo",
                        "Error al enviar email. Status: " + response.getStatusCode()
                );
            }

        } catch (RestClientException e) {
            log.error("❌ Error al enviar email: {}", e.getMessage(), e);
            throw new ExternalServiceException(
                    "Brevo",
                    "Error de comunicación con Brevo: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Envía un email con asunto personalizado
     */
    public String sendMessageWithSubject(String toEmail, String message, String recipientName, String subject) {
        if (!isConfigured()) {
            log.warn("Brevo no está configurado. Email no enviado: para={}", toEmail);
            return "simulated-" + System.currentTimeMillis();
        }

        log.info("📧 Enviando email con asunto a: {}", toEmail);

        String url = apiUrl + "/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = buildEmailBody(toEmail, message, recipientName, subject);

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
                log.info("✅ Email con asunto enviado exitosamente. providerId: {}", messageId);
                return messageId;
            } else {
                throw new ExternalServiceException(
                        "Brevo",
                        "Error al enviar email. Status: " + response.getStatusCode()
                );
            }

        } catch (RestClientException e) {
            log.error("❌ Error al enviar email: {}", e.getMessage(), e);
            throw new ExternalServiceException(
                    "Brevo",
                    "Error de comunicación con Brevo: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Envía un email usando una plantilla de Brevo
     */
    public String sendTemplateMessage(String toEmail, Long templateId, Map<String, String> params, String recipientName) {
        if (!isConfigured()) {
            log.warn("Brevo no está configurado. Email con plantilla no enviado");
            return "simulated-" + System.currentTimeMillis();
        }

        log.info("📧 Enviando email con plantilla a: {}, templateId: {}", toEmail, templateId);

        String url = apiUrl + "/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("to", List.of(Map.of("email", toEmail, "name", recipientName != null ? recipientName : "")));
        requestBody.put("templateId", templateId);

        if (params != null && !params.isEmpty()) {
            requestBody.put("params", params);
        }

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
                log.info("✅ Email con plantilla enviado exitosamente. providerId: {}", messageId);
                return messageId;
            } else {
                throw new ExternalServiceException(
                        "Brevo",
                        "Error al enviar email con plantilla. Status: " + response.getStatusCode()
                );
            }

        } catch (RestClientException e) {
            log.error("❌ Error al enviar email con plantilla: {}", e.getMessage(), e);
            throw new ExternalServiceException(
                    "Brevo",
                    "Error de comunicación con Brevo: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Construye el cuerpo del email
     */
    private Map<String, Object> buildEmailBody(String toEmail, String message, String recipientName, String subject) {
        Map<String, Object> body = new HashMap<>();

        // Remitente
        body.put("sender", Map.of("email", senderEmail, "name", senderName));

        // Destinatario
        body.put("to", List.of(Map.of("email", toEmail, "name", recipientName != null ? recipientName : "")));

        // Asunto
        String finalSubject = subject != null ? subject : "Nuevo mensaje de tu equipo de ventas";
        body.put("subject", finalSubject);

        // Contenido HTML
        String htmlContent = buildHtmlMessage(message, recipientName);
        body.put("htmlContent", htmlContent);

        // Contenido texto plano (fallback)
        body.put("textContent", message);

        return body;
    }

    /**
     * Construye el mensaje HTML con formato
     */
    private String buildHtmlMessage(String message, String recipientName) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head><meta charset='UTF-8'></head>");
        html.append("<body style='font-family: Arial, sans-serif; padding: 20px;'>");

        if (recipientName != null && !recipientName.isEmpty()) {
            html.append("<p>Hola <strong>").append(escapeHtml(recipientName)).append("</strong>,</p>");
        } else {
            html.append("<p>Hola,</p>");
        }

        html.append("<p>").append(escapeHtml(message).replace("\n", "<br>")).append("</p>");
        html.append("<br>");
        html.append("<p>Saludos cordiales,<br>");
        html.append("<strong>").append(escapeHtml(senderName)).append("</strong></p>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Escapa caracteres HTML para seguridad
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Extrae el messageId de la respuesta de Brevo
     */
    private String extractMessageId(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageId = root.get("messageId");
            if (messageId != null) {
                return messageId.asText();
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer messageId de la respuesta: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Verifica si la configuración de Brevo está completa
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty()
                && senderEmail != null && !senderEmail.isEmpty();
    }
}