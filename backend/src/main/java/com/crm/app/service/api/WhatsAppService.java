package com.crm.app.service.api;

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
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class WhatsAppService {

    private final RestTemplate restTemplate;
    private final PhoneNumberNormalizer phoneNormalizer;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${whatsapp.api.url:https://graph.facebook.com/v22.0}")
    private String apiUrl;

    @Value("${whatsapp.api.phone-number-id:}")
    private String phoneId;

    @Value("${whatsapp.api.token:}")
    private String token;

    public WhatsAppService(RestTemplate restTemplate, PhoneNumberNormalizer phoneNormalizer) {
        this.restTemplate = restTemplate;
        this.phoneNormalizer = phoneNormalizer;
    }

    public String sendMessage(String to, String msg) {
        String number = phoneNormalizer.normalize(to);

        if (!isConfigured()) {
            throw new ExternalServiceException("WhatsApp", "No configurado");
        }

        try {
            log.info("📤 Enviando mensaje WhatsApp a: {}", number);
            log.debug("📤 Mensaje: {}", msg);

            ResponseEntity<String> res = restTemplate.exchange(
                    apiUrl + "/" + phoneId + "/messages",
                    HttpMethod.POST,
                    buildRequest(number, msg),
                    String.class
            );

            log.debug("📤 Respuesta completa de Meta: {}", res.getBody());

            String providerId = extractId(res.getBody());
            log.info("✅ Mensaje enviado exitosamente. providerId={}", providerId);

            return providerId;

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("🔑 Token expirado o inválido para WhatsApp");
            throw new TokenExpiredException("WhatsApp");

        } catch (HttpClientErrorException e) {
            log.error("❌ Error HTTP enviando mensaje WhatsApp: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalServiceException("WhatsApp", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error inesperado enviando mensaje WhatsApp: {}", e.getMessage(), e);
            throw new ExternalServiceException("WhatsApp", e.getMessage(), e);
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(String to, String msg) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "text",
                "text", Map.of("body", msg)
        );

        return new HttpEntity<>(body, headers);
    }

    private String extractId(String body) {
        try {
            JsonNode root = mapper.readTree(body);
            String id = root.get("messages").get(0).get("id").asText();
            log.debug("📤 ID extraído del cuerpo: {}", id);
            return id;
        } catch (Exception e) {
            log.error("❌ Error extrayendo ID de respuesta: {}", e.getMessage());
            log.error("❌ Cuerpo de respuesta: {}", body);
            return null;
        }
    }

    public boolean isConfigured() {
        return token != null && !token.isEmpty() &&
                phoneId != null && !phoneId.isEmpty();
    }
}