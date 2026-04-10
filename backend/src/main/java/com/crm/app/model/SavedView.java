package com.crm.app.model;

import com.crm.app.model.enums.EntityType;
import com.crm.app.model.enums.SortOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;


/**
 * Vista guardada: configuración de filtros que un usuario reutiliza frecuentemente.
 *
 * Reglas:
 * - entity solo puede ser CONTACTS o TASKS
 * - sortOrder solo puede ser ASC o DESC
 * - filters debe ser un JSON válido (validar antes de persistir)
 * - global indica si la vista es compartida (true) o privada (false)
 */
@Entity
@Table(name = "saved_views")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    @Size(min = 3, max = 150, message = "Nombre obligatorio y entre 3 y 150 caracteres")
    private String name;

    /**
     * Configuración de filtros serializada en JSON.
     * El backend interpreta este JSON para construir la query dinámica.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode filters;

    /**
     * Entidad sobre la que opera esta vista.
     * Valores posibles: "contacts", "tasks".
     * Determina qué endpoint se llama al abrir la vista.
     */
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntityType entity;

    @Column(name = "sort_by", nullable = false, length = 50)
    private String sortBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_order", nullable = false, length = 4)
    @Builder.Default
    private SortOrder sortOrder = SortOrder.ASC;

    // Indica si la vista es global (compartida) o privada.
    @Column(nullable = false)
    @Builder.Default
    private boolean global = false;

    // Relación con el usuario propietario de la vista.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
