package com.crm.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class TagDTOs {

    public record TagRequest(

            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
            String name,

            @Pattern(
                regexp = "^#([A-Fa-f0-9]{6})$",
                message = "El color debe estar en formato HEX válido, ej: #FF5733"
            )
            String color

    ) {}

    public record TagResponse(
            Long id,
            String name,
            String color,
            LocalDateTime createdAt
    ) {}
}