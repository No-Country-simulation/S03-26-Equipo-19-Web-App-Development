package com.crm.app.controller;

import com.crm.app.dto.MessageDTOs;
import com.crm.app.model.Message;
import com.crm.app.model.User;
import com.crm.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Mensajes", description = "Gestión de mensajes y comunicación")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @Operation(summary = "Enviar mensaje", description = "Envía un mensaje por WhatsApp o Email desde el CRM")
    public ResponseEntity<Message> sendMessage(
            @RequestBody @Valid MessageDTOs.SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails  // Solo el email
    ) {
        // El servicio se encarga de buscar el User completo
        return ResponseEntity.ok(messageService.sendMessage(request, userDetails.getUsername()));
    }

    @GetMapping("/conversations/{conversationId}/history")
    @Operation(summary = "Historial de conversación", description = "Obtiene todos los mensajes de una conversación")
    public ResponseEntity<List<Message>> getConversationHistory(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(messageService.getConversationHistory(conversationId, userDetails.getUsername()));
    }
}