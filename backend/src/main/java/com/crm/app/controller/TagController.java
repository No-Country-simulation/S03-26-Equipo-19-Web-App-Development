package com.crm.app.controller;

import com.crm.app.dto.TagDTOs;
import com.crm.app.mapper.TagMapper;
import com.crm.app.model.Tag;
import com.crm.app.model.User;
import com.crm.app.service.TagService;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Etiquetas",
    description = "Gestión de etiquetas de clasificación"
)
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    // =========================
    // CREATE
    // =========================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear etiqueta (solo admin)")
    public ResponseEntity<TagDTOs.TagResponse> create(
            @RequestBody @Valid TagDTOs.TagRequest request,
            @AuthenticationPrincipal User currentUser
    ) {

        Tag tag = tagService.create(request.name(), request.color(), currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tagMapper.toResponse(tag));
    }

    // =========================
    // GET ALL
    // =========================
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar todas las etiquetas")
    public ResponseEntity<List<TagDTOs.TagResponse>> getAll(
            @AuthenticationPrincipal User currentUser
    ) {

        List<Tag> tags = tagService.getAll(currentUser);

        return ResponseEntity.ok(tagMapper.toResponseList(tags));
    }

    // =========================
    // SEARCH
    // =========================
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar etiquetas por nombre")
    public ResponseEntity<List<TagDTOs.TagResponse>> search(
            @RequestParam String name,
            @AuthenticationPrincipal User currentUser
    ) {

        List<Tag> tags = tagService.search(name, currentUser);

        return ResponseEntity.ok(tagMapper.toResponseList(tags));
    }

    // =========================
    // GET ONE
    // =========================
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener etiqueta por ID")
    public ResponseEntity<TagDTOs.TagResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {

        Tag tag = tagService.getById(id, currentUser);

        return ResponseEntity.ok(tagMapper.toResponse(tag));
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar etiqueta (solo admin)")
    public ResponseEntity<TagDTOs.TagResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid TagDTOs.TagRequest request,
            @AuthenticationPrincipal User currentUser
    ) {

        Tag updated = tagService.update(id, request.name(), request.color(), currentUser);

        return ResponseEntity.ok(tagMapper.toResponse(updated));
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar etiqueta (solo admin)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {

        tagService.delete(id, currentUser);

        return ResponseEntity.noContent().build();
    }
}