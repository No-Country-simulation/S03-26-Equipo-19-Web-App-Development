package com.crm.app.controller;

import com.crm.app.dto.ConversationDTOs;
import com.crm.app.model.Conversation;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversaciones", description = "Gestión de conversaciones (WhatsApp y Email)")
public class ConversationController {

    private final ConversationService conversationService;

    // ==================== LISTAR CONVERSACIONES ====================

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar mis conversaciones",
            description = """
                    Retorna la lista de conversaciones del usuario autenticado.
                    
                    **Permisos:**
                    - **ADMIN**: Ve todas las conversaciones abiertas
                    - **VENDEDOR**: Solo ve las conversaciones que tiene asignadas
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<ConversationDTOs.ConversationResponse>> getMyConversations(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.getMyConversationsResponse(currentUser));
    }

   /* @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Listar todas las conversaciones",
            description = "Obtiene todas las conversaciones del sistema. **Solo disponible para ADMIN**."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    public ResponseEntity<List<ConversationDTOs.ConversationResponse>> getAllConversations() {
        return ResponseEntity.ok(conversationService.getAllConversationsResponse());
    }*/

    @GetMapping("/contact/{contactId}")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#contactId, principal)")
    @Operation(
            summary = "Conversaciones de un contacto",
            description = """
                    Retorna todas las conversaciones (WhatsApp y Email) de un contacto específico.
                    
                    **Permisos:**
                    - **ADMIN**: Puede ver conversaciones de cualquier contacto
                    - **VENDEDOR**: Solo puede ver conversaciones de sus propios contactos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversaciones obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - No eres el dueño del contacto"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado")
    })
    public ResponseEntity<List<ConversationDTOs.ConversationResponse>> getConversationsByContact(
            @Parameter(description = "ID del contacto", example = "1", required = true)
            @PathVariable Long contactId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.getConversationsByContactResponse(contactId, currentUser));
    }

    @GetMapping("/contact/{contactId}/channel/{channel}")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#contactId, principal)")
    @Operation(
            summary = "Conversación por contacto y canal",
            description = """
                    Retorna la conversación específica de un contacto para un canal determinado.
                    
                    **Canales disponibles:**
                    - `WHATSAPP`: Conversación de WhatsApp
                    - `EMAIL`: Conversación de email
                    
                    **Permisos:**
                    - **ADMIN**: Puede acceder a cualquier conversación
                    - **VENDEDOR**: Solo puede acceder a conversaciones de sus propios contactos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversación obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado")
    })
    public ResponseEntity<ConversationDTOs.ConversationResponse> getConversationByContactAndChannel(
            @Parameter(description = "ID del contacto", example = "1", required = true)
            @PathVariable Long contactId,
            @Parameter(description = "Canal de comunicación", example = "WHATSAPP", required = true)
            @PathVariable Channel channel,
            @AuthenticationPrincipal User currentUser) {
        Conversation conversation = conversationService.getOrCreateConversation(contactId, channel, currentUser);
        return ResponseEntity.ok(conversationService.getConversationResponse(conversation.getId(), currentUser));
    }

    // ==================== OBTENER UNA CONVERSACIÓN ====================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @conversationService.isOwner(#id, principal)")
    @Operation(
            summary = "Obtener conversación por ID",
            description = """
                    Retorna los detalles de una conversación específica.
                    
                    **Permisos:**
                    - **ADMIN**: Puede ver cualquier conversación
                    - **VENDEDOR**: Solo puede ver conversaciones que tiene asignadas
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversación encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - No eres el dueño de la conversación"),
            @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
    })
    public ResponseEntity<ConversationDTOs.ConversationResponse> getConversation(
            @Parameter(description = "ID de la conversación", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.getConversationResponse(id, currentUser));
    }

    // ==================== GESTIÓN DE ESTADO ====================

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN') or @conversationService.isOwner(#id, principal)")
    @Operation(
            summary = "Cerrar conversación",
            description = """
                    Marca una conversación como cerrada. Una vez cerrada, no se pueden enviar nuevos mensajes.
                    
                    **Permisos:**
                    - **ADMIN**: Puede cerrar cualquier conversación
                    - **VENDEDOR**: Solo puede cerrar conversaciones que tiene asignadas
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversación cerrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
    })
    public ResponseEntity<Conversation> closeConversation(
            @Parameter(description = "ID de la conversación", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.closeConversation(id, currentUser));
    }

    @PatchMapping("/{id}/reopen")
    @PreAuthorize("hasRole('ADMIN') or @conversationService.isOwner(#id, principal)")
    @Operation(
            summary = "Reabrir conversación",
            description = """
                    Reabre una conversación previamente cerrada.
                    
                    **Permisos:**
                    - **ADMIN**: Puede reabrir cualquier conversación
                    - **VENDEDOR**: Solo puede reabrir conversaciones que tiene asignadas
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversación reabierta exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
    })
    public ResponseEntity<Conversation> reopenConversation(
            @Parameter(description = "ID de la conversación", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.reopenConversation(id, currentUser));
    }

    // ==================== BANDEJA DE ENTRADA ====================

    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Bandeja de entrada unificada",
            description = """
                    Retorna el último mensaje de cada conversación del usuario autenticado,
                    ordenado cronológicamente (más reciente primero).
                    
                    **Información incluida:**
                    - Canal de la conversación (WHATSAPP/EMAIL)
                    - Preview del último mensaje (truncado)
                    - Identificador del contacto (email para EMAIL, teléfono para WHATSAPP)
                    - Nombre del contacto
                    - Fecha y hora del mensaje
                    - IDs de contacto, conversación y mensaje
                    
                    **Permisos:**
                    - **ADMIN**: Ve el último mensaje de TODAS las conversaciones del sistema
                    - **VENDEDOR**: Solo ve el último mensaje de sus propias conversaciones
                    
                    **Ordenamiento:** Por fecha del mensaje descendente (más reciente primero)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bandeja de entrada obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<List<ConversationDTOs.InboxItemResponse>> getInbox(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.getInbox(currentUser));
    }
}