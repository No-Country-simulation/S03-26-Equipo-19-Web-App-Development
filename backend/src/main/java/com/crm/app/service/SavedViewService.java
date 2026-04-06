package com.crm.app.service;

import com.crm.app.model.SavedView;
import com.crm.app.model.User;
import com.crm.app.model.enums.EntityType;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.SavedViewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedViewService {

    private final SavedViewRepository repository;
    private final ObjectMapper objectMapper;

    // =========================
    // CREATE
    // =========================
    public SavedView create(SavedView view, User currentUser) {

        // Setear owner SIEMPRE
        view.setUser(currentUser);

        // Validar nombre duplicado
        if (repository.existsByNameAndUser(view.getName(), currentUser)) {
            throw new IllegalArgumentException("Ya existe una vista con ese nombre");
        }

        // Validar permisos de global
        if (view.isGlobal() && currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Solo ADMIN puede crear vistas globales");
        }

        // Validar JSON
        validateFilters(view.getFilters(), view.getEntity());

        return repository.save(view);
    }

    // =========================
    // GET ALL (lo que ve el usuario)
    // =========================
    public List<SavedView> getAccessible(User currentUser) {
        return repository.findAccessibleByUser(currentUser);
    }

    public List<SavedView> getAccessibleByEntity(User currentUser, EntityType entity) {
        return repository.findAccessibleByUserAndEntity(currentUser, entity);
    }

    // =========================
    // GET ONE (con seguridad)
    // =========================
    public SavedView getById(Long id, User currentUser) {
        SavedView view = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vista no encontrada"));

        validateReadAccess(view, currentUser);

        return view;
    }

    // =========================
    // UPDATE
    // =========================
    public SavedView update(Long id, SavedView updated, User currentUser) {

        SavedView existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vista no encontrada"));

        validateOwnership(existing, currentUser);

        // Validar nombre duplicado (si cambia)
        if (!existing.getName().equals(updated.getName()) &&
                repository.existsByNameAndUser(updated.getName(), currentUser)) {
            throw new IllegalArgumentException("Ya existe una vista con ese nombre");
        }

        // Validar global
        if (updated.isGlobal() && currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Solo ADMIN puede usar vistas globales");
        }

        // Validar JSON
        validateFilters(updated.getFilters(), updated.getEntity());

        // Update campos
        existing.setName(updated.getName());
        existing.setFilters(updated.getFilters());
        existing.setEntity(updated.getEntity());
        existing.setSortBy(updated.getSortBy());
        existing.setSortOrder(updated.getSortOrder());
        existing.setGlobal(updated.isGlobal());

        return repository.save(existing);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(Long id, User currentUser) {

        SavedView view = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vista no encontrada"));

        validateOwnership(view, currentUser);

        repository.delete(view);
    }

    // =========================
    // VALIDACIONES
    // =========================

    private void validateFilters(String filters, EntityType entity) {
        try {
            // Solo validamos que sea JSON válido por ahora
            objectMapper.readTree(filters);

        } catch (Exception e) {
            throw new IllegalArgumentException("Filters JSON inválido");
        }
    }

    private void validateOwnership(SavedView view, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) return;

        if (!view.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tenés permisos para modificar esta vista");
        }
    }

    private void validateReadAccess(SavedView view, User currentUser) {

        // ADMIN puede todo
        if (currentUser.getRole() == Role.ADMIN) return;

        // Si es global → ok
        if (view.isGlobal()) return;

        // Si es propia → ok
        if (view.getUser().getId().equals(currentUser.getId())) return;

        throw new RuntimeException("No tenés acceso a esta vista");
    }
}