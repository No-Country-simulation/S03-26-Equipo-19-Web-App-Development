package com.crm.app.service;

import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.UnauthorizedAccessException;
import com.crm.app.model.Contact;
import com.crm.app.model.Conversation;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.ConversationStatus;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.ConversationRepository;
import com.crm.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ContactService contactService;
    private final UserRepository userRepository; // ✅ Inyectado directamente

    /**
     * Obtiene una conversación existente o la crea si no existe
     */
    public Conversation getOrCreateConversation(Long contactId, Channel channel, User currentUser) {
        Contact contact = contactService.findByIdAndCheckAccess(contactId, currentUser);

        return conversationRepository.findByContactAndChannel(contact, channel)
                .orElseGet(() -> createNewConversation(contact, channel, currentUser));
    }

    /**
     * Crea una nueva conversación
     */
    private Conversation createNewConversation(Contact contact, Channel channel, User currentUser) {
        // Validar que el contacto tenga la información necesaria para el canal
        if (channel == Channel.WHATSAPP && (contact.getPhone() == null || contact.getPhone().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "No se puede crear una conversación por WhatsApp porque el contacto no tiene número de teléfono"
            );
        }

        if (channel == Channel.EMAIL && (contact.getEmail() == null || contact.getEmail().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "No se puede crear una conversación por email porque el contacto no tiene dirección de correo"
            );
        }

        // Determinar quién asigna la conversación
        User assignedTo;
        if (currentUser.getRole() == Role.ADMIN) {
            assignedTo = contact.getOwner();
        } else {
            assignedTo = currentUser;
        }

        Conversation conversation = Conversation.builder()
                .contact(contact)
                .channel(channel)
                .status(ConversationStatus.OPEN)
                .assignedTo(assignedTo)
                .lastInteraction(LocalDateTime.now())
                .build();

        log.info("Nueva conversación creada: Contacto={}, Canal={}, Asignado a={}",
                contact.getName(), channel, assignedTo.getEmail());

        return conversationRepository.save(conversation);
    }

    /**
     * Obtiene todas las conversaciones del usuario actual
     */
    public List<Conversation> getMyConversations(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return conversationRepository.findByStatus(ConversationStatus.OPEN);
        } else {
            return conversationRepository.findByAssignedToAndStatus(currentUser, ConversationStatus.OPEN);
        }
    }

    /**
     * Obtiene todas las conversaciones del usuario actual ordenadas por última interacción
     */
    public List<Conversation> getMyConversationsOrderedByLastInteraction(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return conversationRepository.findAllByOrderByLastInteractionDesc();
        } else {
            return conversationRepository.findByAssignedToOrderByLastInteraction(currentUser);
        }
    }

    /**
     * Obtiene una conversación por ID con validación de acceso
     */
    public Conversation findByIdAndCheckAccess(Long id, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return conversationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversación", id));
        } else {
            return conversationRepository.findByIdAndAssignedTo(id, currentUser)
                    .orElseThrow(() -> new UnauthorizedAccessException("conversación", id));
        }
    }

    /**
     * Obtiene todas las conversaciones de un contacto específico
     */
    public List<Conversation> getConversationsByContact(Long contactId, User currentUser) {
        Contact contact = contactService.findByIdAndCheckAccess(contactId, currentUser);
        return conversationRepository.findByContact(contact);
    }

    /**
     * Cierra una conversación
     */
    public Conversation closeConversation(Long id, User currentUser) {
        Conversation conversation = findByIdAndCheckAccess(id, currentUser);

        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            throw new BusinessRuleViolationException("La conversación ya está cerrada");
        }

        conversation.setStatus(ConversationStatus.CLOSED);

        log.info("Conversación cerrada: id={}, Contacto={}, Canal={}",
                id, conversation.getContact().getName(), conversation.getChannel());

        return conversationRepository.save(conversation);
    }

    /**
     * Reabre una conversación cerrada
     */
    public Conversation reopenConversation(Long id, User currentUser) {
        Conversation conversation = findByIdAndCheckAccess(id, currentUser);

        if (conversation.getStatus() == ConversationStatus.OPEN) {
            throw new BusinessRuleViolationException("La conversación ya está abierta");
        }

        conversation.setStatus(ConversationStatus.OPEN);
        conversation.setLastInteraction(LocalDateTime.now());

        log.info("Conversación reabierta: id={}, Contacto={}, Canal={}",
                id, conversation.getContact().getName(), conversation.getChannel());

        return conversationRepository.save(conversation);
    }

    /**
     * Reasigna una conversación a otro vendedor (solo Admin)
     */
    public Conversation reassignConversation(Long id, Long newOwnerId, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo el Administrador puede reasignar conversaciones");
        }

        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación", id));

        // ✅ Usar userRepository directamente
        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", newOwnerId));

        if (newOwner.getRole() != Role.SALESPERSON) {
            throw new BusinessRuleViolationException(
                    "Solo se pueden reasignar conversaciones a vendedores. El usuario " + newOwner.getEmail() + " es " + newOwner.getRole()
            );
        }

        conversation.setAssignedTo(newOwner);

        log.info("Conversación reasignada: id={}, Nuevo responsable={}", id, newOwner.getEmail());

        return conversationRepository.save(conversation);
    }

    /**
     * Actualiza la última interacción de una conversación
     */
    public void updateLastInteraction(Conversation conversation) {
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }
}