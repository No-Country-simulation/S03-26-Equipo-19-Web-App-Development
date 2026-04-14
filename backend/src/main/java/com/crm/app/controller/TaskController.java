package com.crm.app.controller;

import com.crm.app.dto.TaskDTOs;
import com.crm.app.mapper.TaskMapper;
import com.crm.app.model.Task;
import com.crm.app.model.User;
import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;
import com.crm.app.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tareas", description = "Gestión de tareas del CRM")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    // =========================
    // CREATE
    // =========================
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear tarea")
    public ResponseEntity<TaskDTOs.TaskResponse> create(
            @RequestBody @Valid TaskDTOs.TaskRequest request,
            @AuthenticationPrincipal User currentUser
    ) {

        Task task = taskService.create(
                request.title(),
                request.description(),
                request.type(),
                request.dueDate(),
                request.contactId(),
                currentUser
        );

        return ResponseEntity.status(HttpStatus.CREATED)
        .body(taskMapper.toResponse(task));
    }

    // =========================
    // GET ALL (FILTROS + SORT)
    // =========================
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar tareas con filtros y ordenamiento")
    public ResponseEntity<List<TaskDTOs.TaskResponse>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskType type,
            @RequestParam(required = false) Long assignedTo,
            
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueDateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueDateTo,

            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @AuthenticationPrincipal User currentUser
    ) {

        List<Task> tasks = taskService.search(
                status,
                type,
                assignedTo,
                dueDateFrom,
                dueDateTo,
                sortBy,
                sortOrder,
                currentUser
        );

        return ResponseEntity.ok(taskMapper.toResponseList(tasks));
    }

    // =========================
    // GET ONE
    // =========================
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener tarea por ID")
    public ResponseEntity<TaskDTOs.TaskResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {

        Task task = taskService.getById(id, currentUser);

        return ResponseEntity.ok(taskMapper.toResponse(task));
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar tarea")
    public ResponseEntity<TaskDTOs.TaskResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid TaskDTOs.TaskRequest request,
            @AuthenticationPrincipal User currentUser
    ) {

        Task updated = taskService.update(
                id,
                request.title(),
                request.description(),
                request.type(),
                request.dueDate(),
                currentUser
        );

        return ResponseEntity.ok(taskMapper.toResponse(updated));
    }

    // =========================
    // COMPLETE
    // =========================
    @PatchMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marcar tarea como completada")
    public ResponseEntity<TaskDTOs.TaskResponse> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {

        Task task = taskService.complete(id, currentUser);

        return ResponseEntity.ok(taskMapper.toResponse(task));
    }

    // =========================
    // REOPEN
    // =========================
    @PatchMapping("/{id}/reopen")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reabrir tarea")
    public ResponseEntity<TaskDTOs.TaskResponse> reopen(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {

        Task task = taskService.reopen(id, currentUser);

        return ResponseEntity.ok(taskMapper.toResponse(task));
    }

    // =========================
    // REASSIGN (ADMIN)
    // =========================
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reasignar tarea (solo ADMIN)")
    public ResponseEntity<TaskDTOs.TaskResponse> reassign(
            @PathVariable Long id,
            @RequestParam Long userId,
            @AuthenticationPrincipal User currentUser
    ) {

        Task task = taskService.reassign(id, userId, currentUser);

        return ResponseEntity.ok(taskMapper.toResponse(task));
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar tarea")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {

        taskService.delete(id, currentUser);

        return ResponseEntity.noContent().build();
    }
}