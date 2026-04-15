package com.crm.app.service;

import com.crm.app.dto.BrevoWebhookDTO;
import com.crm.app.dto.ContactDTOs;
import com.crm.app.dto.MessageDTOs;
import com.crm.app.dto.WhatsAppWebhookDTO;
import com.crm.app.exception.*;
import com.crm.app.mapper.MessageMapper;
import com.crm.app.model.Contact;
import com.crm.app.model.Conversation;
import com.crm.app.model.Message;
import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.*;
import com.crm.app.repository.ConversationRepository;
import com.crm.app.repository.MessageRepository;
import com.crm.app.repository.TemplateRepository;
import com.crm.app.repository.UserRepository;
import com.crm.app.service.api.EmailService;
import com.crm.app.service.api.WhatsAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final TemplateRepository templateRepository;
    private final TemplateService templateService;
    private final MessageMapper messageMapper;

    // ==================== MÉTODOS CON DTO ====================

    public MessageDTOs.MessageResponse sendMessageResponse(MessageDTOs.SendMessageRequest request, String userEmail) {
        Message message = sendMessage(request, userEmail);
        return messageMapper.toResponse(message);
    }

    public List<MessageDTOs.MessageResponse> getConversationHistoryResponse(Long conversationId, String userEmail) {
        List<Message> messages = getConversationHistory(conversationId, userEmail);
        return messageMapper.toResponseList(messages);
    }

    public List<MessageDTOs.MessageResponse> getContactConversationHistoryResponse(Long contactId, String userEmail) {
        List<Message> messages = getContactConversationHistory(contactId, userEmail);
        return messageMapper.toResponseList(messages);
    }

    // ==================== MÉTODOS ORIGINALES ====================

    public Message sendMessage(MessageDTOs.SendMessageRequest request, String userEmail) {
        // 1. Validar usuario autenticado
        if (userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedAccessException("Debes iniciar sesión para enviar mensajes");
        }

        User currentUser = getUserByEmail(userEmail);
        log.info("📨 Usuario {} enviando mensaje a contacto ID={} por canal {}",
                currentUser.getEmail(), request.contactId(), request.channel());

        // 2. Obtener o crear conversación
        Conversation conversation;
        try {
            conversation = conversationService.getOrCreateConversation(
                    request.contactId(),
                    request.channel(),
                    currentUser
            );
        } catch (ResourceNotFoundException e) {
            log.error("❌ Contacto no encontrado: ID={}", request.contactId());
            throw new ResourceNotFoundException("Contacto", request.contactId());
        } catch (UnauthorizedAccessException e) {
            log.error("❌ Usuario {} no tiene acceso al contacto ID={}", currentUser.getEmail(), request.contactId());
            throw e;
        }

        // 3. Validar que la conversación esté abierta
        if (conversation.getStatus() != ConversationStatus.OPEN) {
            log.warn("⚠️ Intento de enviar mensaje a conversación cerrada: ID={}", conversation.getId());
            throw new ConversationClosedException(conversation.getId());
        }

        // 4. Validar que el contacto tenga la información necesaria para el canal
        Contact contact = conversation.getContact();
        validateContactHasChannelInfo(contact, request.channel());

        // 5. Construir el cuerpo del mensaje (libre o desde plantilla)
        String finalBody;
        Template usedTemplate = null;

        if (request.templateId() != null) {
            usedTemplate = templateService.getTemplate(request.templateId(), currentUser);
            Map<String, String> variables = request.variables();

            if (variables == null || variables.isEmpty()) {
                throw new BusinessRuleViolationException(
                        "Envío con plantilla",
                        "Debes proporcionar los valores para las variables de la plantilla"
                );
            }

            finalBody = templateService.renderTemplate(usedTemplate, variables);
            log.info("📝 Mensaje renderizado desde plantilla: {}", usedTemplate.getName());
        } else {
            MessageDTOs.MessageContent content = request.content();
            if (content == null || content.body() == null || content.body().trim().isEmpty()) {
                throw new BusinessRuleViolationException(
                        "Envío de mensaje",
                        "El contenido del mensaje no puede estar vacío"
                );
            }
            finalBody = content.body();
        }

        // 6. Crear mensaje en BD (estado SENT)
        Message message = createOutboundMessage(conversation, finalBody, currentUser, usedTemplate);

        // 7. Enviar por canal externo y actualizar
        try {
            String providerId = sendViaExternalService(conversation, finalBody, request.channel());
            message.setProviderId(providerId);
            message = messageRepository.save(message);

            conversation.setLastInteraction(LocalDateTime.now());
            conversationRepository.save(conversation);

            log.info("✅ Mensaje enviado exitosamente: id={}, providerId={}, canal={}",
                    message.getId(), providerId, request.channel());
            return message;

        } catch (TokenExpiredException e) {
            log.error("🔑 Token expirado para {}: {}", request.channel(), e.getMessage());
            message.setDeliveryStatus(DeliveryStatus.FAILED);
            messageRepository.save(message);
            throw e;

        } catch (ExternalServiceException e) {
            log.error("❌ Error en servicio externo ({}) al enviar mensaje: {}",
                    request.channel(), e.getMessage());
            message.setDeliveryStatus(DeliveryStatus.FAILED);
            messageRepository.save(message);
            throw e;

        } catch (Exception e) {
            log.error("💥 Error inesperado al enviar mensaje: {}", e.getMessage(), e);
            message.setDeliveryStatus(DeliveryStatus.FAILED);
            messageRepository.save(message);
            throw new ExternalServiceException(
                    request.channel() == Channel.WHATSAPP ? "WhatsApp Cloud API" : "Brevo",
                    "Error al enviar mensaje: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Message> getConversationHistory(Long conversationId, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedAccessException("Debes iniciar sesión para ver el historial");
        }

        User currentUser = getUserByEmail(userEmail);
        log.info("📜 Usuario {} consultando historial de conversación ID={}",
                currentUser.getEmail(), conversationId);

        Conversation conversation = conversationService.findByIdAndCheckAccess(conversationId, currentUser);
        return messageRepository.findByConversationOrderBySentAtAsc(conversation);
    }

    public List<Message> getContactConversationHistory(Long contactId, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedAccessException("Debes iniciar sesión para ver el historial");
        }

        User currentUser = getUserByEmail(userEmail);
        Contact contact = contactService.findByIdAndCheckAccess(contactId, currentUser);

        List<Conversation> conversations = conversationRepository.findByContact(contact);
        if (conversations.isEmpty()) {
            return List.of();
        }

        return messageRepository.findByConversationInOrderBySentAtAsc(conversations);
    }

    // ==================== WEBHOOKS ====================

    public void handleWhatsAppInbound(WhatsAppWebhookDTO.@Valid IncomingMessage webhook) {
        log.info("📱 Procesando mensaje entrante de WhatsApp: from={}, messageId={}",
                webhook.from(), webhook.messageId());

        try {
            User admin = getDefaultAdmin();
            Contact existingContact = contactService.findByExternalId(webhook.from(), Channel.WHATSAPP);
            boolean isNewContact = (existingContact == null);

            Contact contact = getOrCreateContactFromWhatsApp(webhook.from(), admin);
            Conversation conversation = getOrCreateConversation(contact, Channel.WHATSAPP);

            if (messageRepository.findByProviderId(webhook.messageId()).isPresent()) {
                log.warn("⚠️ Mensaje WhatsApp duplicado ignorado: messageId={}", webhook.messageId());
                return;
            }

            Message inboundMessage = Message.builder()
                    .conversation(conversation)
                    .direction(MessageDirection.INBOUND)
                    .body(webhook.messageBody())
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .providerId(webhook.messageId())
                    .sentAt(LocalDateTime.now())
                    .build();
            messageRepository.save(inboundMessage);
            updateConversationLastInteraction(conversation);

            log.info("💾 Mensaje WhatsApp entrante guardado: id={}, conversationId={}",
                    inboundMessage.getId(), conversation.getId());

            if (isNewContact) {
                sendWelcomeAutoReply(contact, conversation, admin);
            }

        } catch (Exception e) {
            log.error("❌ Error procesando mensaje WhatsApp entrante: {}", e.getMessage(), e);
        }
    }

    public void handleBrevoInbound(BrevoWebhookDTO.@Valid IncomingEmail webhook) {
        log.info("📧 Procesando mensaje entrante de Email: from={}, subject={}, messageId={}",
                webhook.from(), webhook.subject(), webhook.messageId());

        try {
            User admin = getDefaultAdmin();
            Contact existingContact = contactService.findByExternalId(webhook.from(), Channel.EMAIL);
            boolean isNewContact = (existingContact == null);

            Contact contact = getOrCreateContactFromEmail(webhook, admin);
            Conversation conversation = getOrCreateConversation(contact, Channel.EMAIL);

            if (messageRepository.findByProviderId(webhook.messageId()).isPresent()) {
                log.warn("⚠️ Mensaje Email duplicado ignorado: messageId={}", webhook.messageId());
                return;
            }

            String body = webhook.text() != null ? webhook.text() : webhook.subject();
            if (body.contains("<") && body.contains(">")) {
                body = body.replaceAll("<[^>]*>", "").trim();
            }

            Message inboundMessage = Message.builder()
                    .conversation(conversation)
                    .direction(MessageDirection.INBOUND)
                    .body(body)
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .providerId(webhook.messageId())
                    .sentAt(LocalDateTime.now())
                    .build();

            messageRepository.save(inboundMessage);
            updateConversationLastInteraction(conversation);

            log.info("💾 Mensaje Email entrante guardado: id={}, conversationId={}",
                    inboundMessage.getId(), conversation.getId());

            if (isNewContact) {
                sendEmailWelcomeAutoReply(contact, conversation, admin);
            }

        } catch (Exception e) {
            log.error("❌ Error procesando mensaje Email entrante: {}", e.getMessage(), e);
        }
    }

    // En MessageService.java - Agrega más logs

    public void updateDeliveryStatus(String providerId, DeliveryStatus newStatus) {
        log.info("📬 Buscando mensaje con providerId: '{}' para actualizar a {}", providerId, newStatus);

        if (providerId == null || providerId.isBlank()) {
            log.warn("⚠️ providerId es null o vacío, no se puede actualizar");
            return;
        }

        try {
            Optional<Message> messageOpt = messageRepository.findByProviderId(providerId);

            if (messageOpt.isEmpty()) {
                log.warn("⚠️ NO se encontró mensaje con providerId: '{}'", providerId);

                // Opcional: Buscar todos los mensajes para debug
                List<Message> allMessages = messageRepository.findAll();
                log.debug("📬 Total de mensajes en BD: {}", allMessages.size());
                for (Message m : allMessages) {
                    log.debug("📬 Mensaje en BD: id={}, providerId='{}'", m.getId(), m.getProviderId());
                }
                return;
            }

            Message message = messageOpt.get();
            DeliveryStatus oldStatus = message.getDeliveryStatus();

            if (oldStatus == newStatus) {
                log.info("ℹ️ El mensaje ID={} ya estaba en estado {}", message.getId(), newStatus);
                return;
            }

            message.setDeliveryStatus(newStatus);
            messageRepository.save(message);

            log.info("✅ Estado actualizado: mensajeId={}, providerId='{}', {} -> {}",
                    message.getId(), providerId, oldStatus, newStatus);

        } catch (Exception e) {
            log.error("❌ Error actualizando estado de mensaje: {}", e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void validateContactHasChannelInfo(Contact contact, Channel channel) {
        if (channel == Channel.WHATSAPP && (contact.getPhone() == null || contact.getPhone().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "El contacto no tiene número de teléfono para enviar mensaje por WhatsApp");
        }
        if (channel == Channel.EMAIL && (contact.getEmail() == null || contact.getEmail().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "El contacto no tiene dirección de email para enviar mensaje por correo");
        }
    }

    private Message createOutboundMessage(Conversation conversation, String body, User sender, Template template) {
        Message message = Message.builder()
                .conversation(conversation)
                .direction(MessageDirection.OUTBOUND)
                .body(body)
                .deliveryStatus(DeliveryStatus.SENT)
                .sender(sender)
                .template(template)
                .sentAt(LocalDateTime.now())
                .build();
        return messageRepository.save(message);
    }

    private String sendViaExternalService(Conversation conversation, String body, Channel channel) {
        Contact contact = conversation.getContact();

        if (channel == Channel.WHATSAPP) {
            log.info("📱 Enviando WhatsApp a: {}", contact.getPhone());
            return whatsAppService.sendMessage(contact.getPhone(), body);
        } else {
            String recipientName = (contact.getName() != null && !contact.getName().isBlank())
                    ? contact.getName() : "Cliente";
            log.info("📧 Enviando Email a: {}", contact.getEmail());
            return emailService.sendMessage(contact.getEmail(), body, recipientName);
        }
    }

    private Contact getOrCreateContactFromWhatsApp(String phoneNumber, User admin) {
        Contact contact = contactService.findByExternalId(phoneNumber, Channel.WHATSAPP);
        if (contact == null) {
            contact = contactService.createContactFromWebhook(phoneNumber, Channel.WHATSAPP, admin);
            log.info("🆕 Nuevo contacto creado desde WhatsApp: phone={}", phoneNumber);
        }
        return contact;
    }

    private Contact getOrCreateContactFromEmail(BrevoWebhookDTO.IncomingEmail webhook, User admin) {
        Contact contact = contactService.findByExternalId(webhook.from(), Channel.EMAIL);
        if (contact == null) {
            contact = contactService.createContactFromWebhook(webhook.from(), Channel.EMAIL, admin);
            log.info("🆕 Nuevo contacto creado desde Email: email={}", webhook.from());

            if (webhook.fromName() != null && !webhook.fromName().isEmpty()) {
                updateContactName(contact, webhook.fromName(), admin);
            }
        }
        return contact;
    }

    private void updateContactName(Contact contact, String fullName, User admin) {
        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : null;

        ContactDTOs.ContactBase contactBase = new ContactDTOs.ContactBase(
                firstName, lastName, contact.getEmail(), null, null);

        ContactDTOs.CreateContactRequest updateRequest = new ContactDTOs.CreateContactRequest(
                contactBase, Channel.EMAIL, admin.getId());

        contactService.updateContact(contact.getId(), updateRequest, admin);
        log.info("✏️ Contacto actualizado con nombre: {} {}", firstName, lastName);
    }

    private Conversation getOrCreateConversation(Contact contact, Channel channel) {
        return conversationRepository.findByContactAndChannel(contact, channel)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .contact(contact)
                            .channel(channel)
                            .status(ConversationStatus.OPEN)
                            .assignedTo(contact.getOwner())
                            .lastInteraction(LocalDateTime.now())
                            .build();
                    log.info("💬 Nueva conversación creada: contacto={}, canal={}", contact.getId(), channel);
                    return conversationRepository.save(newConv);
                });
    }

    private void updateConversationLastInteraction(Conversation conversation) {
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    private void sendWelcomeAutoReply(Contact contact, Conversation conversation, User admin) {
        try {
            Optional<Template> welcomeTemplateOpt = templateRepository.findByNameAndChannel(
                    "Bienvenida automática - WhatsApp", Channel.WHATSAPP);

            if (welcomeTemplateOpt.isEmpty()) {
                log.warn("⚠️ No se encontró plantilla de bienvenida.");
                return;
            }

            Template welcomeTemplate = welcomeTemplateOpt.get();
            String contactName = (contact.getName() != null && !contact.getName().isBlank())
                    ? contact.getName() : "";

            String welcomeMessage = templateService.renderTemplate(welcomeTemplate, Map.of("name", contactName));
            String providerId = whatsAppService.sendMessage(contact.getPhone(), welcomeMessage);

            Message autoReply = Message.builder()
                    .conversation(conversation)
                    .direction(MessageDirection.OUTBOUND)
                    .body(welcomeMessage)
                    .deliveryStatus(DeliveryStatus.SENT)
                    .sender(admin)
                    .template(welcomeTemplate)
                    .providerId(providerId)
                    .sentAt(LocalDateTime.now())
                    .build();
            messageRepository.save(autoReply);
            updateConversationLastInteraction(conversation);

            log.info("🤖 Respuesta automática enviada a: {}", contact.getPhone());

        } catch (Exception e) {
            log.error("❌ Error enviando respuesta automática: {}", e.getMessage(), e);
        }
    }

    private void sendEmailWelcomeAutoReply(Contact contact, Conversation conversation, User admin) {
        try {
            Optional<Template> welcomeTemplateOpt = templateRepository.findByNameAndChannel(
                    "Email de bienvenida - Lead", Channel.EMAIL);

            if (welcomeTemplateOpt.isEmpty()) {
                log.warn("⚠️ No se encontró plantilla de bienvenida para Email.");
                return;
            }

            Template welcomeTemplate = welcomeTemplateOpt.get();
            String contactName = (contact.getName() != null && !contact.getName().isBlank())
                    ? contact.getName() : "cliente";
            String companyName = (contact.getCompany() != null && !contact.getCompany().isBlank())
                    ? contact.getCompany() : "nuestra empresa";

            String welcomeMessage = templateService.renderTemplate(welcomeTemplate, Map.of(
                    "name", contactName,
                    "company", companyName,
                    "salesperson", "nuestro equipo"
            ));

            String providerId = emailService.sendMessage(contact.getEmail(), welcomeMessage, contactName);

            Message autoReply = Message.builder()
                    .conversation(conversation)
                    .direction(MessageDirection.OUTBOUND)
                    .body(welcomeMessage)
                    .deliveryStatus(DeliveryStatus.SENT)
                    .sender(admin)
                    .template(welcomeTemplate)
                    .providerId(providerId)
                    .sentAt(LocalDateTime.now())
                    .build();
            messageRepository.save(autoReply);
            updateConversationLastInteraction(conversation);

            log.info("🤖 Respuesta automática por Email enviada a: {}", contact.getEmail());

        } catch (Exception e) {
            log.error("❌ Error enviando respuesta automática por Email: {}", e.getMessage(), e);
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email: " + email));
    }

    private User getDefaultAdmin() {
        List<User> admins = userRepository.findByRoleAndActiveTrue(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new BusinessRuleViolationException("No hay un administrador activo en el sistema");
        }
        return admins.getFirst();
    }

    // ==================== MÉTODOS PARA @PreAuthorize ====================

    public boolean canSendToContact(Long contactId, User currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;
        return contactService.isOwner(contactId, currentUser);
    }

    public boolean isConversationOwner(Long conversationId, User currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;
        return conversationRepository.findById(conversationId)
                .map(conv -> conv.getAssignedTo().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    // En MessageService.java - Agrega este método

    // ==================== MARCAR MENSAJE COMO LEÍDO ====================

    public MessageDTOs.MarkAsReadResponse markAsRead(Long messageId, String userEmail) {
        // 1. Validar usuario autenticado
        if (userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedAccessException("Debes iniciar sesión para marcar mensajes como leídos");
        }

        User currentUser = getUserByEmail(userEmail);
        log.info("📖 Usuario {} marcando mensaje ID={} como leído", currentUser.getEmail(), messageId);

        // 2. Buscar el mensaje
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje", messageId));

        // 3. Validar que el usuario tenga acceso a la conversación
        Conversation conversation = message.getConversation();
        if (!conversation.getAssignedTo().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("No tienes acceso a este mensaje");
        }

        // 4. Validar que sea un mensaje INBOUND (del cliente al CRM)
        if (message.getDirection() != MessageDirection.INBOUND) {
            log.warn("⚠️ Intento de marcar mensaje OUTBOUND como leído: id={}", messageId);
            throw new BusinessRuleViolationException(
                    "Solo los mensajes recibidos (INBOUND) pueden marcarse como leídos por el vendedor"
            );
        }

        // 5. Validar que no esté ya leído
        if (message.getDeliveryStatus() == DeliveryStatus.READ) {
            log.info("ℹ️ El mensaje ID={} ya estaba marcado como leído", messageId);
            return new MessageDTOs.MarkAsReadResponse(
                    message.getId(),
                    message.getDeliveryStatus(),
                    null
            );
        }

        // 6. Actualizar estado a READ
        DeliveryStatus oldStatus = message.getDeliveryStatus();
        message.setDeliveryStatus(DeliveryStatus.READ);
        messageRepository.save(message);

        log.info("✅ Mensaje ID={} marcado como leído: {} -> READ", messageId, oldStatus);

        return new MessageDTOs.MarkAsReadResponse(
                message.getId(),
                DeliveryStatus.READ,
                LocalDateTime.now()
        );
    }

    // ==================== MARCAR TODOS LOS MENSAJES DE UNA CONVERSACIÓN COMO LEÍDOS ====================

    public int markAllAsRead(Long conversationId, String userEmail) {
        // 1. Validar usuario autenticado
        if (userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedAccessException("Debes iniciar sesión para marcar mensajes como leídos");
        }

        User currentUser = getUserByEmail(userEmail);
        log.info("📖 Usuario {} marcando todos los mensajes de conversación ID={} como leídos",
                currentUser.getEmail(), conversationId);

        // 2. Validar acceso a la conversación
        Conversation conversation = conversationService.findByIdAndCheckAccess(conversationId, currentUser);

        // 3. Actualizar todos los mensajes INBOUND no leídos
        int updatedCount = messageRepository.updateInboundMessagesToRead(conversationId);

        log.info("✅ {} mensajes marcados como leídos en conversación ID={}", updatedCount, conversationId);

        return updatedCount;
    }
}
