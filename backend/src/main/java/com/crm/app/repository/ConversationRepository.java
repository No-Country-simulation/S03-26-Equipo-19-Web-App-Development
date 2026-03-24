package com.crm.app.repository;

import com.crm.app.model.Contact;
import com.crm.app.model.Conversation;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Todas las conversaciones de un contacto (WhatsApp + email)
    List<Conversation> findByContact(Contact contact);

    // Conversación específica por contacto y canal — la restricción unique del modelo
    Optional<Conversation> findByContactAndChannel(Contact contact, Channel channel);

    // Conversaciones asignadas a un vendedor — vista del Vendedor
    List<Conversation> findByAssignedTo(User assignedTo);

    // Conversaciones abiertas asignadas a un vendedor — bandeja de entrada
    List<Conversation> findByAssignedToAndStatus(User assignedTo, ConversationStatus status);

    // Conversaciones de un contacto específico asignadas a un vendedor
    List<Conversation> findByContactAndAssignedTo(Contact contact, User assignedTo);

    // Todas las conversaciones abiertas — vista del Admin
    List<Conversation> findByStatus(ConversationStatus status);

    // Conversaciones ordenadas por última interacción — para la bandeja de entrada
    @Query("SELECT c FROM Conversation c WHERE c.assignedTo = :user " +
            "ORDER BY c.lastInteraction DESC NULLS LAST")
    List<Conversation> findByAssignedToOrderByLastInteraction(@Param("user") User user);

    // Cantidad de mensajes en cada conversación — para métricas
    @Query("SELECT c, COUNT(m) FROM Conversation c LEFT JOIN Message m ON m.conversation = c " +
            "WHERE c.assignedTo = :user GROUP BY c")
    List<Object[]> countMessagesByConversationForUser(@Param("user") User user);

    // Verificar que la conversación pertenece al vendedor antes de operar
    Optional<Conversation> findByIdAndAssignedTo(Long id, User assignedTo);
}
