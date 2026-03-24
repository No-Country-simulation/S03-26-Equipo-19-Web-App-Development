package com.crm.app.model;

import com.crm.app.model.enums.DeliveryStatus;
import com.crm.app.model.enums.MessageDirection;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Mensaje individual dentro de una conversación.
 * Puede ser enviado por el vendedor (OUTBOUND) o recibido del contacto (INBOUND).
 *
 * Reglas de negocio:
 * - Todo mensaje pertenece a una conversación. No existen mensajes huérfanos.
 * - El campo providerId es el identificador del mensaje en el proveedor externo
 *   (WhatsApp Cloud API o Brevo). Se usa para reconciliar webhooks de entrega y lectura.
 * - El campo template es nullable: solo se completa cuando el vendedor usó una plantilla.
 *   Si el mensaje fue redactado libremente, template es null.
 * - El sender es null cuando el mensaje es INBOUND (vino del contacto, no de un usuario).
 */
@Entity
@Table(name = "messages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Conversación a la que pertenece este mensaje.
     * La conversación define el canal y el contacto.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * Dirección del mensaje:
     * OUTBOUND = el vendedor escribió al contacto.
     * INBOUND  = el contacto respondió al vendedor.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MessageDirection direction;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * Estado de entrega del mensaje. Se actualiza vía webhooks del proveedor.
     * SENT → DELIVERED → READ (o FAILED si hubo error).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 15)
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.SENT;

    /**
     * ID del mensaje en el proveedor externo (WhatsApp o Brevo).
     * Necesario para correlacionar notificaciones de entrega y respuestas entrantes.
     * Puede ser null si el proveedor no retornó un ID (ej: mensajes de prueba).
     */
    @Column(name = "provider_id")
    private String providerId;

    /**
     * Plantilla usada para generar este mensaje. Nullable.
     * Si el vendedor redactó el mensaje manualmente, este campo es null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    /**
     * Usuario que envió el mensaje. Null cuando direction = INBOUND
     * (el mensaje lo envió el contacto, no un usuario del sistema).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;
}
