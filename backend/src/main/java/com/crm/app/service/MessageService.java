package com.crm.app.service;

import com.crm.app.dto.BrevoWebhookDTO;
import com.crm.app.dto.ContactDTOs;
import com.crm.app.dto.MessageDTOs;
import com.crm.app.dto.WhatsAppWebhookDTO;
import com.crm.app.exception.*;
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
    private final TemplateService templateService;  // ✅ Agregado

    // ==================== MÉTODOS PÚBLICOS ====================

    /**
     * Envía un mensaje desde el CRM a un contacto (WhatsApp o Email)
     * Soporta mensaje libre o uso de plantillas
     */
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
            // ✅ Usar plantilla
            // ✅ Correcto
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
            // ✅ Mensaje libre
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

            // 8. Actualizar última interacción
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

    /**
     * Obtiene el historial de mensajes de una conversación
     */
    public List<Message> getConversationHistory(Long conversationId, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedAccessException("Debes iniciar sesión para ver el historial");
        }

        User currentUser = getUserByEmail(userEmail);
        log.info("📜 Usuario {} consultando historial de conversación ID={}",
                currentUser.getEmail(), conversationId);

        Conversation conversation = conversationService.findByIdAndCheckAccess(conversationId, currentUser);
        List<Message> history = messageRepository.findByConversationOrderBySentAtAsc(conversation);

        log.info("📜 Historial recuperado: {} mensajes", history.size());
        return history;
    }

    // ==================== MANEJO DE WEBHOOKS ====================

    public void handleWhatsAppInbound(WhatsAppWebhookDTO.@Valid IncomingMessage webhook) {
        log.info("📱 Procesando mensaje entrante de WhatsApp: from={}, messageId={}",
                webhook.from(), webhook.messageId());

        try {
            User admin = getDefaultAdmin();

            // 1. Buscar contacto existente
            Contact contact = contactService.findByExternalId(webhook.from(), Channel.WHATSAPP);
            boolean isNewContact = (contact == null);

            if (isNewContact) {
                contact = contactService.createContactFromWebhook(webhook.from(), Channel.WHATSAPP, admin);
                log.info("🆕 Nuevo contacto creado desde WhatsApp: phone={}", webhook.from());
            }

            // 2. Hacer una copia final de contact para usar en la lambda
            final Contact finalContact = contact;

            // 3. Buscar conversación EXISTENTE antes de crear una nueva
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

            // 4. Verificar mensaje duplicado por providerId
            if (messageRepository.findByProviderId(webhook.messageId()).isPresent()) {
                log.warn("⚠️ Mensaje WhatsApp duplicado ignorado: messageId={}", webhook.messageId());
                return;
            }

            // 5. Guardar mensaje entrante
            Message inboundMessage = Message.builder()
                    .conversation(conversation)
                    .direction(MessageDirection.INBOUND)
                    .body(webhook.messageBody())
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .providerId(webhook.messageId())
                    .sentAt(LocalDateTime.now())
                    .build();
            messageRepository.save(inboundMessage);

            // 6. Actualizar última interacción
            conversation.setLastInteraction(LocalDateTime.now());
            conversationRepository.save(conversation);

            log.info("💾 Mensaje WhatsApp entrante guardado: id={}, conversationId={}",
                    inboundMessage.getId(), conversation.getId());

            // 7. Enviar respuesta automática SOLO si es nuevo contacto
            if (isNewContact) {
                sendWelcomeAutoReply(contact, conversation, admin);
            }

        } catch (Exception e) {
            log.error("❌ Error procesando mensaje WhatsApp entrante: {}", e.getMessage(), e);
        }
    }

    /**
     * Envía respuesta automática de bienvenida usando plantilla
     */
    private void sendWelcomeAutoReply(Contact contact, Conversation conversation, User admin) {
        try {
            // Buscar plantilla de bienvenida para WhatsApp
            Optional<Template> welcomeTemplateOpt = templateRepository.findByNameAndChannel(
                    "Bienvenida automática - WhatsApp", Channel.WHATSAPP);

            if (welcomeTemplateOpt.isEmpty()) {
                log.warn("⚠️ No se encontró plantilla de bienvenida. No se enviará respuesta automática.");
                return;
            }

            Template welcomeTemplate = welcomeTemplateOpt.get();

            // Preparar variables
            String contactName = contact.getName();
            if (contactName == null || contactName.isBlank()) {
                contactName = "cliente";
            }

            Map<String, String> variables = Map.of("name", contactName);

            // Renderizar mensaje
            String welcomeMessage = templateService.renderTemplate(welcomeTemplate, variables);

            // Enviar mensaje automático
            String providerId = whatsAppService.sendMessage(contact.getPhone(), welcomeMessage);

            // Guardar mensaje saliente automático
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

    public void handleBrevoInbound(BrevoWebhookDTO.@Valid IncomingEmail webhook) {
        log.info("📧 Procesando mensaje entrante de Email: from={}, subject={}, messageId={}",
                webhook.from(), webhook.subject(), webhook.messageId());

        try {
            User admin = getDefaultAdmin();

            // 1. Buscar contacto existente
            Contact contact = contactService.findByExternalId(webhook.from(), Channel.EMAIL);
            boolean isNewContact = (contact == null);

            if (contact == null) {
                contact = contactService.createContactFromWebhook(webhook.from(), Channel.EMAIL, admin);
                log.info("🆕 Nuevo contacto creado desde Email: email={}", webhook.from());

                if (webhook.fromName() != null && !webhook.fromName().isEmpty()) {
                    updateContactName(contact, webhook.fromName(), admin);
                }
            }

            // 2. Hacer una copia final de contact para usar en la lambda
            final Contact finalContact = contact;

            // 3. Obtener o crear conversación
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

            String brevoMessageId = webhook.messageId();

            if (messageRepository.findByProviderId(brevoMessageId).isPresent()) {
                log.warn("⚠️ Mensaje Email duplicado ignorado: messageId={}", brevoMessageId);
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
                    .providerId(brevoMessageId)
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

    private void sendEmailWelcomeAutoReply(Contact contact, Conversation conversation, User admin) {
        try {
            Optional<Template> welcomeTemplateOpt = templateRepository.findByNameAndChannel(
                    "Email de bienvenida", Channel.EMAIL);

            if (welcomeTemplateOpt.isEmpty()) {
                log.warn("⚠️ No se encontró plantilla de bienvenida para Email.");
                return;
            }

            Template welcomeTemplate = welcomeTemplateOpt.get();

            // Preparar variables
            Map<String, String> variables = Map.of(
                    "name", contact.getName() != null ? contact.getName() : "cliente",
                    "company", contact.getCompany() != null ? contact.getCompany() : "tu empresa",
                    "salesperson", "nuestro equipo"
            );

            // Renderizar mensaje
            String welcomeMessage = templateService.renderTemplate(welcomeTemplate, variables);

            // Enviar email automático
            String providerId = emailService.sendMessage(contact.getEmail(), welcomeMessage, contact.getName());

            // Guardar mensaje saliente automático
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

    public void updateDeliveryStatus(String providerId, DeliveryStatus newStatus) {
        log.info("📬 Actualizando estado de mensaje: providerId={}, newStatus={}", providerId, newStatus);

        try {
            Message message = messageRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mensaje con providerId: " + providerId));

            DeliveryStatus oldStatus = message.getDeliveryStatus();
            message.setDeliveryStatus(newStatus);
            messageRepository.save(message);

            log.info("✅ Estado de mensaje actualizado: id={}, {} -> {}",
                    message.getId(), oldStatus, newStatus);

        } catch (ResourceNotFoundException e) {
            log.warn("⚠️ No se encontró mensaje con providerId: {}", providerId);
        } catch (Exception e) {
            log.error("❌ Error actualizando estado de mensaje: {}", e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    private void validateContactHasChannelInfo(Contact contact, Channel channel) {
        if (channel == Channel.WHATSAPP && (contact.getPhone() == null || contact.getPhone().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "Envío de mensaje",
                    "El contacto no tiene número de teléfono para enviar mensaje por WhatsApp"
            );
        }
        if (channel == Channel.EMAIL && (contact.getEmail() == null || contact.getEmail().isEmpty())) {
            throw new BusinessRuleViolationException(
                    "Envío de mensaje",
                    "El contacto no tiene dirección de email para enviar mensaje por correo"
            );
        }
    }

    private Message createOutboundMessage(Conversation conversation, String body, User sender, Template template) {
        Message message = Message.builder()
                .conversation(conversation)
                .direction(MessageDirection.OUTBOUND)
                .body(body)
                .deliveryStatus(DeliveryStatus.SENT)
                .sender(sender)
                .template(template)  // ✅ Guardar referencia a la plantilla usada
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
            log.info("📧 Enviando Email a: {}", contact.getEmail());
            return emailService.sendMessage(contact.getEmail(), body, contact.getName());
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

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email: " + email));
    }

    private User getDefaultAdmin() {
        List<User> admins = userRepository.findByRoleAndActiveTrue(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Configuración del sistema",
                    "No hay un administrador activo en el sistema. Contacta al soporte técnico."
            );
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
                .map(conversation -> conversation.getAssignedTo().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    /**
     * Obtiene TODOS los mensajes de un contacto (WhatsApp + Email combinados)
     * Útil para ver el timeline completo de comunicación con un contacto
     */
    public List<Message> getContactConversationHistory(Long contactId, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedAccessException("Debes iniciar sesión para ver el historial");
        }

        User currentUser = getUserByEmail(userEmail);

        // Verificar acceso al contacto
        Contact contact = contactService.findByIdAndCheckAccess(contactId, currentUser);

        // Obtener todas las conversaciones del contacto (WhatsApp y Email)
        List<Conversation> conversations = conversationRepository.findByContact(contact);

        if (conversations.isEmpty()) {
            return List.of();
        }

        // Obtener todos los mensajes de todas las conversaciones y combinarlos
        return messageRepository.findByConversationInOrderBySentAtAsc(conversations);
    }
}