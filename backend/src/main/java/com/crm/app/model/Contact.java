package com.crm.app.model;

import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.FunnelStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa a una persona del mundo real con quien la startup
 * tiene o quiere tener una relación comercial.
 *
 * Reglas de negocio clave:
 * - Siempre tiene un vendedor responsable (owner). Nunca puede ser nulo.
 * - El estado del funnel refleja en qué punto del proceso de venta se encuentra.
 * - Un Vendedor solo puede ver los contactos donde él es el owner.
 * - El Admin puede ver y gestionar todos los contactos.
 */
@Entity
@Table(name = "contacts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del contacto. Puede ser null inicialmente (ej: desde webhook)
     */
    @Column(nullable = true)
    private String name;

    /**
     * Apellido del contacto. Puede ser null inicialmente (ej: desde webhook)
     */
    @Column(name = "last_name", nullable = true)
    private String lastName;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String company;

    /**
     * Estado actual del contacto en el funnel de ventas.
     * Este campo es el corazón de la segmentación del CRM.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "funnel_status", nullable = false, length = 30)
    @Builder.Default
    private FunnelStatus funnelStatus = FunnelStatus.NEW_LEAD;

    /**
     * Cómo llegó el contacto al sistema. Ej: "LinkedIn", "Referido", "Formulario web".
     * Puede ser null para contactos creados automáticamente.
     */
    @Column(nullable = true)
    private String source;

    /**
     * Canal de comunicación preferido del contacto.
     * Determina por dónde se iniciará la primera conversación por defecto.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_channel", length = 20)
    private Channel preferredChannel;

    /**
     * Vendedor responsable de este contacto.
     * NUNCA puede ser nulo. Si el vendedor es desactivado,
     * el Admin debe reasignar el contacto antes.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User owner;

    /**
     * Etiquetas de clasificación. Muchos-a-muchos con Tag.
     * Un contacto puede tener múltiples etiquetas simultáneamente.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "contact_tags",
            joinColumns = @JoinColumn(name = "contact_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}