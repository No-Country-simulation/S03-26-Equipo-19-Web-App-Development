package com.crm.app.repository;

import com.crm.app.model.Contact;
import com.crm.app.model.Task;
import com.crm.app.model.User;
import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Tareas de un vendedor — vista del Vendedor
    List<Task> findByAssignedTo(User assignedTo);

    // Tareas pendientes de un vendedor ordenadas por vencimiento
    List<Task> findByAssignedToAndStatusOrderByDueDateAsc(User assignedTo, TaskStatus status);

    // Todas las tareas de un contacto — para mostrar en el detalle del contacto
    List<Task> findByContact(Contact contact);

    // Tareas de un contacto asignadas a un vendedor específico
    List<Task> findByContactAndAssignedTo(Contact contact, User assignedTo);

    // Tareas vencidas que siguen en estado PENDING — para el job de actualización automática
    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.dueDate < :now")
    List<Task> findPendingOverdue(@Param("now") LocalDateTime now);

    // Tareas que vencen en las próximas N horas — para recordatorios automáticos
    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' " +
            "AND t.dueDate BETWEEN :now AND :deadline")
    List<Task> findDueSoon(
            @Param("now") LocalDateTime now,
            @Param("deadline") LocalDateTime deadline
    );

    // Marcar como OVERDUE todas las tareas vencidas — ejecutado por el scheduler
    @Modifying
    @Query("UPDATE Task t SET t.status = 'OVERDUE' " +
            "WHERE t.status = 'PENDING' AND t.dueDate < :now")
    int markOverdueTasks(@Param("now") LocalDateTime now);

    // Tareas completadas vs vencidas por vendedor — para métricas
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.assignedTo = :user " +
            "GROUP BY t.status")
    List<Object[]> countByStatusForUser(@Param("user") User user);

    // Tareas completadas vs vencidas globales — para métricas del Admin
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatusGlobal();

    // Verificar acceso del vendedor antes de operar sobre la tarea
    Optional<Task> findByIdAndAssignedTo(Long id, User assignedTo);

    // Tareas por tipo para un vendedor — para filtros de la UI
    List<Task> findByAssignedToAndType(User assignedTo, TaskType type);
}
