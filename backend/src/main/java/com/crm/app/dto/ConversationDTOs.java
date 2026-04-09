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
}