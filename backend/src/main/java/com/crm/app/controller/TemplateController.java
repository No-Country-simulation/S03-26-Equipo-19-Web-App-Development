package com.crm.app.controller;

import com.crm.app.dto.TemplateDTOs;
import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = "Plantillas", description = "Gestión de plantillas de mensajes (Admin puede CRUD, Vendedor solo lectura)")
public class TemplateController {

    private final TemplateService templateService;

    // ==================== ADMIN ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear plantilla", description = "Solo ADMIN puede crear plantillas")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plantilla creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o variables inconsistentes"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere ADMIN)")
    })
    public ResponseEntity<Template> create(@Valid @RequestBody TemplateDTOs.TemplateRequest request,
                                           @AuthenticationPrincipal User admin) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(request, admin));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar plantilla", description = "Solo ADMIN puede actualizar")
    public ResponseEntity<Template> update(@PathVariable Long id,
                                           @Valid @RequestBody TemplateDTOs.TemplateRequest request,
                                           @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request, admin));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar plantilla", description = "Solo ADMIN puede eliminar")
    @ApiResponse(responseCode = "204", description = "Plantilla eliminada")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User admin) {
        templateService.deleteTemplate(id, admin);
        return ResponseEntity.noContent().build();
    }

    // ==================== VENDEDOR (lectura) ====================

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar plantillas", description = "Cualquier usuario autenticado puede ver plantillas")
    @ApiResponse(responseCode = "200", description = "Lista de plantillas")
    public ResponseEntity<List<Template>> list(@RequestParam(required = false) Channel channel) {
        return ResponseEntity.ok(templateService.listTemplates(channel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener plantilla por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plantilla encontrada"),
            @ApiResponse(responseCode = "404", description = "Plantilla no encontrada")
    })
    public ResponseEntity<Template> get(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplate(id));
    }
}