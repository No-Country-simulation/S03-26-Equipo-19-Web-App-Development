package com.crm.app.service;

import com.crm.app.dto.BrevoWebhookDTO;
import com.crm.app.dto.ContactDTOs;
import com.crm.app.dto.MessageDTOs;
import com.crm.app.dto.WhatsAppWebhookDTO;
import com.crm.app.model.Contact;
import com.crm.app.model.Conversation;
import com.crm.app.model.Message;
import com.crm.app.model.User;
import com.crm.app.model.enums.*;
import com.crm.app.repository.ConversationRepository;
import com.crm.app.repository.MessageRepository;
import com.crm.app.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ContactService contactService;
    private final ConversationService conversationService;
    private final WhatsAppService whatsAppService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public Message sendMessage(MessageDTOs.SendMessageRequest request, User currentUser) {
        MessageDTOs.MessageContent content = request.content();

        // Obtener o crear conversación
        Conversation conversation = conversationService.getOrCreateConversation(
                request.contactId(),
                request.channel(),
                currentUser
        );

        // Validar que la conversación esté abierta
        if (conversation.getStatus() != ConversationStatus.OPEN) {
            throw new RuntimeException("La conversación está cerrada");
        }

        // Crear mensaje en BD primero
        Message message = Message.builder()
                .conversation(conversation)
                .direction(MessageDirection.OUTBOUND)
                .body(content.body())
                .deliveryStatus(DeliveryStatus.SENT)
                .sender(currentUser)
                .sentAt(LocalDateTime.now())
                .build();

        message = messageRepository.save(message);

        try {
            // Enviar por canal externo
            String providerId;
            if (request.channel() == Channel.WHATSAPP) {
                providerId = whatsAppService.sendMessage(
                        conversation.getContact().getPhone(),
                        content.body()
                );
            } else {
                /*providerId = emailService.sendMessage(
                        conversation.getContact().getEmail(),
                        content.body(),
                        conversation.getContact().getName()*//*
                );*/
            }

          /*  message.setProviderId(providerId);*/
            message = messageRepository.save(message);

            // Actualizar última interacción
            conversation.setLastInteraction(LocalDateTime.now());
            conversationRepository.save(conversation);

        } catch (Exception e) {
            message.setDeliveryStatus(DeliveryStatus.FAILED);
            messageRepository.save(message);
            throw new RuntimeException("Error al enviar mensaje: " + e.getMessage());
        }

        return message;
    }

    public List<Message> getConversationHistory(Long conversationId, User currentUser) {
        Conversation conversation = conversationService.findByIdAndCheckAccess(conversationId, currentUser);
        return messageRepository.findByConversationOrderBySentAtAsc(conversation);
    }

    /**
     * Maneja mensajes entrantes desde webhooks (WhatsApp)
     */
    public void handleWhatsAppInbound(WhatsAppWebhookDTO.@Valid IncomingMessage webhook) {
        User admin = getDefaultAdmin();

        // Buscar contacto por teléfono
        Contact contact = contactService.findByExternalId(webhook.from(), Channel.WHATSAPP);

        if (contact == null) {
            // Crear contacto automáticamente con datos mínimos
            contact = contactService.createContactFromWebhook(
                    webhook.from(),
                    Channel.WHATSAPP,
                    admin
            );
        }

        // Obtener o crear conversación
        Contact finalContact = contact;
        Conversation conversation = conversationRepository.findByContactAndChannel(contact, Channel.WHATSAPP)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .contact(finalContact)
                            .channel(Channel.WHATSAPP)
                            .status(ConversationStatus.OPEN)
                            .assignedTo(finalContact.getOwner())
                            .lastInteraction(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(newConv);
                });

        // Crear mensaje entrante
        Message message = Message.builder()
                .conversation(conversation)
                .direction(MessageDirection.INBOUND)
                .body(webhook.messageBody())
                .deliveryStatus(DeliveryStatus.DELIVERED)
                .providerId(webhook.messageId())
                .sentAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        // Actualizar última interacción
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    /**
     * Maneja mensajes entrantes desde webhooks (Brevo/Email)
     */
    public void handleBrevoInbound(BrevoWebhookDTO.@Valid IncomingEmail webhook) {
        User admin = getDefaultAdmin();

        // Buscar contacto por email
        Contact contact = contactService.findByExternalId(webhook.from(), Channel.EMAIL);

        if (contact == null) {
            // Crear contacto automáticamente con datos mínimos
            contact = contactService.createContactFromWebhook(
                    webhook.from(),
                    Channel.EMAIL,
                    admin
            );
            // Si el email trae nombre, actualizarlo
            if (webhook.fromName() != null && !webhook.fromName().isEmpty()) {
                contact.setName(webhook.fromName());
                contact = contactService.updateContact(contact.getId(),
                        new ContactDTOs.CreateContactRequest(
                                new ContactDTOs.ContactBase(
                                        webhook.fromName(),
                                        webhook.from(),
                                        null,
                                        null
                                ),
                                "email_inbound",
                                Channel.EMAIL,
                                admin.getId()
                        ), admin);
            }
        }

        // Obtener o crear conversación
        Contact finalContact = contact;
        Conversation conversation = conversationRepository.findByContactAndChannel(contact, Channel.EMAIL)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .contact(finalContact)
                            .channel(Channel.EMAIL)
                            .status(ConversationStatus.OPEN)
                            .assignedTo(finalContact.getOwner())
                            .lastInteraction(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(newConv);
                });

        // Crear mensaje entrante
        String body = webhook.text() != null ? webhook.text() : webhook.subject();
        Message message = Message.builder()
                .conversation(conversation)
                .direction(MessageDirection.INBOUND)
                .body(body)
                .deliveryStatus(DeliveryStatus.DELIVERED)
                .providerId(webhook.messageId())
                .sentAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        // Actualizar última interacción
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    private User getDefaultAdmin() {
        List<User> admins = userRepository.findByRoleAndActiveTrue(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new RuntimeException("No hay un administrador en el sistema");
        }
        return admins.getFirst();
    }
}