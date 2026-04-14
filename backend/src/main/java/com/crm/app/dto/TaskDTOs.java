package com.crm.app.dto;

import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class TaskDTOs {

    // =========================
    // CREATE / UPDATE
    // =========================
    public record TaskRequest(
            @NotBlank(message = "El título es obligatorio")
            @Size(max = 150, message = "El título no puede superar los 150 caracteres")
            String title,

            @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
            String description,

            TaskType type,

            @NotNull(message = "La fecha de vencimiento es obligatoria")
            LocalDateTime dueDate,
            
            @NotNull(message = "El contacto es obligatorio")
            Long contactId
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
            LocalDateTime dueDate,
            LocalDateTime completedAt,
            Long contactId,
            Long assignedTo,
            LocalDateTime createdAt
    ) {}
}
