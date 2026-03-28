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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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

        // Validar contenido
        if (content.body() == null || content.body().trim().isEmpty()) {
            throw new RuntimeException("El contenido del mensaje no puede estar vacío");
        }

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

        // Validar que el contacto tenga la información necesaria
        Contact contact = conversation.getContact();
        if (request.channel() == Channel.WHATSAPP && (contact.getPhone() == null || contact.getPhone().isEmpty())) {
            throw new RuntimeException("El contacto no tiene número de teléfono para enviar mensaje por WhatsApp");
        }
        if (request.channel() == Channel.EMAIL && (contact.getEmail() == null || contact.getEmail().isEmpty())) {
            throw new RuntimeException("El contacto no tiene dirección de email para enviar mensaje por correo");
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
                log.info("📱 Mensaje WhatsApp enviado: to={}, providerId={}",
                        conversation.getContact().getPhone(), providerId);
            } else {
                providerId = emailService.sendMessage(
                        conversation.getContact().getEmail(),
                        content.body(),
                        conversation.getContact().getName()
                );
                log.info("📧 Mensaje Email enviado: to={}, providerId={}",
                        conversation.getContact().getEmail(), providerId);
            }

            // Actualizar mensaje con providerId
            message.setProviderId(providerId);
            message = messageRepository.save(message);

            // Actualizar última interacción de la conversación
            conversation.setLastInteraction(LocalDateTime.now());
            conversationRepository.save(conversation);

            log.info("✅ Mensaje guardado exitosamente: id={}, providerId={}", message.getId(), providerId);

        } catch (Exception e) {
            log.error("❌ Error al enviar mensaje: {}", e.getMessage(), e);
            message.setDeliveryStatus(DeliveryStatus.FAILED);
            messageRepository.save(message);
            throw new RuntimeException("Error al enviar mensaje: " + e.getMessage(), e);
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
        log.info("📱 Procesando mensaje entrante de WhatsApp: from={}, messageId={}",
                webhook.from(), webhook.messageId());

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
            log.info("🆕 Nuevo contacto creado desde webhook WhatsApp: phone={}", webhook.from());
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
                    log.info("💬 Nueva conversación WhatsApp creada para contacto: id={}", finalContact.getId());
                    return conversationRepository.save(newConv);
                });

        // Verificar si el mensaje ya fue procesado (evitar duplicados)
        if (messageRepository.findByProviderId(webhook.messageId()).isPresent()) {
            log.warn("⚠️ Mensaje duplicado ignorado: messageId={}", webhook.messageId());
            return;
        }

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
        log.info("💾 Mensaje entrante WhatsApp guardado: id={}, conversationId={}", message.getId(), conversation.getId());

        // Actualizar última interacción
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    /**
     * Actualiza el estado de entrega de un mensaje (desde message_echoes)
     */
    public void updateDeliveryStatus(String providerId, DeliveryStatus newStatus) {
        log.info("📬 Actualizando estado de mensaje: providerId={}, newStatus={}", providerId, newStatus);

        Message message = messageRepository.findByProviderId(providerId)
                .orElseThrow(() -> new RuntimeException("Mensaje con providerId no encontrado: " + providerId));

        DeliveryStatus oldStatus = message.getDeliveryStatus();
        message.setDeliveryStatus(newStatus);
        messageRepository.save(message);

        log.info("✅ Estado de mensaje actualizado: id={}, providerId={}, {} -> {}",
                message.getId(), providerId, oldStatus, newStatus);
    }

    /**
     * Maneja mensajes entrantes desde webhooks (Brevo/Email)
     */
    public void handleBrevoInbound(BrevoWebhookDTO.@Valid IncomingEmail webhook) {
        log.info("📧 Procesando mensaje entrante de Email: from={}, subject={}",
                webhook.from(), webhook.subject());

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
            log.info("🆕 Nuevo contacto creado desde webhook Email: email={}", webhook.from());

            // Si el email trae nombre, actualizarlo
            if (webhook.fromName() != null && !webhook.fromName().isEmpty()) {
                // Separar nombre y apellido si es posible
                String[] nameParts = webhook.fromName().split(" ", 2);
                String firstName = nameParts[0];
                String lastName = nameParts.length > 1 ? nameParts[1] : null;

                contact.setName(firstName);
                contact.setLastName(lastName);

                // Crear el DTO con los 5 campos requeridos
                ContactDTOs.ContactBase contactBase = new ContactDTOs.ContactBase(
                        firstName,
                        lastName,
                        webhook.from(),
                        null,  // phone
                        null   // company
                );

                ContactDTOs.CreateContactRequest updateRequest = new ContactDTOs.CreateContactRequest(
                        contactBase,
                        "email_inbound",
                        Channel.EMAIL,
                        admin.getId()
                );

                contact = contactService.updateContact(contact.getId(), updateRequest, admin);
                log.info("✏️ Contacto actualizado con nombre: {} {}", firstName, lastName);
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
                    log.info("💬 Nueva conversación Email creada para contacto: id={}", finalContact.getId());
                    return conversationRepository.save(newConv);
                });

        // Verificar si el mensaje ya fue procesado (evitar duplicados)
        if (messageRepository.findByProviderId(webhook.messageId()).isPresent()) {
            log.warn("⚠️ Mensaje duplicado ignorado: messageId={}", webhook.messageId());
            return;
        }

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
        log.info("💾 Mensaje entrante Email guardado: id={}, conversationId={}", message.getId(), conversation.getId());

        // Actualizar última interacción
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    /**
     * Obtiene el administrador por defecto (primer admin activo)
     */
    private User getDefaultAdmin() {
        List<User> admins = userRepository.findByRoleAndActiveTrue(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new RuntimeException("No hay un administrador en el sistema");
        }
        return admins.getFirst();
    }
}