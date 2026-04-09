package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.DeliveryStatus;
import com.crm.app.model.enums.MessageDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

public class MessageDTOs {

    @Schema(description = "Contenido de mensaje libre")
    public record MessageContent(
            @NotBlank @Schema(example = "Hola, te contacto por...") String body
    ) {}

    @Schema(description = "Enviar mensaje (libre o con plantilla)")
    public record SendMessageRequest(
            @NotNull @Schema(example = "10") Long contactId,
            @NotNull Channel channel,
            MessageContent content,
            @Schema(description = "ID de la plantilla (opcional)") Long templateId,
            @Schema(description = "Variables para la plantilla") Map<String, String> variables
    ) {}

    // ==================== RESPUESTAS OPTIMIZADAS ====================

    @Schema(description = "Información resumida de la conversación")
    public record ConversationInfo(
            Long id,
            Channel channel
    ) {}

    @Schema(description = "Información resumida del usuario que envió el mensaje")
    public record SenderInfo(
            Long id,
            String name,
            String email
    ) {}

    @Schema(description = "Información resumida de la plantilla usada")
    public record TemplateInfo(
            Long id,
            String name
    ) {}

    @Schema(description = "Respuesta de mensaje (versión optimizada)")
    public record MessageResponse(
            Long id,
            ConversationInfo conversation,
            MessageDirection direction,
            String body,
            DeliveryStatus deliveryStatus,
            String providerId,
            SenderInfo sender,
            TemplateInfo template,
            LocalDateTime sentAt
    ) {}
}