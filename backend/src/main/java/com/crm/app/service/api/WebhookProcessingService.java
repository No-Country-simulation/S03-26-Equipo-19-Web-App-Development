package com.crm.app.service.api;

import com.crm.app.dto.WhatsAppWebhookDTO;
import com.crm.app.model.enums.DeliveryStatus;
import com.crm.app.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookProcessingService {

    private final MessageService messageService;

    public void processWhatsAppPayload(Map<String, Object> payload) {
        if (!(payload.get("entry") instanceof List<?> entries)) {
            log.debug("Payload inválido o sin 'entry'");
            return;
        }

        for (Object entryObj : entries) {
            if (entryObj instanceof Map<?, ?> entry) {
                processEntry(cast(entry));
            }
        }
    }

    private void processEntry(Map<String, Object> entry) {
        if (!(entry.get("changes") instanceof List<?> changes)) return;

        for (Object changeObj : changes) {
            if (changeObj instanceof Map<?, ?> change) {
                processChange(cast(change));
            }
        }
    }

    private void processChange(Map<String, Object> change) {
        if (!"messages".equals(change.get("field"))) return;
        if (!(change.get("value") instanceof Map<?, ?> value)) return;

        Map<String, Object> data = cast(value);

        if (data.containsKey("messages")) {
            processIncomingMessages(data);
        }

        if (data.containsKey("statuses")) {
            processStatuses(data);
        }
    }

    private void processIncomingMessages(Map<String, Object> data) {
        List<Map<String, Object>> messages = cast(data.get("messages"));

        for (Map<String, Object> msg : messages) {
            try {
                String from = (String) msg.get("from");
                String messageId = (String) msg.get("id");
                String text = extractText(msg);
                Long timestamp = extractTimestamp(msg);

                log.info("📩 INBOUND | from={} | id={} | text={}", from, messageId, text);

                var dto = new WhatsAppWebhookDTO.IncomingMessage(
                        from, text, messageId, timestamp
                );

                messageService.handleWhatsAppInbound(dto);

            } catch (Exception e) {
                log.error("Error procesando mensaje", e);
            }
        }
    }

    private void processStatuses(Map<String, Object> data) {
        List<Map<String, Object>> statuses = cast(data.get("statuses"));

        if (statuses == null || statuses.isEmpty()) {
            log.warn("📬 No hay statuses para procesar");
            return;
        }

        log.info("📬 Procesando {} statuses de WhatsApp", statuses.size());

        for (Map<String, Object> status : statuses) {
            try {
                String messageId = (String) status.get("id");
                String type = (String) status.get("status");
                Long timestamp = extractTimestamp(status);

                log.info("📬 STATUS RECIBIDO | providerId='{}' | status='{}' | timestamp={}",
                        messageId, type, timestamp);

                DeliveryStatus newStatus = mapStatus(type);
                log.info("📬 Mapeando status '{}' a DeliveryStatus.{}", type, newStatus);

                messageService.updateDeliveryStatus(messageId, newStatus);

            } catch (Exception e) {
                log.error("❌ Error procesando status: {}", e.getMessage(), e);
            }
        }
    }

    private String extractText(Map<String, Object> msg) {
        if (!(msg.get("text") instanceof Map<?, ?> textObj)) return null;
        return (String) ((Map<?, ?>) textObj).get("body");
    }

    private Long extractTimestamp(Map<String, Object> obj) {
        Object ts = obj.get("timestamp");

        if (ts instanceof Number n) return n.longValue();

        if (ts instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                log.warn("Timestamp inválido: {}", s);
            }
        }

        return null;
    }

    private DeliveryStatus mapStatus(String status) {
        return switch (status) {
            case "sent" -> DeliveryStatus.SENT;
            case "delivered" -> DeliveryStatus.DELIVERED;
            case "read" -> DeliveryStatus.READ;
            default -> DeliveryStatus.FAILED;
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object obj) {
        return (T) obj;
    }
}