package com.crm.app.controller;

import com.crm.app.dto.BrevoWebhookDTO;
import com.crm.app.dto.WhatsAppWebhookDTO;
import com.crm.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints para integración con servicios externos")
public class WebhookController {

    private final MessageService messageService;

    @PostMapping("/whatsapp")
    @Operation(summary = "Webhook WhatsApp", description = "Recibe mensajes entrantes de WhatsApp Cloud API")
    public ResponseEntity<Void> handleWhatsAppWebhook(
            @RequestBody @Valid WhatsAppWebhookDTO.IncomingMessage webhook
    ) {
        log.info("Mensaje entrante de WhatsApp: from={}, messageId={}", webhook.from(), webhook.messageId());
        messageService.handleWhatsAppInbound(webhook);
        return ResponseEntity.ok().build();
    }

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