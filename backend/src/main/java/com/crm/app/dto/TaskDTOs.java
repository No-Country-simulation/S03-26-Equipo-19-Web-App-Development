package com.crm.app.dto;

import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

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
            
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime dueDate,

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime completedAt,

            Long contactId,
            Long assignedTo,

            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime createdAt
    ) {}
}
