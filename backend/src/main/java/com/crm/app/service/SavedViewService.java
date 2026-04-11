package com.crm.app.service;

import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.UnauthorizedAccessException;
import com.crm.app.model.SavedView;
import com.crm.app.model.User;
import com.crm.app.model.enums.EntityType;
import com.crm.app.model.enums.FunnelStatus;
import com.crm.app.model.enums.Channel;
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
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class SavedViewService {


    private final SavedViewRepository repository;

    private static final Set<String> CONTACT_FIELDS = Set.of(
        "funnelStatus",
        "ownerId",
        "tagIds",
        "preferredChannel"
    );

    private static final Set<String> TASK_FIELDS = Set.of(
        "status",
        "type",
        "assignedTo",
        "dueDateFrom",
        "dueDateTo"
    );

    private static final Set<String> CONTACT_SORT_FIELDS = Set.of(
    "name", "createdAt", "funnelStatus"
    );

    private static final Set<String> TASK_SORT_FIELDS = Set.of(
        "dueDate", "status", "createdAt"
    );

    // =========================
    // CREATE
    // =========================
    @Transactional
    public SavedView create(SavedView view, User currentUser) {

        // Setear owner SIEMPRE
        view.setUser(currentUser);

        // Setear sortOrder por defecto
        if (view.getSortOrder() == null) {
            view.setSortOrder(SortOrder.ASC);
        }

        // Validar nombre duplicado
        if (repository.existsByNameAndUser(view.getName(), currentUser)) {
           throw new BusinessRuleViolationException(
            "SavedView",
            "Ya existe una vista con ese nombre"
            );
        }

        // Validar permisos de global
        if (view.isGlobal() && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessRuleViolationException(
            "SavedView",
            "Solo ADMIN puede crear vistas globales"
            );
        }

        // Validar entidad
        if (view.getEntity() == null) {
            throw new BusinessRuleViolationException(
            "SavedView",
            "entity es obligatorio"
            );
        }

        EntityType entity = view.getEntity();

        if (view.getSortBy() == null || view.getSortBy().isBlank()) {
            if (view.getEntity() == EntityType.CONTACTS) {
                view.setSortBy("createdAt");
            } else if (view.getEntity() == EntityType.TASKS) {
                view.setSortBy("dueDate");
            }
        }

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
                .orElseThrow(() -> new ResourceNotFoundException("SavedView", id));

        validateReadAccess(view, currentUser);

        return view;
    }

    // =========================
    // UPDATE
    // =========================
    @Transactional
    public SavedView update(Long id, SavedView updated, User currentUser) {
        // Traer la vista original
        SavedView existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavedView", id));

        // Validar que el usuario tenga permisos para modificar (owner o ADMIN)
        validateOwnership(existing, currentUser);

        // Setear sortOrder por defecto
        if (updated.getSortOrder() == null) {
            updated.setSortOrder(SortOrder.ASC);
        }

        // Validar nombre duplicado (si cambia)
        if (!existing.getName().equals(updated.getName()) &&
                repository.existsByNameAndUser(updated.getName(), currentUser)) {
            throw new BusinessRuleViolationException(
                "SavedView",
                "Ya existe una vista con ese nombre"
            );
        }

        // Validar global
        if (updated.isGlobal() && currentUser.getRole() != Role.ADMIN) {
            throw new BusinessRuleViolationException(
                "SavedView",
                "Solo ADMIN puede usar vistas globales"
            );
        }
        
        // Validar entidad
        if (updated.getEntity() == null) {
            throw new BusinessRuleViolationException(
                "SavedView",
                "entity es obligatorio"
            );
        }

        if (!existing.getEntity().equals(updated.getEntity())) {
            throw new BusinessRuleViolationException(
        "SavedView",
        "No se puede cambiar la entidad de la vista"
            );
        }

        EntityType entity = updated.getEntity();

        if (updated.getSortBy() == null || updated.getSortBy().isBlank()) {
            if (updated.getEntity() == EntityType.CONTACTS) {
                updated.setSortBy("createdAt");
            } else if (updated.getEntity() == EntityType.TASKS) {
                updated.setSortBy("dueDate");
            }
        }

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
    @Transactional
    public void delete(Long id, User currentUser) {

        SavedView view = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavedView", id));

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
            throw new BusinessRuleViolationException(
                "SavedView",
                "Filters no puede ser null"
            );
        }

        // El JSON puede ser vacío ({}), pero no puede ser otro tipo (ej: array, string, etc)
        if (!filters.isObject()) {
            throw new BusinessRuleViolationException(
                "SavedView",
                "Filters debe ser un objeto JSON"
            );
        }

        // Validar campos permitidos según la entidad
        switch (entity) {
            case CONTACTS -> validateContactFilters(filters);
            case TASKS -> validateTaskFilters(filters);
            default -> throw new BusinessRuleViolationException(
                "SavedView",
                "Entidad no soportada"
            );
        }
    }

    // Validar campos específicos para filtros de CONTACTS
    private void validateContactFilters(JsonNode filters) {

        validateAllowedFields(filters, CONTACT_FIELDS, "CONTACTS");

        if (filters.has("funnelStatus")) {
            try {
                FunnelStatus.valueOf(filters.get("funnelStatus").asText().toUpperCase());
            } catch (Exception e) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    "funnelStatus inválido"
                );
            }
        }

        if (filters.has("ownerId") && !filters.get("ownerId").canConvertToLong()) {
            throw new BusinessRuleViolationException(
                "SavedView",
                "ownerId debe ser numérico"
            );
        }

        if (filters.has("preferredChannel")) {
            try {
                Channel.valueOf(filters.get("preferredChannel").asText().toUpperCase());
            } catch (Exception e) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    "preferredChannel inválido"
                );
            }
        }

        if (filters.has("tagIds")) {
            JsonNode tagIdsNode = filters.get("tagIds");

            if (!tagIdsNode.isArray()) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    "tagIds debe ser un array"
                );
            }

            if (tagIdsNode.isEmpty()) {
                return; // no filtra por tags
            }

            for (JsonNode id : tagIdsNode) {
                if (!id.canConvertToLong()) {
                    throw new BusinessRuleViolationException(
                        "SavedView",
                        "tagIds debe contener números"
                    );
                }
            }
        }
    }

    // Validar campos específicos para filtros de TASKS
    private void validateTaskFilters(JsonNode filters) {
        
        validateAllowedFields(filters, TASK_FIELDS, "TASKS");

        if (filters.has("status")) {
            try {
                TaskStatus.valueOf(filters.get("status").asText().toUpperCase());
            } catch (Exception e) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    "status inválido"
                );
            }
        }

        if (filters.has("type")) {
            try {
                TaskType.valueOf(filters.get("type").asText().toUpperCase());
            } catch (Exception e) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    "type inválido"
                );
            }
        }

        if (filters.has("assignedTo") && !filters.get("assignedTo").canConvertToLong()) {
            throw new BusinessRuleViolationException(
                "SavedView",
                "assignedTo debe ser numérico"
            );
        }

        validateDateField(filters, "dueDateFrom");
        validateDateField(filters, "dueDateTo");

        if (filters.has("dueDateFrom") && filters.has("dueDateTo")) {
            LocalDate from = LocalDate.parse(filters.get("dueDateFrom").asText());
            LocalDate to = LocalDate.parse(filters.get("dueDateTo").asText());

            if (from.isAfter(to)) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    "dueDateFrom no puede ser mayor a dueDateTo"
                );
            }
        }
    }


    // Solo el owner o ADMIN pueden modificar o eliminar
    private void validateOwnership(SavedView view, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) return;

        if (!view.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("SavedView", view.getId());
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

        throw new UnauthorizedAccessException("SavedView", view.getId());
    }

    // Validar que el campo sortBy sea uno permitido según la entidad
    private void validateSortBy(String sortBy, EntityType entity) {

        if (sortBy == null || sortBy.isBlank()) {
            throw new BusinessRuleViolationException( 
                "SavedView",
                "sortBy es obligatorio"
            );
        }

        Set<String> allowed = switch (entity) {
            case CONTACTS -> CONTACT_SORT_FIELDS;
            case TASKS -> TASK_SORT_FIELDS;
        };

        if (!allowed.contains(sortBy)) {
            throw new BusinessRuleViolationException(
                "SavedView",
                "sortBy inválido para " + entity + ". Permitidos: " + allowed
            );
        }
    }

    // Validar que un campo de fecha tenga formato correcto (YYYY-MM-DD)
    private void validateDateField(JsonNode filters, String fieldName) {
        if (filters.has(fieldName)) {
            JsonNode node = filters.get(fieldName);

            if (!node.isTextual()) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    fieldName + " debe ser fecha (YYYY-MM-DD)"
                );
            }

            try {
                LocalDate.parse(node.asText());
            } catch (DateTimeParseException e) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    fieldName + " formato inválido (YYYY-MM-DD)"
                );
            }
        }
    } 

    // Validar que solo se usen campos permitidos en el JSON de filtros según la entidad
    private void validateAllowedFields(JsonNode filters, Set<String> allowed, String entityName) {

        filters.fields().forEachRemaining(entry -> {
            String field = entry.getKey();
            JsonNode value = entry.getValue();

            if (!allowed.contains(field)) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    "Campo no permitido para " + entityName + ": " + field
                );
            }

            if (value == null || value.isNull()) {
                throw new BusinessRuleViolationException(
                    "SavedView",
                    field + " no puede ser null"
                );
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