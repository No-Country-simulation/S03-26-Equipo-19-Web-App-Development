package com.crm.app.controller;

import com.crm.app.dto.SavedViewRequest;
import com.crm.app.dto.SavedViewResponse;
import com.crm.app.mapper.SavedViewMapper;
import com.crm.app.model.SavedView;
import com.crm.app.model.User;
import com.crm.app.model.enums.EntityType;
import com.crm.app.service.SavedViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-views")
@RequiredArgsConstructor
public class SavedViewController {

    private final SavedViewService savedViewService;

    // =========================
    // CREATE
    // =========================
    @PostMapping
    public SavedViewResponse create(@RequestBody SavedViewRequest request) {

        User currentUser = getCurrentUser();

        SavedView view = SavedViewMapper.toEntity(request, currentUser);

        SavedView saved = savedViewService.create(view, currentUser);

        return SavedViewMapper.toResponse(saved);
    }

    // =========================
    // GET ALL
    // =========================
    @GetMapping
    public List<SavedViewResponse> getAll() {

        User currentUser = getCurrentUser();

        return savedViewService.getAccessible(currentUser)
                .stream()
                .map(SavedViewMapper::toResponse)
                .toList();
    }

    // =========================
    // GET BY ENTITY
    // =========================
    @GetMapping("/entity/{entity}")
    public List<SavedViewResponse> getByEntity(@PathVariable EntityType entity) {

        User currentUser = getCurrentUser();

        return savedViewService.getAccessibleByEntity(currentUser, entity)
                .stream()
                .map(SavedViewMapper::toResponse)
                .toList();
    }

    // =========================
    // GET ONE
    // =========================
    @GetMapping("/{id}")
    public SavedViewResponse getById(@PathVariable Long id) {

        User currentUser = getCurrentUser();

        SavedView view = savedViewService.getById(id, currentUser);

        return SavedViewMapper.toResponse(view);
    }

    // =========================
    // UPDATE
    // =========================
    @PutMapping("/{id}")
    public SavedViewResponse update(
            @PathVariable Long id,
            @RequestBody SavedViewRequest request
    ) {

        User currentUser = getCurrentUser();

        SavedView updated = SavedViewMapper.toEntity(request, currentUser);

        SavedView saved = savedViewService.update(id, updated, currentUser);

        return SavedViewMapper.toResponse(saved);
    }

    // =========================
    // DELETE
    // =========================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {

        User currentUser = getCurrentUser();

        savedViewService.delete(id, currentUser);
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