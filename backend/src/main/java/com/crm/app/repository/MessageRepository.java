package com.crm.app.repository;

import com.crm.app.model.Conversation;
import com.crm.app.model.enums.DeliveryStatus;
import com.crm.app.model.Message;
import com.crm.app.model.enums.MessageDirection;
import com.crm.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // ✅ MÉTRICAS GLOBALES
    @Query("SELECT COUNT(m) FROM Message m WHERE m.direction = 'OUTBOUND'")
    long countOutboundMessages();

    @Query("SELECT COUNT(m) FROM Message m WHERE m.direction = 'OUTBOUND' AND m.sentAt BETWEEN :start AND :end")
    long countOutboundMessagesInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.direction = 'INBOUND' AND m.sentAt BETWEEN :start AND :end")
    long countInboundMessagesInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.channel, COUNT(m) FROM Message m JOIN m.conversation c GROUP BY c.channel")
    List<Object[]> countMessagesByChannel();

    @Query("SELECT u.name, COUNT(m) FROM Message m JOIN m.sender u WHERE u.role = 'SALESPERSON' GROUP BY u.id, u.name ORDER BY COUNT(m) DESC")
    List<Object[]> findTopSalespersonsByMessages();

    @Query("SELECT COUNT(DISTINCT c.id) FROM Conversation c WHERE EXISTS (SELECT m FROM Message m WHERE m.conversation = c AND m.direction = 'INBOUND')")
    long countConversationsWithInbound();

    @Query("SELECT COUNT(DISTINCT c.id) FROM Conversation c")
    long countTotalConversations();

    // ✅ MÉTRICAS POR VENDEDOR
    @Query("SELECT COUNT(m) FROM Message m WHERE m.direction = 'OUTBOUND' AND m.sender = :sender AND m.sentAt BETWEEN :start AND :end")
    long countOutboundBySenderAndPeriod(@Param("sender") User sender, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.direction = 'INBOUND' AND m.conversation.contact.owner = :owner AND m.sentAt BETWEEN :start AND :end")
    long countInboundByContactOwnerAndPeriod(@Param("owner") User owner, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.channel, COUNT(m) FROM Message m JOIN m.conversation c WHERE m.sender = :sender AND m.sentAt BETWEEN :start AND :end GROUP BY c.channel")
    List<Object[]> countOutboundByChannelAndSender(@Param("sender") User sender, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.channel, COUNT(m) FROM Message m JOIN m.conversation c WHERE m.sentAt BETWEEN :start AND :end GROUP BY c.channel")
    List<Object[]> countOutboundByChannelInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT m FROM Message m WHERE m.conversation IN :conversations ORDER BY m.sentAt ASC")
    List<Message> findByConversationInOrderBySentAtAsc(@Param("conversations") List<Conversation> conversations);


    /**
     * Obtiene el último mensaje de cada conversación del usuario
     * donde el contacto tiene funnelStatus = NEW_LEAD
     * Ordenado por fecha descendente (más reciente primero)
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.assignedTo = :user " +
            "AND m.conversation.contact.funnelStatus = 'NEW_LEAD' " +
            "AND m.id IN (" +
            "    SELECT MAX(m2.id) FROM Message m2 " +
            "    WHERE m2.conversation.assignedTo = :user " +
            "    AND m2.conversation.contact.funnelStatus = 'NEW_LEAD' " +
            "    GROUP BY m2.conversation.id" +
            ") " +
            "ORDER BY m.sentAt DESC")
    List<Message> findLastMessagePerNewLeadConversationForUser(@Param("user") User user);

    /**
     * Para ADMIN - obtiene el último mensaje de TODAS las conversaciones
     * donde el contacto tiene funnelStatus = NEW_LEAD
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation.contact.funnelStatus = 'NEW_LEAD' " +
            "AND m.id IN (" +
            "    SELECT MAX(m2.id) FROM Message m2 " +
            "    WHERE m2.conversation.contact.funnelStatus = 'NEW_LEAD' " +
            "    GROUP BY m2.conversation.id" +
            ") " +
            "ORDER BY m.sentAt DESC")
    List<Message> findLastMessagePerAllNewLeadConversations();

    // En MessageRepository.java - Agrega estos métodos

    // Marcar un mensaje específico como leído
    @Modifying
    @Query("UPDATE Message m SET m.deliveryStatus = 'READ' WHERE m.id = :messageId AND m.direction = 'INBOUND'")
    int markAsRead(@Param("messageId") Long messageId);

    // Marcar todos los mensajes INBOUND de una conversación como leídos
    @Modifying
    @Query("UPDATE Message m SET m.deliveryStatus = 'READ' " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.direction = 'INBOUND' " +
            "AND m.deliveryStatus != 'READ'")
    int updateInboundMessagesToRead(@Param("conversationId") Long conversationId);

    // Contar mensajes no leídos de una conversación
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.direction = 'INBOUND' " +
            "AND m.deliveryStatus != 'READ'")
    long countUnreadByConversation(@Param("conversationId") Long conversationId);
}
