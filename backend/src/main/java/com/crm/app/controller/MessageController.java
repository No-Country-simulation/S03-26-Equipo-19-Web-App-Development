package com.crm.app.controller;

import com.crm.app.dto.MessageDTOs;
import com.crm.app.model.User;
import com.crm.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    
                    **Uso de plantillas:**
                    - Enviar `templateId` y `variables` para usar una plantilla predefinida
                    - Enviar `content` para mensaje libre
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del mensaje a enviar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Mensaje libre por Email",
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
                                            name = "Mensaje libre por WhatsApp",
                                            value = """
                                            {
                                                "contactId": 10,
                                                "channel": "WHATSAPP",
                                                "content": {
                                                    "body": "Hola, te contacto por WhatsApp para coordinar la reunión."
                                                }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Usando plantilla",
                                            value = """
                                            {
                                                "contactId": 10,
                                                "channel": "EMAIL",
                                                "templateId": 1,
                                                "variables": {
                                                    "name": "Juan",
                                                    "company": "TechCorp"
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
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error al enviar el mensaje")
    })
    @PreAuthorize("hasRole('ADMIN') or @messageService.canSendToContact(#request.contactId(), principal)")
    public ResponseEntity<MessageDTOs.MessageResponse> sendMessage(
            @RequestBody @Valid MessageDTOs.SendMessageRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.sendMessageResponse(request, currentUser.getEmail()));
    }

    @GetMapping("/conversations/{conversationId}/history")
    @Operation(
            summary = "Historial de conversación",
            description = """
                    Obtiene todos los mensajes de una conversación específica (WhatsApp o Email).
                    
                    **Permisos:**
                    - **ADMIN**: Puede ver cualquier conversación
                    - **VENDEDOR**: Solo puede ver las conversaciones que tiene asignadas
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN') or @messageService.isConversationOwner(#conversationId, principal)")
    public ResponseEntity<List<MessageDTOs.MessageResponse>> getConversationHistory(
            @Parameter(description = "ID de la conversación", example = "1", required = true)
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.getConversationHistoryResponse(conversationId, currentUser.getEmail()));
    }

    @GetMapping("/contact/{contactId}/history")
    @Operation(
            summary = "Historial completo del contacto",
            description = """
                    Obtiene TODOS los mensajes de un contacto (WhatsApp + Email) combinados y ordenados cronológicamente.
                    
                    **Permisos:**
                    - **ADMIN**: Puede ver historial de cualquier contacto
                    - **VENDEDOR**: Solo puede ver historial de sus propios contactos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#contactId, principal)")
    public ResponseEntity<List<MessageDTOs.MessageResponse>> getContactHistory(
            @Parameter(description = "ID del contacto", example = "1", required = true)
            @PathVariable Long contactId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.getContactConversationHistoryResponse(contactId, currentUser.getEmail()));
    }

    // En MessageController.java - Agrega estos endpoints

    // ==================== MARCAR MENSAJES COMO LEÍDOS ====================

    @PatchMapping("/{id}/read")
    @Operation(
            summary = "Marcar mensaje como leído",
            description = """
                    Marca un mensaje específico como leído por el vendedor.
                    
                    **Reglas:**
                    - Solo aplica para mensajes INBOUND (recibidos del cliente)
                    - El vendedor debe tener acceso a la conversación
                    - Si ya estaba leído, retorna el estado actual sin cambios
                    
                    **Permisos:**
                    - **ADMIN**: Puede marcar cualquier mensaje como leído
                    - **VENDEDOR**: Solo puede marcar mensajes de sus conversaciones asignadas
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje marcado como leído exitosamente"),
            @ApiResponse(responseCode = "400", description = "El mensaje es OUTBOUND (no aplica)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado - No tienes acceso a este mensaje"),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageDTOs.MarkAsReadResponse> markAsRead(
            @Parameter(description = "ID del mensaje", example = "42", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(messageService.markAsRead(id, currentUser.getEmail()));
    }

    @PatchMapping("/conversations/{conversationId}/read-all")
    @Operation(
            summary = "Marcar todos los mensajes de una conversación como leídos",
            description = """
                    Marca TODOS los mensajes INBOUND no leídos de una conversación como leídos.
                    
                    **Uso típico:** Cuando un vendedor abre una conversación, se llama a este endpoint.
                    
                    **Permisos:**
                    - **ADMIN**: Puede marcar cualquier conversación
                    - **VENDEDOR**: Solo puede marcar conversaciones que tiene asignadas
                    
                    **Retorna:** Cantidad de mensajes que se marcaron como leídos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensajes marcados como leídos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN') or @messageService.isConversationOwner(#conversationId, principal)")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @Parameter(description = "ID de la conversación", example = "1", required = true)
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User currentUser) {
        int updatedCount = messageService.markAllAsRead(conversationId, currentUser.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("conversationId", conversationId);
        response.put("markedCount", updatedCount);
        response.put("message", updatedCount + " mensajes marcados como leídos");

        return ResponseEntity.ok(response);
    }
}