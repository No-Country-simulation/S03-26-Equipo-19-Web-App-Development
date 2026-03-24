package com.crm.app.model;

import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Hilo de comunicación entre un vendedor y un contacto por un canal específico.
 *
 * Reglas de negocio:
 * - El canal es INMUTABLE. Una vez creada la conversación, no puede cambiar de canal.
 * - Un mismo contacto puede tener una conversación de WhatsApp y una de email en paralelo.
 *   Son dos entidades Conversation distintas con historiales separados.
 * - El campo lastInteraction se actualiza automáticamente con cada mensaje nuevo.
 *   Se usa para ordenar conversaciones y detectar contactos inactivos.
 * - assignedTo puede diferir del owner del contacto. El Admin puede asignar
 *   una conversación específica a un vendedor distinto al responsable del contacto.
 */
@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contact_id", "channel"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Contacto al que pertenece esta conversación.
     * La restricción unique (contact_id, channel) garantiza
     * que solo exista una conversación activa por contacto por canal.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    /**
     * Canal de comunicación. Inmutable después de la creación.
     * WhatsApp usa la Cloud API de Meta. Email usa SMTP o Brevo.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.OPEN;

    /**
     * Vendedor asignado a esta conversación.
     * Por defecto es el owner del contacto, pero el Admin puede reasignarla.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    /**
     * Timestamp del último mensaje en esta conversación.
     * Se actualiza con cada mensaje nuevo para permitir ordenamiento por actividad reciente.
     */
    @Column(name = "last_interaction")
    private LocalDateTime lastInteraction;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
