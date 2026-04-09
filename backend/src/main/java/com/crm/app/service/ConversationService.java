package com.crm.app.service;

import com.crm.app.dto.ConversationDTOs;
import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.UnauthorizedAccessException;
import com.crm.app.mapper.ConversationMapper;
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
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;

    // ==================== MÉTODOS CON DTO ====================

    public List<ConversationDTOs.ConversationResponse> getMyConversationsResponse(User currentUser) {
        List<Conversation> conversations = getMyConversations(currentUser);
        return conversationMapper.toResponseList(conversations);
    }

    public List<ConversationDTOs.ConversationResponse> getAllConversationsResponse() {
        List<Conversation> conversations = getAllConversations();
        return conversationMapper.toResponseList(conversations);
    }

    public List<ConversationDTOs.ConversationResponse> getConversationsByContactResponse(Long contactId, User currentUser) {
        List<Conversation> conversations = getConversationsByContact(contactId, currentUser);
        return conversationMapper.toResponseList(conversations);
    }

    public ConversationDTOs.ConversationResponse getConversationResponse(Long id, User currentUser) {
        Conversation conversation = findByIdAndCheckAccess(id, currentUser);
        return conversationMapper.toResponse(conversation);
    }

    // ==================== MÉTODOS ORIGINALES ====================

    public Conversation getOrCreateConversation(Long contactId, Channel channel, User currentUser) {
        Contact contact = contactService.findByIdAndCheckAccess(contactId, currentUser);
        return conversationRepository.findByContactAndChannel(contact, channel)
                .orElseGet(() -> createNewConversation(contact, channel, currentUser));
    }

    public List<Conversation> getAllConversations() {
        log.info("📋 Admin obteniendo todas las conversaciones");
        return conversationRepository.findAll();
    }

    private Conversation createNewConversation(Contact contact, Channel channel, User currentUser) {
        if (channel == Channel.WHATSAPP && (contact.getPhone() == null || contact.getPhone().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "No se puede crear una conversación por WhatsApp porque el contacto no tiene número de teléfono");
        }
        if (channel == Channel.EMAIL && (contact.getEmail() == null || contact.getEmail().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "No se puede crear una conversación por email porque el contacto no tiene dirección de correo");
        }

        User assignedTo = (currentUser.getRole() == Role.ADMIN) ? contact.getOwner() : currentUser;

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

    public List<Conversation> getMyConversations(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return conversationRepository.findByStatus(ConversationStatus.OPEN);
        } else {
            return conversationRepository.findByAssignedToAndStatus(currentUser, ConversationStatus.OPEN);
        }
    }

    public List<Conversation> getMyConversationsOrderedByLastInteraction(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return conversationRepository.findAllByOrderByLastInteractionDesc();
        } else {
            return conversationRepository.findByAssignedToOrderByLastInteraction(currentUser);
        }
    }

    public Conversation findByIdAndCheckAccess(Long id, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return conversationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversación", id));
        } else {
            return conversationRepository.findByIdAndAssignedTo(id, currentUser)
                    .orElseThrow(() -> new UnauthorizedAccessException("conversación", id));
        }
    }

    public List<Conversation> getConversationsByContact(Long contactId, User currentUser) {
        Contact contact = contactService.findByIdAndCheckAccess(contactId, currentUser);
        return conversationRepository.findByContact(contact);
    }

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

    public Conversation reassignConversation(Long id, Long newOwnerId, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo el Administrador puede reasignar conversaciones");
        }

        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversación", id));

        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", newOwnerId));

        if (newOwner.getRole() != Role.SALESPERSON) {
            throw new BusinessRuleViolationException("Solo se pueden reasignar conversaciones a vendedores");
        }

        conversation.setAssignedTo(newOwner);
        log.info("Conversación reasignada: id={}, Nuevo responsable={}", id, newOwner.getEmail());
        return conversationRepository.save(conversation);
    }

    public void updateLastInteraction(Conversation conversation) {
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    // ==================== MÉTODO PARA @PreAuthorize ====================

    public boolean isOwner(Long conversationId, User currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;
        return conversationRepository.findById(conversationId)
                .map(conv -> conv.getAssignedTo().getId().equals(currentUser.getId()))
                .orElse(false);
    }
}