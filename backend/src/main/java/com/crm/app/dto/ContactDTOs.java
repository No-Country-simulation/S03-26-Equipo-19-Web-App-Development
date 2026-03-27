package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public class ContactDTOs {

    @Schema(description = "Datos básicos de contacto")
    public record ContactBase(

            @NotBlank
            @Schema(example = "Juan Pérez")
            String name,

            @Email @NotBlank
            @Schema(example = "juan@gmail.com")
            String email,

            @NotBlank
            @Schema(example = "+5491123456789")
            String phone,

            @Schema(example = "Tech Solutions")
            String company
    ) {}

    @Schema(description = "Creación de contacto")
    public record CreateContactRequest(

            @NotNull
            ContactBase contact,

            @Schema(example = "Instagram Ads")
            String source,

            @NotNull
            Channel preferredChannel,

            @Schema(description = "Solo Admin puede setearlo")
            Long ownerId
    ) {}
}