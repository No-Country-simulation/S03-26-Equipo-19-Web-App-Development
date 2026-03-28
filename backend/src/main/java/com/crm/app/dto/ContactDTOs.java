package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class ContactDTOs {

    @Schema(description = "Datos básicos de contacto")
    public record ContactBase(

            @Schema(example = "Juan", description = "Nombre del contacto (opcional en creación automática)")
            String name,

            @Schema(example = "Pérez", description = "Apellido del contacto (opcional)")
            String lastName,

            @Email
            @Schema(example = "juan@gmail.com")
            String email,

            @Schema(example = "+5491123456789")
            String phone,

            @Schema(example = "Tech Solutions")
            String company
    ) {
        // Validación personalizada: al menos name o lastName o email o phone debe estar presente
        public boolean hasAtLeastOneIdentifier() {
            return (name != null && !name.isBlank()) ||
                    (lastName != null && !lastName.isBlank()) ||
                    (email != null && !email.isBlank()) ||
                    (phone != null && !phone.isBlank());
        }
    }

    @Schema(description = "Creación de contacto")
    public record CreateContactRequest(

            @NotNull
            ContactBase contact,

            @Schema(example = "Instagram Ads")
            String source,

            Channel preferredChannel,

            @Schema(description = "Solo Admin puede setearlo")
            Long ownerId
    ) {}
}