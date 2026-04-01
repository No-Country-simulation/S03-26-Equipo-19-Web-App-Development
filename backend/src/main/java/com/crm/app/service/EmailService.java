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
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    }

    // ==================== PUBLIC METHODS ====================

    public String sendMessage(String toEmail, String message, String recipientName) {
        return send(toEmail, message, recipientName, null);
    }

    public String sendMessageWithSubject(String toEmail, String message, String recipientName, String subject) {
        return send(toEmail, message, recipientName, subject);
    }

    public String sendTemplateMessage(String toEmail, Long templateId, Map<String, String> params, String recipientName) {
        if (!isConfigured()) return simulate(toEmail, "template");

        log.info("📧 Enviando template a: {}", toEmail);

        Map<String, Object> body = new HashMap<>();
        body.put("to", List.of(Map.of("email", toEmail, "name", safe(recipientName))));
        body.put("templateId", templateId);

        if (params != null && !params.isEmpty()) {
            body.put("params", params);
        }

        return executeRequest(body, "template");
    }

    // ==================== CORE ====================

    private String send(String toEmail, String message, String recipientName, String subject) {
        if (!isConfigured()) return simulate(toEmail, "email");

        log.info("📧 Enviando email a: {}", toEmail);

        Map<String, Object> body = buildEmailBody(toEmail, message, recipientName, subject);
        return executeRequest(body, "email");
    }

    private String executeRequest(Map<String, Object> body, String type) {
        String url = apiUrl + "/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String id = extractMessageId(response.getBody());
                log.info("✅ {} enviado. id={}", type, id);
                return id;
            }

            throw new ExternalServiceException("Brevo", "Error HTTP: " + response.getStatusCode());

        } catch (RestClientException e) {
            log.error("❌ Error Brevo", e);
            throw new ExternalServiceException("Brevo", e.getMessage(), e);
        }
    }

    private String simulate(String toEmail, String type) {
        log.warn("Brevo no configurado. {} simulado para: {}", type, toEmail);
        return "simulated-" + System.currentTimeMillis();
    }

    // ==================== BUILDERS ====================

    private Map<String, Object> buildEmailBody(String toEmail, String message, String recipientName, String subject) {
        Map<String, Object> body = new HashMap<>();

        body.put("sender", Map.of("email", senderEmail, "name", senderName));
        body.put("to", List.of(Map.of("email", toEmail, "name", safe(recipientName))));
        body.put("subject", subject != null ? subject : "Nuevo mensaje de tu equipo de ventas");
        body.put("htmlContent", buildHtml(message, recipientName));
        body.put("textContent", message);

        return body;
    }

    private String buildHtml(String message, String recipientName) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial; padding:20px;'>" +
                "<p>Hola " + (hasText(recipientName) ? "<strong>" + escape(recipientName) + "</strong>" : "") + ",</p>" +
                "<p>" + escape(message).replace("\n", "<br>") + "</p>" +
                "<br><p>Saludos,<br><strong>" + escape(senderName) + "</strong></p>" +
                "</body></html>";
    }

    // ==================== HELPERS ====================

    private String extractMessageId(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            return node.has("messageId") ? node.get("messageId").asText() : null;
        } catch (Exception e) {
            log.warn("No se pudo parsear messageId");
            return null;
        }
    }

    private String escape(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private String safe(String text) {
        return text != null ? text : "";
    }

    public boolean isConfigured() {
        return hasText(apiKey) && hasText(senderEmail);
    }
}