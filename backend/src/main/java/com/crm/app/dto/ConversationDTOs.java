package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.ConversationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "DTOs para conversaciones")
public class ConversationDTOs {

    @Schema(description = "Información resumida del contacto")
    public record ContactInfo(
            Long id,
            String name,
            String lastName,
            String email,
            String phone
    ) {}

    @Schema(description = "Información resumida del usuario (vendedor)")
    public record UserInfo(
            Long id,
            String name,
            String email
    ) {}

    @Schema(description = "Respuesta de conversación (versión optimizada)")
    public record ConversationResponse(
            Long id,
            ContactInfo contact,
            Channel channel,
            ConversationStatus status,
            UserInfo assignedTo,
            LocalDateTime lastInteraction,
            LocalDateTime createdAt
    ) {}

    @Schema(description = "Item de bandeja de entrada unificada")
    public record InboxItemResponse(
            @Schema(description = "ID del contacto")
            Long contactId,

            @Schema(description = "ID de la conversación")
            Long conversationId,

            @Schema(description = "ID del último mensaje")
            Long lastMessageId,

            @Schema(description = "Canal de la conversación (WHATSAPP/EMAIL)")
            Channel channel,

            @Schema(description = "Texto del último mensaje (truncado para vista previa)")
            String lastMessagePreview,

            @Schema(description = "Email o teléfono del contacto según corresponda")
            String contactIdentifier,

            @Schema(description = "Nombre del contacto")
            String contactName,

            @Schema(description = "Fecha y hora del último mensaje")
            LocalDateTime lastMessageAt
    ) {}
}