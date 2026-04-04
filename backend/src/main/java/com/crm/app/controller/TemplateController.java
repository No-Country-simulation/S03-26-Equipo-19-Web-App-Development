package com.crm.app.controller;

import com.crm.app.dto.TemplateDTOs;
import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Plantillas", description = "Gestión de plantillas (Admin: globales, Vendedor: personales)")
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear plantilla",
            description = "Admin crea plantillas globales. Vendedor crea plantillas personales (solo él las ve)")
    public ResponseEntity<Template> create(@Valid @RequestBody TemplateDTOs.TemplateRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar plantilla", description = "Solo el creador puede actualizar")
    public ResponseEntity<Template> update(@PathVariable Long id,
                                           @Valid @RequestBody TemplateDTOs.TemplateRequest request,
                                           @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar plantilla", description = "Solo el creador puede eliminar")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        templateService.deleteTemplate(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar plantillas",
            description = "Admin ve todas. Vendedor ve globales (de Admin) + sus personales")
    public ResponseEntity<List<Template>> list(@RequestParam(required = false) Channel channel,
                                               @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(templateService.listTemplates(currentUser, channel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener plantilla por ID", description = "Solo si tiene acceso")
    public ResponseEntity<Template> get(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(templateService.getTemplate(id, currentUser));
    }
}