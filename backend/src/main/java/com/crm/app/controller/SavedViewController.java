package com.crm.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.crm.app.dto.SavedViewRequest;
import com.crm.app.dto.SavedViewResponse;
import com.crm.app.mapper.SavedViewMapper;
import com.crm.app.model.SavedView;
import com.crm.app.model.User;
import com.crm.app.model.enums.EntityType;
import com.crm.app.service.SavedViewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/saved-views")
@RequiredArgsConstructor
@Tag(name = "Saved Views", description = "Configuraciones de vistas guardadas por usuario")
public class SavedViewController {

    private final SavedViewService savedViewService;

    // =========================
    // CREATE
    // =========================
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear vista guardada")
    public ResponseEntity<SavedViewResponse> create(
        @RequestBody @Valid SavedViewRequest request,
        @AuthenticationPrincipal User currentUser
    ) {

        SavedView view = SavedViewMapper.toEntity(request, currentUser);

        SavedView saved = savedViewService.create(view, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SavedViewMapper.toResponse(saved));
    }

    // =========================
    // GET ALL
    // =========================
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar vistas accesibles (propias + globales)")
    public ResponseEntity<List<SavedViewResponse>> getAll(
            @RequestParam(required = false) EntityType entity,
            @RequestParam(required = false) Boolean global,
            @AuthenticationPrincipal User currentUser
    ) {

        List<SavedView> views;

        if (entity != null && global != null) {
            views = savedViewService.getAccessibleByEntityAndGlobal(currentUser, entity, global);

        } else if (entity != null) {
            views = savedViewService.getAccessibleByEntity(currentUser, entity);

        } else if (global != null) {
            views = savedViewService.getAccessibleByGlobal(currentUser, global);

        } else {
            views = savedViewService.getAccessible(currentUser);
        }

        // 3. Mapear a response
        List<SavedViewResponse> result = views.stream()
                .map(SavedViewMapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    // =========================
    // GET ONE
    // =========================
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener vista por ID")
    public ResponseEntity<SavedViewResponse> getById(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {

        SavedView view = savedViewService.getById(id, currentUser);

        return ResponseEntity.ok(SavedViewMapper.toResponse(view));
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar vista (solo owner o admin)")
    public ResponseEntity<SavedViewResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid SavedViewRequest request,
            @AuthenticationPrincipal User currentUser
    ) {

        SavedView updated = SavedViewMapper.toEntity(request, currentUser);

        SavedView saved = savedViewService.update(id, updated, currentUser);

        return ResponseEntity.ok(SavedViewMapper.toResponse(saved));
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar vista (solo owner o admin)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {

        savedViewService.delete(id, currentUser);

        return ResponseEntity.noContent().build();
    }

}