package com.crm.app.service;

import com.crm.app.model.SavedView;
import com.crm.app.model.User;
import com.crm.app.model.enums.EntityType;
import com.crm.app.model.enums.FunnelStatus;
import com.crm.app.model.enums.Role;
import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;
import com.crm.app.model.enums.SortOrder;
import com.crm.app.repository.SavedViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SavedViewService {


    private final SavedViewRepository repository;

    private static final Set<String> CONTACT_FIELDS = Set.of(
        "funnelStatus", "ownerId", "company", "email"
    );

    private static final Set<String> TASK_FIELDS = Set.of(
        "status", "type", "assignedTo", "contactId", "dueDateFrom", "dueDateTo"
    );

    private static final Set<String> CONTACT_SORT_FIELDS = Set.of(
    "name", "email", "createdAt", "funnelStatus"
    );

    private static final Set<String> TASK_SORT_FIELDS = Set.of(
        "dueDate", "status", "createdAt"
    );

    // =========================
    // CREATE
    // =========================
    public SavedView create(SavedView view, User currentUser) {

        // Setear owner SIEMPRE
        view.setUser(currentUser);

        // Setear sortOrder por defecto
        if (view.getSortOrder() == null) {
            view.setSortOrder(SortOrder.ASC);
        }

        // Validar nombre duplicado
        if (repository.existsByNameAndUser(view.getName(), currentUser)) {
            throw new IllegalArgumentException("Ya existe una vista con ese nombre");
        }

        // Validar permisos de global
        if (view.isGlobal() && currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Solo ADMIN puede crear vistas globales");
        }

        // Validar entidad
        if (view.getEntity() == null) {
            throw new IllegalArgumentException("entity es obligatorio");
        }

        EntityType entity = view.getEntity();

        validateSortBy(view.getSortBy(), entity);

        // Validar JSON
        validateFiltersByEntity(view.getFilters(), entity);

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
        // Traer la vista original
        SavedView existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vista no encontrada"));

        // Validar que el usuario tenga permisos para modificar (owner o ADMIN)
        validateOwnership(existing, currentUser);

        // Setear sortOrder por defecto
        if (updated.getSortOrder() == null) {
            updated.setSortOrder(SortOrder.ASC);
        }

        // Validar nombre duplicado (si cambia)
        if (!existing.getName().equals(updated.getName()) &&
                repository.existsByNameAndUser(updated.getName(), currentUser)) {
            throw new IllegalArgumentException("Ya existe una vista con ese nombre");
        }

        // Validar global
        if (updated.isGlobal() && currentUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Solo ADMIN puede usar vistas globales");
        }
        
        // Validar entidad
        if (updated.getEntity() == null) {
            throw new IllegalArgumentException("entity es obligatorio");
        }

        if (!existing.getEntity().equals(updated.getEntity())) {
            throw new IllegalArgumentException("No se puede cambiar la entidad de la vista");
        }

        EntityType entity = updated.getEntity();

        validateSortBy(updated.getSortBy(), entity);

        // Validar JSON
        validateFiltersByEntity(updated.getFilters(), entity);

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

    
    // Validar que el JSON de filtros sea correcto (no vacío, formato json válido)
    // Y que los campos dentro del JSON sean válidos según la entidad
    private void validateFiltersByEntity(JsonNode filters, EntityType entity) {
        
        // El JSON de filtros puede ser vacío ({}), pero no puede ser null ni otro tipo (ej: array, string, etc)
        if (filters == null || filters.isNull()) {
            throw new IllegalArgumentException("Filters no puede ser null");
        }

        // El JSON puede ser vacío ({}), pero no puede ser otro tipo (ej: array, string, etc)
        if (!filters.isObject()) {
            throw new IllegalArgumentException("Filters debe ser un objeto JSON");
        }

        // Validar campos permitidos según la entidad
        switch (entity) {
            case CONTACTS -> validateContactFilters(filters);
            case TASKS -> validateTaskFilters(filters);
            default -> throw new IllegalArgumentException("Entidad no soportada");
        }
    }

    // Validar campos específicos para filtros de CONTACTS
    private void validateContactFilters(JsonNode filters) {

        validateAllowedFields(filters, CONTACT_FIELDS, "CONTACTS");

        if (filters.has("funnelStatus")) {
            try {
                FunnelStatus.valueOf(filters.get("funnelStatus").asText());
            } catch (Exception e) {
                throw new IllegalArgumentException("funnelStatus inválido");
            }
        }

        if (filters.has("ownerId") && !filters.get("ownerId").canConvertToLong()) {
            throw new IllegalArgumentException("ownerId debe ser numérico");
        }

        if (filters.has("company") && !filters.get("company").isTextual()) {
            throw new IllegalArgumentException("company debe ser texto");
        }

        if (filters.has("email") && !filters.get("email").isTextual()) {
            throw new IllegalArgumentException("email debe ser texto");
        }
    }

    // Validar campos específicos para filtros de TASKS
    private void validateTaskFilters(JsonNode filters) {
        
        validateAllowedFields(filters, TASK_FIELDS, "TASKS");

        if (filters.has("status")) {
            try {
                TaskStatus.valueOf(filters.get("status").asText());
            } catch (Exception e) {
                throw new IllegalArgumentException("status inválido");
            }
        }

        if (filters.has("type")) {
            try {
                TaskType.valueOf(filters.get("type").asText());
            } catch (Exception e) {
                throw new IllegalArgumentException("type inválido");
            }
        }

        if (filters.has("assignedTo") && !filters.get("assignedTo").canConvertToLong()) {
            throw new IllegalArgumentException("assignedTo debe ser numérico");
        }

        if (filters.has("contactId") && !filters.get("contactId").canConvertToLong()) {
            throw new IllegalArgumentException("contactId debe ser numérico");
        }

        validateDateField(filters, "dueDateFrom");
        validateDateField(filters, "dueDateTo");

        if (filters.has("dueDateFrom") && filters.has("dueDateTo")) {
            LocalDate from = LocalDate.parse(filters.get("dueDateFrom").asText());
            LocalDate to = LocalDate.parse(filters.get("dueDateTo").asText());

            if (from.isAfter(to)) {
                throw new IllegalArgumentException("dueDateFrom no puede ser mayor a dueDateTo");
            }
        }
    }


    // Solo el owner o ADMIN pueden modificar o eliminar
    private void validateOwnership(SavedView view, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) return;

        if (!view.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No tenés permisos para modificar esta vista");
        }
    }


    // Para ver una vista (GET ONE), alcanza con que sea global o propia
    private void validateReadAccess(SavedView view, User currentUser) {

        // ADMIN puede todo
        if (currentUser.getRole() == Role.ADMIN) return;

        // Si es global → ok
        if (view.isGlobal()) return;

        // Si es propia → ok
        if (view.getUser().getId().equals(currentUser.getId())) return;

        throw new RuntimeException("No tenés acceso a esta vista");
    }

    // Validar que el campo sortBy sea uno permitido según la entidad
    private void validateSortBy(String sortBy, EntityType entity) {

        if (sortBy == null || sortBy.isBlank()) {
            throw new IllegalArgumentException("sortBy es obligatorio");
        }

        Set<String> allowed = switch (entity) {
            case CONTACTS -> CONTACT_SORT_FIELDS;
            case TASKS -> TASK_SORT_FIELDS;
        };

        if (!allowed.contains(sortBy)) {
            throw new IllegalArgumentException(
                "Campo inválido para " + entity + ": " + sortBy
            );
        }
    }

    // Validar que un campo de fecha tenga formato correcto (YYYY-MM-DD)
    private void validateDateField(JsonNode filters, String fieldName) {
        if (filters.has(fieldName)) {
            JsonNode node = filters.get(fieldName);

            if (!node.isTextual()) {
                throw new IllegalArgumentException(fieldName + " debe ser fecha (YYYY-MM-DD)");
            }

            try {
                LocalDate.parse(node.asText());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(fieldName + " formato inválido (YYYY-MM-DD)");
            }
        }
    } 

    // Validar que solo se usen campos permitidos en el JSON de filtros según la entidad
    private void validateAllowedFields(JsonNode filters, Set<String> allowed, String entityName) {
        filters.fieldNames().forEachRemaining(field -> {
            if (!allowed.contains(field)) {
                throw new IllegalArgumentException("Campo no permitido para " + entityName + ": " + field);
            }
        });
    }

    // Métodos adicionales para filtros específicos (global, entidad + global)
    public List<SavedView> getAccessibleByGlobal(User currentUser, boolean global) {
        return repository.findAccessibleByUserAndGlobal(currentUser, global);
    }

    // Filtros para vistas de una entidad específica + global
    public List<SavedView> getAccessibleByEntityAndGlobal(User currentUser, EntityType entity, boolean global) {
        return repository.findAccessibleByUserAndEntityAndGlobal(currentUser, entity, global);
    }

}