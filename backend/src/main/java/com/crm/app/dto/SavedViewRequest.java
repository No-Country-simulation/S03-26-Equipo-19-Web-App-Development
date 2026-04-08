package com.crm.app.dto;

import com.crm.app.model.enums.EntityType;
import com.crm.app.model.enums.SortOrder;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SavedViewRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    private String name;

    @NotNull(message = "filters es obligatorio")
    private JsonNode filters;

    @NotNull(message = "entity es obligatorio")
    private EntityType entity;

    @NotBlank(message = "sortBy es obligatorio")
    private String sortBy;

    // opcional → lo maneja el service con default
    private SortOrder sortOrder;

    private boolean global;
}