package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.Map;

public class MessageDTOs {

    @Schema(description = "Contenido de mensaje")
    public record MessageContent(

            @NotBlank
            @Schema(example = "Hola, te contacto por...")
            String body
    ) {}

    @Schema(description = "Enviar mensaje (libre o con plantilla)")
    public record SendMessageRequest(

            @NotNull @Schema(example = "10")
            Long contactId,

            @NotNull
            Channel channel,

            // Opción 1: Mensaje libre
            MessageContent content,

            // Opción 2: Usar plantilla
            @Schema(description = "ID de la plantilla (opcional)")
            Long templateId,

            @Schema(description = "Variables para la plantilla (requerido si templateId está presente)")
            Map<String, String> variables
    ) {}
}