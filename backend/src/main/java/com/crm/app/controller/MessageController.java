package com.crm.app.controller;

import com.crm.app.dto.MessageDTOs;
import com.crm.app.model.Message;
import com.crm.app.model.User;
import com.crm.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Mensajes", description = "Gestión de mensajes y comunicación (WhatsApp y Email)")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @Operation(
            summary = "Enviar mensaje",
            description = """
            Envía un mensaje por WhatsApp o Email desde el CRM.
            
            **Permisos:**
            - **ADMIN**: Puede enviar mensajes a cualquier contacto
            - **VENDEDOR**: Solo puede enviar mensajes a sus propios contactos
            
            **Canales disponibles:**
            - `WHATSAPP`: Requiere que el contacto tenga número de teléfono
            - `EMAIL`: Requiere que el contacto tenga dirección de email
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del mensaje a enviar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Mensaje por Email",
                                            summary = "Enviar email a un contacto",
                                            value = """
                        {
                            "contactId": 10,
                            "channel": "EMAIL",
                            "content": {
                                "body": "Hola, gracias por tu interés. ¿En qué podemos ayudarte?"
                            }
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "Mensaje por WhatsApp",
                                            summary = "Enviar WhatsApp a un contacto",
                                            value = """
                        {
                            "contactId": 10,
                            "channel": "WHATSAPP",
                            "content": {
                                "body": "Hola, te contacto por WhatsApp para coordinar la reunión."
                            }
                        }
                        """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje enviado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o contacto sin información para el canal"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (vendedor intenta enviar a contacto ajeno)"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error al enviar el mensaje")
    })
    @PreAuthorize("hasRole('ADMIN') or @messageService.canSendToContact(#request.contactId(), principal)")
    public ResponseEntity<Message> sendMessage(
            @RequestBody @Valid MessageDTOs.SendMessageRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.sendMessage(request, currentUser.getEmail()));
    }

    @GetMapping("/conversations/{conversationId}/history")
    @Operation(
            summary = "Historial de conversación",
            description = """
            Obtiene todos los mensajes de una conversación (WhatsApp o Email).
            
            **Permisos:**
            - **ADMIN**: Puede ver cualquier conversación
            - **VENDEDOR**: Solo puede ver las conversaciones que tiene asignadas
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado (vendedor intenta ver conversación ajena)"),
                    @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
            }
    )
    @PreAuthorize("hasRole('ADMIN') or @messageService.isConversationOwner(#conversationId, principal)")
    public ResponseEntity<List<Message>> getConversationHistory(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.getConversationHistory(conversationId, currentUser.getEmail()));
    }
}