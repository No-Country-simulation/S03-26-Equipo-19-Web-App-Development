package com.crm.app.controller;

import com.crm.app.dto.BrevoWebhookDTO;
import com.crm.app.service.MessageService;
import com.crm.app.service.api.WebhookProcessingService;
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
            summary = "Webhook Brevo - Email entrante",
            description = """
        Recibe emails entrantes desde Brevo cuando un cliente responde.
        
        **¿Qué hace este endpoint?**
        1. Recibe el payload de Brevo con los datos del email
        2. Busca o crea el contacto por email
        3. Busca o crea la conversación (canal EMAIL)
        4. Guarda el mensaje entrante con el `providerId` = `messageId` de Brevo
        5. Previene duplicados verificando si ya existe un mensaje con ese `messageId`
        
        **Nota:** El campo `messageId` es el identificador ÚNICO que Brevo asigna al mensaje.
        Se almacena en nuestra BD como `provider_id` para correlacionar eventos futuros.
        """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del email entrante enviados por Brevo",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Email entrante válido",
                                            description = "Ejemplo de payload que Brevo envía a este webhook",
                                            value = """
                    {
                        "from": "cliente@ejemplo.com",
                        "fromName": "Juan Pérez",
                        "subject": "Re: Consulta sobre el producto",
                        "text": "Me interesa, ¿pueden darme más información?",
                        "messageId": "brevo_msg_20240401_123456789"
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "Email sin nombre de remitente",
                                            description = "Cuando el cliente no tiene nombre configurado en su cuenta de email",
                                            value = """
                    {
                        "from": "anonimo@gmail.com",
                        "fromName": null,
                        "subject": "Consulta",
                        "text": "Hola, quisiera saber más sobre sus servicios",
                        "messageId": "brevo_msg_20240401_987654321"
                    }
                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email procesado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (email mal formado, messageId faltante)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> handleBrevoWebhook(
            @RequestBody @Valid BrevoWebhookDTO.IncomingEmail webhook
    ) {
        log.info("📧 Email entrante: from={}, subject={}, messageId={}",
                webhook.from(), webhook.subject(), webhook.messageId());
        messageService.handleBrevoInbound(webhook);
        return ResponseEntity.ok().build();
    }
}