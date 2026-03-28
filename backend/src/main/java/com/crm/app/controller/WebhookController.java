package com.crm.app.controller;

import com.crm.app.dto.BrevoWebhookDTO;
import com.crm.app.dto.WhatsAppWebhookDTO;
import com.crm.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints para integración con servicios externos")
public class WebhookController {

    private final MessageService messageService;

    /**
     * Verificación del webhook (GET) - Requerido por Meta para activar el webhook
     */
    @GetMapping("/whatsapp")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        log.info("🔵 GET /webhooks/whatsapp llamado");
        log.info("   - mode: {}", mode);
        log.info("   - token: {}", token);
        log.info("   - challenge: {}", challenge);

        String verifyToken = "crmwebhook2024";

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("✅ Webhook verificado exitosamente");
            return ResponseEntity.ok(challenge);
        }

        log.warn("❌ Verificación fallida - mode: {}, token recibido: {}, esperado: {}",
                mode, token, verifyToken);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Webhook de WhatsApp (POST) - Recibe mensajes entrantes y ecos de estado
     *
     * Meta puede enviar dos tipos de payload:
     * 1. Mensajes entrantes: contiene "messages"
     * 2. Ecos de estado: contiene "statuses"
     */
    @PostMapping("/whatsapp")
    @Operation(summary = "Webhook WhatsApp", description = "Recibe mensajes entrantes y ecos de estado de WhatsApp Cloud API")
    public ResponseEntity<Void> handleWhatsAppWebhook(
            @RequestBody Map<String, Object> payload) {

        log.info("📨 Webhook de WhatsApp recibido");
        log.debug("Payload completo: {}", payload);

        try {
            // Verificar si es un mensaje entrante o un eco
            if (payload.containsKey("entry")) {
                Object entry = payload.get("entry");
                if (entry instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> entries = (java.util.List<Map<String, Object>>) entry;

                    for (Map<String, Object> entryMap : entries) {
                        if (entryMap.containsKey("changes")) {
                            @SuppressWarnings("unchecked")
                            java.util.List<Map<String, Object>> changes =
                                    (java.util.List<Map<String, Object>>) entryMap.get("changes");

                            for (Map<String, Object> change : changes) {
                                if (change.containsKey("field")) {
                                    String field = (String) change.get("field");

                                    if ("messages".equals(field) && change.containsKey("value")) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> value = (Map<String, Object>) change.get("value");

                                        // Procesar mensajes entrantes
                                        if (value.containsKey("messages")) {
                                            processIncomingMessages(value);
                                        }

                                        // Procesar ecos de estado (message_echoes)
                                        if (value.containsKey("statuses")) {
                                            processStatusEchoes(value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("❌ Error procesando webhook de WhatsApp: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Procesa mensajes entrantes de usuarios
     */
    private void processIncomingMessages(Map<String, Object> value) {
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> messages =
                (java.util.List<Map<String, Object>>) value.get("messages");

        for (Map<String, Object> message : messages) {
            try {
                String from = (String) message.get("from");
                String messageId = (String) message.get("id");
                String text = null;

                // Extraer el texto del mensaje
                if (message.containsKey("text")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> textObj = (Map<String, Object>) message.get("text");
                    text = (String) textObj.get("body");
                }

                Long timestamp = null;
                if (message.containsKey("timestamp")) {
                    timestamp = ((Number) message.get("timestamp")).longValue();
                }

                log.info("📩 Mensaje entrante de WhatsApp: from={}, messageId={}, text={}",
                        from, messageId, text);

                // Crear DTO y enviar al servicio
                WhatsAppWebhookDTO.IncomingMessage incomingMessage =
                        new WhatsAppWebhookDTO.IncomingMessage(from, text, messageId, timestamp);

                messageService.handleWhatsAppInbound(incomingMessage);

            } catch (Exception e) {
                log.error("Error procesando mensaje entrante: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Procesa ecos de estado (confirmaciones de entrega, lectura, etc.)
     */
    private void processStatusEchoes(Map<String, Object> value) {
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> statuses =
                (java.util.List<Map<String, Object>>) value.get("statuses");

        for (Map<String, Object> status : statuses) {
            try {
                String messageId = (String) status.get("id");
                String statusType = (String) status.get("status");
                Long timestamp = ((Number) status.get("timestamp")).longValue();

                log.info("📬 Eco de estado WhatsApp: messageId={}, status={}, timestamp={}",
                        messageId, statusType, timestamp);

                // Actualizar estado del mensaje en la base de datos
                com.crm.app.model.enums.DeliveryStatus deliveryStatus;
                switch (statusType) {
                    case "sent":
                        deliveryStatus = com.crm.app.model.enums.DeliveryStatus.SENT;
                        break;
                    case "delivered":
                        deliveryStatus = com.crm.app.model.enums.DeliveryStatus.DELIVERED;
                        break;
                    case "read":
                        deliveryStatus = com.crm.app.model.enums.DeliveryStatus.READ;
                        break;
                    default:
                        deliveryStatus = com.crm.app.model.enums.DeliveryStatus.FAILED;
                }

                messageService.updateDeliveryStatus(messageId, deliveryStatus);

            } catch (Exception e) {
                log.error("Error procesando eco de estado: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Webhook Brevo (Email) - Se mantiene igual
     */
    @PostMapping("/brevo")
    @Operation(summary = "Webhook Brevo", description = "Recibe mensajes entrantes de Brevo (email)")
    public ResponseEntity<Void> handleBrevoWebhook(
            @RequestBody @Valid BrevoWebhookDTO.IncomingEmail webhook
    ) {
        log.info("Mensaje entrante de Email: from={}, subject={}", webhook.from(), webhook.subject());
        messageService.handleBrevoInbound(webhook);
        return ResponseEntity.ok().build();
    }
}