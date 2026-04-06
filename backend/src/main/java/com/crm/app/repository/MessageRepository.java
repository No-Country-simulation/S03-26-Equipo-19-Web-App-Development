package com.crm.app.repository;

import com.crm.app.model.Conversation;
import com.crm.app.model.enums.DeliveryStatus;
import com.crm.app.model.Message;
import com.crm.app.model.enums.MessageDirection;
import com.crm.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Historial completo de una conversación ordenado cronológicamente
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

    // Mensajes por dirección en una conversación — para calcular tasa de respuesta
    List<Message> findByConversationAndDirection(Conversation conversation, MessageDirection direction);

    // Buscar por providerId — para reconciliar webhooks de entrega de WhatsApp o Brevo
    Optional<Message> findByProviderId(String providerId);

    // Mensajes enviados por un vendedor en un período — para métricas propias
    @Query("SELECT m FROM Message m WHERE m.sender = :sender " +
            "AND m.direction = 'OUTBOUND' " +
            "AND m.sentAt BETWEEN :from AND :to")
    List<Message> findOutboundBySenderAndPeriod(
            @Param("sender") User sender,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Total de mensajes enviados en un período — métricas globales del Admin
    @Query("SELECT COUNT(m) FROM Message m WHERE m.direction = 'OUTBOUND' " +
            "AND m.sentAt BETWEEN :from AND :to")
    long countOutboundInPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Total de mensajes recibidos en un período — para calcular tasa de respuesta global
    @Query("SELECT COUNT(m) FROM Message m WHERE m.direction = 'INBOUND' " +
            "AND m.sentAt BETWEEN :from AND :to")
    long countInboundInPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Mensajes pendientes de entrega — para reintentos o alertas
    List<Message> findByDeliveryStatus(DeliveryStatus deliveryStatus);

    // Cantidad de mensajes enviados por vendedor — ranking del Admin
    @Query("SELECT m.sender, COUNT(m) FROM Message m WHERE m.direction = 'OUTBOUND' " +
            "AND m.sentAt BETWEEN :from AND :to GROUP BY m.sender")
    List<Object[]> countOutboundPerSenderInPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<Message> findBySender(User sender);
}
