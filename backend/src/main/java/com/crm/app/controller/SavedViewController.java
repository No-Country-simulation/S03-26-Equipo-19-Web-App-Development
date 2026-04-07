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

import com.crm.app.dto.SavedViewRequest;
import com.crm.app.dto.SavedViewResponse;
import com.crm.app.mapper.SavedViewMapper;
import com.crm.app.model.SavedView;
import com.crm.app.model.User;
import com.crm.app.model.enums.EntityType;
import com.crm.app.service.SavedViewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/saved-views")
@RequiredArgsConstructor
public class SavedViewController {

    private final SavedViewService savedViewService;

    // =========================
    // CREATE
    // =========================
    @PostMapping
    public ResponseEntity<SavedViewResponse> create(@RequestBody @Valid SavedViewRequest request) {

        User currentUser = getCurrentUser();

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
    public ResponseEntity<List<SavedViewResponse>> getAll() {

        User currentUser = getCurrentUser();

        List<SavedViewResponse> result = savedViewService.getAccessible(currentUser)
                .stream()
                .map(SavedViewMapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    // =========================
    // GET BY ENTITY
    // =========================
    @GetMapping("/entity/{entity}")
    public ResponseEntity<List<SavedViewResponse>> getByEntity(@PathVariable EntityType entity) {

        User currentUser = getCurrentUser();

        List<SavedViewResponse> result = savedViewService.getAccessibleByEntity(currentUser, entity)
                .stream()
                .map(SavedViewMapper::toResponse)
                .toList();

        return ResponseEntity.ok(result);
    }

    // =========================
    // GET ONE
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<SavedViewResponse> getById(@PathVariable Long id) {

        User currentUser = getCurrentUser();

        SavedView view = savedViewService.getById(id, currentUser);

        return ResponseEntity.ok(SavedViewMapper.toResponse(view));
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<SavedViewResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid SavedViewRequest request
    ) {

        User currentUser = getCurrentUser();

        SavedView updated = SavedViewMapper.toEntity(request, currentUser);

        SavedView saved = savedViewService.update(id, updated, currentUser);

        return ResponseEntity.ok(SavedViewMapper.toResponse(saved));
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        User currentUser = getCurrentUser();

        savedViewService.delete(id, currentUser);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // MOCK USER (temporal)
    // =========================
    private User getCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Anthony");
        return user;
    }
}