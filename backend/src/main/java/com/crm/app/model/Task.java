package com.crm.app.model;

import com.crm.app.model.enums.TaskStatus;
import com.crm.app.model.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tarea de seguimiento asociada a un contacto y a un usuario responsable.
 * Garantiza que ningún contacto quede sin atención por falta de recordatorio.
 *
 * Reglas de negocio:
 * - Toda tarea debe tener un contacto y un responsable. Ninguno puede ser nulo.
 * - El sistema marca automáticamente como OVERDUE las tareas cuya fecha de vencimiento
 *   pasó sin ser completadas. Esto se hace vía un job programado.
 * - El campo completedAt se completa solo cuando el status pasa a COMPLETED.
 * - Un Vendedor solo puede ver y gestionar sus propias tareas.
 * - El Admin puede ver las tareas de todo el equipo para detectar cuellos de botella.
 */
@Entity
@Table(name = "tasks")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Tipo de acción que representa la tarea.
     * Ayuda al vendedor a prepararse: no es lo mismo una llamada que una demo.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private TaskType type = TaskType.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * Fecha límite para completar la tarea.
     * El sistema genera recordatorios antes de este momento.
     */
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    /**
     * Momento en que el vendedor marcó la tarea como completada. Nullable.
     * Solo se completa cuando status = COMPLETED.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Contacto al que pertenece esta tarea.
     * Una tarea siempre hace referencia a un contacto específico.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    /**
     * Vendedor responsable de completar esta tarea.
     * Por defecto es el owner del contacto, pero el Admin puede asignarla a otro.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
