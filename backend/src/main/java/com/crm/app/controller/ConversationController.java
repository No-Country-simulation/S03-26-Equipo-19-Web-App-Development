package com.crm.app.controller;

import com.crm.app.model.Conversation;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Listar mis conversaciones",
            description = "Vendedor ve sus conversaciones asignadas. Admin ve todas las conversaciones abiertas")
    public ResponseEntity<List<Conversation>> getMyConversations(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.getMyConversations(currentUser));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las conversaciones", description = "Solo ADMIN - Lista todas las conversaciones del sistema")
    public ResponseEntity<List<Conversation>> getAllConversations() {
        return ResponseEntity.ok(conversationService.getAllConversations());
    }

    @GetMapping("/contact/{contactId}")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#contactId, principal)")
    @Operation(summary = "Conversaciones de un contacto",
            description = "Devuelve TODAS las conversaciones de un contacto (WhatsApp y Email). Útil para ver el historial completo del contacto")
    public ResponseEntity<List<Conversation>> getConversationsByContact(
            @PathVariable Long contactId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.getConversationsByContact(contactId, currentUser));
    }

    @GetMapping("/contact/{contactId}/channel/{channel}")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#contactId, principal)")
    @Operation(summary = "Conversación por contacto y canal",
            description = "Devuelve la conversación específica de un contacto para un canal (WHATSAPP o EMAIL)")
    public ResponseEntity<Conversation> getConversationByContactAndChannel(
            @PathVariable Long contactId,
            @PathVariable Channel channel,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.getOrCreateConversation(contactId, channel, currentUser));
    }

    // ==================== OBTENER UNA CONVERSACIÓN ====================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @conversationService.isOwner(#id, principal)")
    @Operation(summary = "Obtener conversación por ID")
    public ResponseEntity<Conversation> getConversation(@PathVariable Long id,
                                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.findByIdAndCheckAccess(id, currentUser));
    }

    // ==================== GESTIÓN DE ESTADO ====================

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN') or @conversationService.isOwner(#id, principal)")
    @Operation(summary = "Cerrar conversación", description = "Marca la conversación como cerrada")
    public ResponseEntity<Conversation> closeConversation(@PathVariable Long id,
                                                          @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.closeConversation(id, currentUser));
    }

    @PatchMapping("/{id}/reopen")
    @PreAuthorize("hasRole('ADMIN') or @conversationService.isOwner(#id, principal)")
    @Operation(summary = "Reabrir conversación", description = "Reabre una conversación cerrada")
    public ResponseEntity<Conversation> reopenConversation(@PathVariable Long id,
                                                           @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.reopenConversation(id, currentUser));
    }

    // ==================== REASIGNACIÓN ====================

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reasignar conversación a otro vendedor", description = "Solo ADMIN puede reasignar")
    public ResponseEntity<Conversation> reassignConversation(@PathVariable Long id,
                                                             @RequestParam Long newOwnerId,
                                                             @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(conversationService.reassignConversation(id, newOwnerId, currentUser));
    }
}