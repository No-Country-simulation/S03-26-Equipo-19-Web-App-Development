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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    // Tareas de un vendedor — vista del Vendedor
    List<Task> findByAssignedTo(User assignedTo);

    // Tareas de un vendedor filtradas por estado
    List<Task> findByAssignedToAndStatusOrderByDueDateAsc(User assignedTo, TaskStatus status);

    // Todas las tareas de un contacto
    List<Task> findByContact(Contact contact);

    // Tareas de un contacto asignadas a un vendedor específico
    List<Task> findByContactAndAssignedTo(Contact contact, User assignedTo);

    // Tareas vencidas que siguen en estado PENDING — para el job de actualización automática
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.dueDate < :now")
    List<Task> findPendingOverdue(@Param("status") TaskStatus status,
                             @Param("now") LocalDateTime now);

    // Tareas que vencen en las próximas N horas — para recordatorios automáticos
    @Query("SELECT t FROM Task t WHERE t.status = :status " +
       "AND t.dueDate BETWEEN :now AND :deadline")
    List<Task> findDueSoon(
            @Param("now") LocalDateTime now,
            @Param("deadline") LocalDateTime deadline
    );

    // Marcar como OVERDUE todas las tareas vencidas — ejecutado por el scheduler
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = 'OVERDUE' " +
            "WHERE t.status = 'PENDING' AND t.dueDate < :now")
    int markOverdueTasks(@Param("now") LocalDateTime now);

    // Conteo de tareas por estado para un vendedor — para métricas en el dashboard
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.assignedTo = :user " +
            "GROUP BY t.status")
    List<Object[]> countByStatusForUser(@Param("user") User user);

    // Conteo global de tareas por estado — para métricas del Admin
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatusGlobal();

    // Verificar acceso del vendedor antes de operar sobre la tarea
    Optional<Task> findByIdAndAssignedTo(Long id, User assignedTo);

    // Tareas por tipo para un vendedor — para filtros de la UI
    List<Task> findByAssignedToAndType(User assignedTo, TaskType type);

    // Tareas de un vendedor en un rango de fechas — para filtros avanzados
    List<Task> findByAssignedToAndDueDateBetweenOrderByDueDateAsc(
        User assignedTo,
        LocalDateTime start,
        LocalDateTime end
    );

    long countByStatus(TaskStatus status);

    long countByAssignedToAndStatus(User assignedTo, TaskStatus status);

    long countByAssignedToAndDueDateBetween(User assignedTo, LocalDateTime start, LocalDateTime end);

    long countByDueDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Task t " +
            "WHERE t.assignedTo = :user " +
            "AND t.status = :status " +
            "AND t.createdAt <= :endDate")
    long countByAssignedToAndStatusWithDate(@Param("user") User user,
                                            @Param("status") TaskStatus status,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Task t " +
            "WHERE t.status = :status " +
            "AND t.createdAt <= :endDate")
    long countByStatusWithDate(@Param("status") TaskStatus status,
                               @Param("endDate") LocalDateTime endDate);

    // Contar tareas vencidas con filtro de fecha
    @Query("SELECT COUNT(t) FROM Task t " +
            "WHERE t.assignedTo = :user " +
            "AND t.status = 'OVERDUE' " +
            "AND t.dueDate <= :endDate")
    long countOverdueByUserWithDate(@Param("user") User user,
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Task t " +
            "WHERE t.status = 'OVERDUE' " +
            "AND t.dueDate <= :endDate")
    long countOverdueWithDate(@Param("endDate") LocalDateTime endDate);

}
