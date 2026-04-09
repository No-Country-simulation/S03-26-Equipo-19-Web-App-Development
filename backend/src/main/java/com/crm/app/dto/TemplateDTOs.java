package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

public class TemplateDTOs {

    @Schema(description = "Crear/Actualizar plantilla")
    public record TemplateRequest(
            @NotBlank @Schema(example = "Seguimiento post-reunión")
            String name,

            @NotNull
            Channel channel,

            @Schema(example = "Propuesta para {{company}}")
            String subject,

            @NotBlank @Schema(example = "Hola {{name}}, adjunto la propuesta...")
            String body,

            @Schema(example = "{\"name\":\"string\",\"company\":\"string\"}")
            Map<String, String> variables
    ) {}

    @Schema(description = "Usar plantilla para enviar mensaje")
    public record UseTemplateRequest(
            @NotNull
            Long templateId,

            @NotNull
            Long contactId,

            @NotNull
            Channel channel,

            Map<String, String> variableValues
    ) {}

    // ==================== RESPUESTAS OPTIMIZADAS ====================

    @Schema(description = "Información resumida del creador")
    public record CreatorInfo(
            Long id,
            String name,
            String email,
            String role
    ) {}

    @Schema(description = "Respuesta de plantilla (versión optimizada)")
    public record TemplateResponse(
            Long id,
            String name,
            Channel channel,
            String subject,
            String body,
            Map<String, String> variables,
            CreatorInfo createdBy,
            LocalDateTime createdAt
    ) {}
}