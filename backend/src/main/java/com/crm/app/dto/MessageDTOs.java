package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class MessageDTOs {

    @Schema(description = "Contenido de mensaje")
    public record MessageContent(

            @NotBlank
            @Schema(example = "Hola, te contacto por...")
            String body
    ) {}

    @Schema(description = "Enviar mensaje")
    public record SendMessageRequest(

            @NotNull
            @Schema(example = "10")
            Long contactId,

            @NotNull
            Channel channel,

            @NotNull
            MessageContent content,

            @Schema(description = "Opcional")
            Long templateId
    ) {}
}