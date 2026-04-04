package com.crm.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class BrevoWebhookDTO {

    /**
     * DTO para mensajes entrantes de Brevo.
     *
     * Brevo envía este payload cuando recibe un email para tu dominio.
     * El campo messageId es el identificador ÚNICO que Brevo asigna al mensaje.
     * Este ID se usará para:
     * - Evitar procesar el mismo mensaje dos veces (duplicados)
     * - Correlacionar eventos futuros (entregas, rebotes, etc.)
     *
     * @param from Email del remitente (cliente)
     * @param fromName Nombre del remitente (si está disponible)
     * @param subject Asunto del email
     * @param text Contenido del email (puede ser texto plano o HTML)
     * @param messageId ID ÚNICO del mensaje en Brevo (obligatorio)
     */
    public record IncomingEmail(

            @Email @NotBlank
            @Schema(description = "Email del remitente", example = "cliente@ejemplo.com")
            String from,

            @Schema(description = "Nombre del remitente", example = "Juan Pérez")
            String fromName,

            @NotBlank
            @Schema(description = "Asunto del email", example = "Re: Consulta sobre el producto")
            String subject,

            @NotBlank
            @Schema(description = "Contenido del email (texto plano o HTML)", example = "Me interesa, ¿pueden darme más información?")
            String text,

            @NotBlank
            @Schema(description = "ID único del mensaje en Brevo. Se guardará como providerId en la BD.",
                    example = "msg_brevo_123456789")
            String messageId
    ) {}
}