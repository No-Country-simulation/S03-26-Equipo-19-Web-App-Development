package com.crm.app.dto;

import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public class TaskDTOs {

    // =========================
    // CREATE
    // =========================
    public record TaskCreateRequest(
            @NotBlank(message = "El título es obligatorio")
            @Size(max = 150, message = "El título no puede superar los 150 caracteres")
            String title,

            @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
            String description,

            TaskType type,
         
            @Schema(
                description = "Fecha de vencimiento de la tarea",
                example = "2026-04-10T14:30:00",
                type = "string"
                )
            @NotNull(message = "La fecha de vencimiento es obligatoria")
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime dueDate,
            
            @NotNull(message = "El contacto es obligatorio")
            Long contactId
    ) {}


    // =========================
    // UPDATE
    // =========================
    public record TaskUpdateRequest(

                @Size(max = 150)
                String title,

                @Size(max = 1000)
                String description,

                TaskType type,

                @Schema(
                        description = "Nueva fecha de vencimiento (opcional)",
                        example = "2026-04-10T14:30:00",
                        type = "string"
                )
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                LocalDateTime dueDate
        ) {}

    // =========================
    // RESPONSE
    // =========================
    public record TaskResponse(
            Long id,
            String title,
            String description,
            TaskType type,
            TaskStatus status,
            
            @Schema(
                description = "Fecha de vencimiento",
                example = "2026-04-10T14:30:00",
                type = "string"
            )
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime dueDate,

            @Schema(
                description = "Fecha de finalización (si está completada)",
                example = "2026-04-10T18:00:00",
                type = "string"
            )
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime completedAt,

            Long contactId,
            Long assignedTo,

            @Schema(
                description = "Fecha de creación",
                example = "2026-04-15T12:23:22",
                type = "string"
            )
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime createdAt
    ) {}
}
