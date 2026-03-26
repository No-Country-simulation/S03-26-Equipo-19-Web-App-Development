package com.crm.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class WhatsAppWebhookDTO {

    public record IncomingMessage(

            @NotBlank
            @Schema(example = "+5491123456789")
            String from,

            @NotBlank
            String messageBody,

            @NotBlank
            String messageId,

            @NotNull
            Long timestamp
    ) {}
}
