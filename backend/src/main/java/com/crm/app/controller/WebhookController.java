package com.crm.app.controller;

import com.crm.app.dto.BrevoWebhookDTO;
import com.crm.app.service.MessageService;
import com.crm.app.service.WebhookProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Webhooks", description = "Endpoints para integración con WhatsApp y Brevo")
public class WebhookController {

    private final MessageService messageService;
    private final WebhookProcessingService webhookProcessingService;

    // ==================== WHATSAPP ====================

    @GetMapping("/whatsapp")
    @Operation(summary = "Verificar webhook", description = "Meta verifica la propiedad del webhook")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificación exitosa"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        String verifyToken = "crmwebhook2024";

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/whatsapp")
    @Operation(
            summary = "Webhook WhatsApp",
            description = "Recibe mensajes y ecos de estado de WhatsApp Cloud API",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Mensaje entrante de cliente",
                                            value = """
                    {
                      "entry": [{
                        "changes": [{
                          "field": "messages",
                          "value": {
                            "messages": [{
                              "from": "5491122540454",
                              "id": "wamid.HBgNNTQ5MTEyMjU0MDQ1NA",
                              "text": { "body": "Hola, quiero información" },
                              "timestamp": 1743091200
                            }]
                          }
                        }]
                      }]
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Eco de estado (mensaje entregado)",
                                            value = """
                    {
                      "entry": [{
                        "changes": [{
                          "field": "messages",
                          "value": {
                            "statuses": [{
                              "id": "wamid.HBgNNTQ5MTEyMjU0MDQ1NA",
                              "status": "delivered",
                              "timestamp": 1743091200
                            }]
                          }
                        }]
                      }]
                    }
                    """
                                    )
                            }
                    )
            )
    )
    public ResponseEntity<Void> handleWhatsAppWebhook(@RequestBody Map<String, Object> payload) {
        try {
            webhookProcessingService.processWhatsAppPayload(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== BREVO ====================

    @PostMapping("/brevo")
    @Operation(
            summary = "Webhook Brevo",
            description = "Recibe emails entrantes de Brevo",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(value = """
                {
                    "from": "cliente@ejemplo.com",
                    "fromName": "Juan Pérez",
                    "subject": "Re: Consulta",
                    "text": "Me interesa",
                    "messageId": "msg_123"
                }""")
                    })
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensaje procesado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<Void> handleBrevoWebhook(
            @RequestBody @Valid BrevoWebhookDTO.IncomingEmail webhook
    ) {
        log.info("Email entrante: from={}, subject={}", webhook.from(), webhook.subject());
        messageService.handleBrevoInbound(webhook);
        return ResponseEntity.ok().build();
    }
}