package com.crm.app.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Vista guardada: configuración de filtros que un usuario reutiliza frecuentemente.
 *
 * Qué NO es: no almacena los resultados de la búsqueda.
 * Qué SÍ es: almacena la configuración del filtro en JSON.
 *            Cada vez que se abre la vista, el sistema ejecuta la consulta
 *            en ese momento y devuelve resultados frescos.
 *
 * Reglas de negocio:
 * - Una vista con global = false solo es visible para el usuario que la creó.
 * - Una vista con global = true es visible para todos (solo el Admin puede crearlas).
 * - Las vistas de un Vendedor, aunque sean globales, solo devuelven sus propios datos.
 *   El filtro de visibilidad por rol se aplica siempre en el backend.
 * - El campo filters es JSON. Ejemplo:
 *   {"funnelStatus": "IN_NEGOTIATION", "tagIds": [1, 3], "ownerIds": [5]}
 * - El campo entity indica a qué listado aplica la vista: "contacts", "tasks", "conversations".
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

    /**
     * Nombre descriptivo de la vista. Ej: "Mis leads calientes esta semana".
     */
    @Column(nullable = false)
    private String name;

    /**
     * Configuración de filtros serializada en JSON.
     * El backend interpreta este JSON para construir la query dinámica.
     * Ejemplo: {"funnelStatus":"IN_NEGOTIATION","tagIds":[1,3],"daysInactive":7}
     */
    @Column(nullable = false, columnDefinition = "TEXT")  // <- Cambiar a TEXT
    private String filters;

    /**
     * Entidad sobre la que opera esta vista.
     * Valores posibles: "contacts", "tasks", "conversations".
     * Determina qué endpoint se llama al abrir la vista.
     */
    @Column(nullable = false, length = 30)
    private String entity;

    /**
     * Si es true, la vista es visible para todos los usuarios del sistema.
     * Solo el Admin puede crear vistas globales.
     * Si es false, solo el creador puede verla y usarla.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean global = false;

    /**
     * Usuario que creó la vista. Para vistas globales, siempre es un Admin.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
