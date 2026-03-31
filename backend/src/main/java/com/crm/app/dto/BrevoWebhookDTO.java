package com.crm.app.dto;

import jakarta.validation.constraints.*;

public class BrevoWebhookDTO {

    public record IncomingEmail(

            @Email @NotBlank
            String from,

            String fromName,

            @NotBlank
            String subject,

            @NotBlank
            String text,

            @NotBlank
            String messageId
    ) {}
}