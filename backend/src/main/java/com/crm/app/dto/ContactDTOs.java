package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.FunnelStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class ContactDTOs {

    @Schema(description = "Datos básicos de contacto")
    public record ContactBase(
            @Schema(example = "Juan") String name,
            @Schema(example = "Pérez") String lastName,
            @Email @Schema(example = "juan@gmail.com") String email,
            @Schema(example = "5491123456789") String phone,
            @Schema(example = "Tech Solutions") String company
    ) {
        public boolean hasAtLeastOneIdentifier() {
            return (name != null && !name.isBlank()) ||
                    (lastName != null && !lastName.isBlank()) ||
                    (email != null && !email.isBlank()) ||
                    (phone != null && !phone.isBlank());
        }
    }

    @Schema(description = "Creación de contacto")
    public record CreateContactRequest(
            @NotNull ContactBase contact,
            Channel preferredChannel,
            @Schema(description = "Opcional. Solo Admin puede setearlo") Long ownerId
    ) {}

    // ✅ Respuesta completa (con tags) para GET y PATCH
    @Schema(description = "Respuesta completa de contacto (con tags)")
    public record ContactDetailResponse(
            Long id,
            String name,
            String lastName,
            String email,
            String phone,
            String company,
            FunnelStatus funnelStatus,
            Channel preferredChannel,
            OwnerInfo owner,
            List<TagInfo> tags,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    // ✅ Respuesta resumida (sin tags) para POST (creación)
    @Schema(description = "Respuesta resumida de contacto (sin tags)")
    public record ContactSummaryResponse(
            Long id,
            String name,
            String lastName,
            String email,
            String phone,
            String company,
            FunnelStatus funnelStatus,
            Channel preferredChannel,
            OwnerInfo owner,
            LocalDateTime createdAt
    ) {}

    @Schema(description = "Información resumida del vendedor")
    public record OwnerInfo(
            Long id,
            String name,
            String email
    ) {}

    @Schema(description = "Información resumida de la etiqueta")
    public record TagInfo(
            Long id,
            String name,
            String color
    ) {}
}