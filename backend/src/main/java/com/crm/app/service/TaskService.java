
package com.crm.app.service;

import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.UnauthorizedAccessException;
import com.crm.app.model.Contact;
import com.crm.app.model.Task;
import com.crm.app.model.User;
import com.crm.app.model.enums.Role;
import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;
import com.crm.app.repository.ContactRepository;
import com.crm.app.repository.TaskRepository;
import com.crm.app.repository.UserRepository;
import com.crm.app.specification.TaskSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    private static final List<String> ALLOWED_SORT_FIELDS =
    List.of("dueDate", "status", "createdAt");
    // ==================== SEARCH ====================

    public List<Task> search(
            TaskStatus status,
            TaskType type,
            Long assignedTo,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            String sortBy,
            String sortOrder,
            User currentUser
    ) {

        // ================= VALIDACIONES =================

        
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "dueDate";
        }

        if (sortOrder == null || sortOrder.isBlank()) {
            sortOrder = "ASC";
        }

        sortOrder = sortOrder.toUpperCase();

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BusinessRuleViolationException("Parámetro de ordenamiento inválido: " + sortBy);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortOrder);
        } catch (Exception e) {
            throw new BusinessRuleViolationException(
                "Orden inválido",
                "sortOrder debe ser ASC o DESC"
            );
        }

        if (dueDateFrom != null && dueDateTo != null && dueDateFrom.isAfter(dueDateTo)) {
            throw new BusinessRuleViolationException(
                "Filtro inválido",
                "dueDateFrom no puede ser mayor a dueDateTo"
            );
        }

        Sort sort = Sort.by(direction, sortBy);

        LocalDateTime from = null;
        LocalDateTime to = null;

        if (dueDateFrom != null) {
            from = dueDateFrom.atStartOfDay(); // 00:00
        }

        if (dueDateTo != null) {
            to = dueDateTo.atTime(23, 59, 59, 999999999); // fin del día
        }
        // ================= SEGURIDAD =================

        if (currentUser.getRole() != Role.ADMIN) {
            assignedTo = currentUser.getId();
        }


        // ================= SPEC =================

        Specification<Task> spec = Specification
                .where(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasType(type))
                .and(TaskSpecification.hasAssignedTo(assignedTo))
                .and(TaskSpecification.dueDateBetween(from, to));

        
        return taskRepository.findAll(spec, sort);
    }

    // ==================== READ ====================

    public List<Task> getMyTasks(User currentUser) {
        return taskRepository.findByAssignedTo(currentUser);
    }

    public Task getById(Long id, User currentUser) {
        return findTaskWithAccess(id, currentUser);
    }

    public List<Task> getByContact(Long contactId, User currentUser) {
        Contact contact = findContactOrThrow(contactId);

        validateContactAccess(contact, currentUser);

        return taskRepository.findByContact(contact);
    }

    // ==================== CREATE ====================

    @Transactional
    public Task create(String title,
                       String description,
                       TaskType type,
                       LocalDateTime dueDate,
                       Long contactId,
                       User currentUser) {

        if (title == null || title.isBlank()) {
            throw new BusinessRuleViolationException("El título es obligatorio");
        }

        if (dueDate == null) {
            throw new BusinessRuleViolationException("La fecha de vencimiento es obligatoria");
        }

        Contact contact = findContactOrThrow(contactId);

        validateContactAccess(contact, currentUser);

        TaskType finalType = (type != null) ? type : TaskType.OTHER;

        Task task = Task.builder()
                .title(title.trim())
                .description(description)
                .type(finalType)
                .status(TaskStatus.PENDING)
                .dueDate(dueDate)
                .contact(contact)
                .assignedTo(contact.getOwner()) // default
                .build();

        return taskRepository.save(task);
    }

    // ==================== UPDATE ====================

    @Transactional
    public Task update(Long id,
                    String title,
                    String description,
                    TaskType type,
                    LocalDateTime dueDate,
                    User currentUser) {

        Task task = findTaskWithAccess(id, currentUser);

        if (title != null && !title.isBlank()) {
            task.setTitle(title.trim());
        }

        if (description != null) {
            task.setDescription(description);
        }

        if (type != null) {
            task.setType(type);
        }

        if (dueDate != null) {
            task.setDueDate(dueDate);
        }

        return taskRepository.save(task);
    }

    // ==================== COMPLETE ====================

    @Transactional
    public Task complete(Long taskId, User currentUser) {
        Task task = findTaskWithAccess(taskId, currentUser);

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessRuleViolationException("La tarea ya está completada");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    // ==================== REOPEN ====================

    @Transactional
    public Task reopen(Long taskId, User currentUser) {
        Task task = findTaskWithAccess(taskId, currentUser);

        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Solo se pueden reabrir tareas completadas");
        }

        task.setStatus(TaskStatus.PENDING);
        task.setCompletedAt(null);

        return taskRepository.save(task);
    }

    // ==================== REASSIGN ====================
    @Transactional
    public Task reassign(Long taskId, Long userId, User currentUser) {

        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo ADMIN puede reasignar tareas");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task no encontrada"));

        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User no encontrado"));

        task.setAssignedTo(newUser);

        return taskRepository.save(task);
    }
    // ==================== DELETE ====================

    @Transactional
    public void delete(Long taskId, User currentUser) {
        Task task = findTaskWithAccess(taskId, currentUser);
        taskRepository.delete(task);
    }

    // ==================== OVERDUE JOB ====================

    @Transactional
    public int markOverdueTasks() {
        return taskRepository.markOverdueTasks(LocalDateTime.now());
    }

    // ==================== PRIVATE ====================

    private Task findTaskWithAccess(Long taskId, User user) {
        if (user.getRole() == Role.ADMIN) {
            return taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task no encontrada con id: " + taskId));
        }

        return taskRepository.findByIdAndAssignedTo(taskId, user)
                .orElseThrow(() -> new UnauthorizedAccessException("No tienes acceso a esta tarea"));
    }

    private Contact findContactOrThrow(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact no encontrado con id: " + id));
    }

    private void validateContactAccess(Contact contact, User user) {
        if (user.getRole() == Role.ADMIN) return;

        if (!contact.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("No tienes acceso a este contacto");
        }
    }

    // ==================== MÉTRICAS ====================
    public List<Object[]> getMyMetrics(User user) {
        return taskRepository.countByStatusForUser(user);
    }

    public List<Object[]> getGlobalMetrics(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo ADMIN");
        }
        return taskRepository.countByStatusGlobal();
    }
}