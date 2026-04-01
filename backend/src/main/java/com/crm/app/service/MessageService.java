package com.crm.app.service;

import com.crm.app.dto.BrevoWebhookDTO;
import com.crm.app.dto.ContactDTOs;
import com.crm.app.dto.MessageDTOs;
import com.crm.app.dto.WhatsAppWebhookDTO;
import com.crm.app.exception.*;
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

    // ==================== MÉTODOS PÚBLICOS ====================

    /**
     * Envía un mensaje desde el CRM a un contacto (WhatsApp o Email)
     *
     * @param request Datos del mensaje a enviar (contactId, canal, contenido)
     * @param userEmail Email del usuario autenticado
     * @return Mensaje guardado con su providerId
     * @throws ResourceNotFoundException Si el contacto no existe
     * @throws UnauthorizedAccessException Si el usuario no tiene permisos
     * @throws BusinessRuleViolationException Si hay violación de reglas de negocio
     * @throws ExternalServiceException Si falla el servicio externo (Brevo/WhatsApp)
     */
    public Message sendMessage(MessageDTOs.SendMessageRequest request, String userEmail) {
        // 1. Validar usuario autenticado
        if (userEmail == null || userEmail.isBlank()) {
            throw new UnauthorizedAccessException("Debes iniciar sesión para enviar mensajes");
        }

        User currentUser = getUserByEmail(userEmail);
        log.info("📨 Usuario {} enviando mensaje a contacto ID={} por canal {}",
                currentUser.getEmail(), request.contactId(), request.channel());

        MessageDTOs.MessageContent content = request.content();

        // 2. Validar contenido del mensaje
        if (content.body() == null || content.body().trim().isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Envío de mensaje",
                    "El contenido del mensaje no puede estar vacío"
            );
        }

        // 3. Obtener o crear conversación
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

        // 4. Validar que la conversación esté abierta
        if (conversation.getStatus() != ConversationStatus.OPEN) {
            log.warn("⚠️ Intento de enviar mensaje a conversación cerrada: ID={}", conversation.getId());
            throw new ConversationClosedException(conversation.getId());
        }

        // 5. Validar que el contacto tenga la información necesaria para el canal
        Contact contact = conversation.getContact();
        validateContactHasChannelInfo(contact, request.channel());

        // 6. Crear mensaje en BD (estado SENT)
        Message message = createOutboundMessage(conversation, content.body(), currentUser);

        // 7. Enviar por canal externo y actualizar
        try {
            String providerId = sendViaExternalService(conversation, content.body(), request.channel());
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
     *
     * @param conversationId ID de la conversación
     * @param userEmail Email del usuario autenticado
     * @return Lista de mensajes ordenados cronológicamente
     * @throws ResourceNotFoundException Si la conversación no existe
     * @throws UnauthorizedAccessException Si el usuario no tiene acceso
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

    /**
     * Maneja mensajes entrantes desde webhooks de WhatsApp
     *
     * @param webhook Datos del mensaje entrante
     */
    public void handleWhatsAppInbound(WhatsAppWebhookDTO.@Valid IncomingMessage webhook) {
        log.info("📱 Procesando mensaje entrante de WhatsApp: from={}, messageId={}",
                webhook.from(), webhook.messageId());

        try {
            User admin = getDefaultAdmin();
            Contact contact = getOrCreateContactFromWhatsApp(webhook.from(), admin);
            Conversation conversation = getOrCreateConversation(contact, Channel.WHATSAPP);

            // Evitar duplicados
            if (messageRepository.findByProviderId(webhook.messageId()).isPresent()) {
                log.warn("⚠️ Mensaje WhatsApp duplicado ignorado: messageId={}", webhook.messageId());
                return;
            }

            // Crear y guardar mensaje entrante
            Message message = Message.builder()
                    .conversation(conversation)
                    .direction(MessageDirection.INBOUND)
                    .body(webhook.messageBody())
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .providerId(webhook.messageId())
                    .sentAt(LocalDateTime.now())
                    .build();

            messageRepository.save(message);
            updateConversationLastInteraction(conversation);

            log.info("💾 Mensaje WhatsApp entrante guardado: id={}, conversationId={}",
                    message.getId(), conversation.getId());

        } catch (Exception e) {
            log.error("❌ Error procesando mensaje WhatsApp entrante: {}", e.getMessage(), e);
            // No relanzamos para no interrumpir el webhook
        }
    }

    /**
     * Maneja mensajes entrantes desde webhooks de Brevo (Email)
     *
     * @param webhook Datos del email entrante
     */
    public void handleBrevoInbound(BrevoWebhookDTO.@Valid IncomingEmail webhook) {
        log.info("📧 Procesando mensaje entrante de Email: from={}, subject={}, messageId={}",
                webhook.from(), webhook.subject(), webhook.messageId());

        try {
            User admin = getDefaultAdmin();
            Contact contact = getOrCreateContactFromEmail(webhook, admin);
            Conversation conversation = getOrCreateConversation(contact, Channel.EMAIL);

            // Evitar duplicados
            if (messageRepository.findByProviderId(webhook.messageId()).isPresent()) {
                log.warn("⚠️ Mensaje Email duplicado ignorado: messageId={}", webhook.messageId());
                return;
            }

            // Limpiar contenido HTML si es necesario
            String body = webhook.text() != null ? webhook.text() : webhook.subject();
            if (body.contains("<") && body.contains(">")) {
                body = body.replaceAll("<[^>]*>", "").trim();
            }

            // Crear y guardar mensaje entrante
            Message message = Message.builder()
                    .conversation(conversation)
                    .direction(MessageDirection.INBOUND)
                    .body(body)
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .providerId(webhook.messageId())
                    .sentAt(LocalDateTime.now())
                    .build();

            messageRepository.save(message);
            updateConversationLastInteraction(conversation);

            log.info("💾 Mensaje Email entrante guardado: id={}, conversationId={}",
                    message.getId(), conversation.getId());

        } catch (Exception e) {
            log.error("❌ Error procesando mensaje Email entrante: {}", e.getMessage(), e);
            // No relanzamos para no interrumpir el webhook
        }
    }

    /**
     * Actualiza el estado de entrega de un mensaje (desde message_echoes)
     *
     * @param providerId ID del mensaje en el proveedor externo
     * @param newStatus Nuevo estado de entrega
     */
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

    /**
     * Valida que el contacto tenga la información necesaria para el canal
     */
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

    /**
     * Crea un mensaje saliente en la base de datos
     */
    private Message createOutboundMessage(Conversation conversation, String body, User sender) {
        Message message = Message.builder()
                .conversation(conversation)
                .direction(MessageDirection.OUTBOUND)
                .body(body)
                .deliveryStatus(DeliveryStatus.SENT)
                .sender(sender)
                .sentAt(LocalDateTime.now())
                .build();
        return messageRepository.save(message);
    }

    /**
     * Envía el mensaje a través del servicio externo correspondiente
     */
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

    /**
     * Obtiene o crea un contacto desde webhook de WhatsApp
     */
    private Contact getOrCreateContactFromWhatsApp(String phoneNumber, User admin) {
        Contact contact = contactService.findByExternalId(phoneNumber, Channel.WHATSAPP);

        if (contact == null) {
            contact = contactService.createContactFromWebhook(phoneNumber, Channel.WHATSAPP, admin);
            log.info("🆕 Nuevo contacto creado desde WhatsApp: phone={}", phoneNumber);
        }
        return contact;
    }

    /**
     * Obtiene o crea un contacto desde webhook de Email
     */
    private Contact getOrCreateContactFromEmail(BrevoWebhookDTO.IncomingEmail webhook, User admin) {
        Contact contact = contactService.findByExternalId(webhook.from(), Channel.EMAIL);

        if (contact == null) {
            contact = contactService.createContactFromWebhook(webhook.from(), Channel.EMAIL, admin);
            log.info("🆕 Nuevo contacto creado desde Email: email={}", webhook.from());

            // Actualizar nombre si está disponible
            if (webhook.fromName() != null && !webhook.fromName().isEmpty()) {
                updateContactName(contact, webhook.fromName(), admin);
            }
        }
        return contact;
    }

    /**
     * Actualiza el nombre del contacto
     */
    private void updateContactName(Contact contact, String fullName, User admin) {
        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : null;

        ContactDTOs.ContactBase contactBase = new ContactDTOs.ContactBase(
                firstName, lastName, contact.getEmail(), null, null);

        ContactDTOs.CreateContactRequest updateRequest = new ContactDTOs.CreateContactRequest(
                contactBase, "email_inbound", Channel.EMAIL, admin.getId());

        contactService.updateContact(contact.getId(), updateRequest, admin);
        log.info("✏️ Contacto actualizado con nombre: {} {}", firstName, lastName);
    }

    /**
     * Obtiene o crea una conversación
     */
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

    /**
     * Actualiza la última interacción de una conversación
     */
    private void updateConversationLastInteraction(Conversation conversation) {
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }

    /**
     * Obtiene un usuario por su email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email: " + email));
    }

    /**
     * Obtiene el administrador por defecto (primer admin activo)
     */
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

    /**
     * Verifica si el usuario puede enviar mensajes a un contacto
     *
     * @param contactId ID del contacto
     * @param currentUser Usuario autenticado
     * @return true si puede enviar, false en caso contrario
     */
    public boolean canSendToContact(Long contactId, User currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;

        return contactService.isOwner(contactId, currentUser);
    }

    /**
     * Verifica si el usuario es dueño de la conversación
     *
     * @param conversationId ID de la conversación
     * @param currentUser Usuario autenticado
     * @return true si es dueño o admin, false en caso contrario
     */
    public boolean isConversationOwner(Long conversationId, User currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;

        return conversationRepository.findById(conversationId)
                .map(conversation -> conversation.getAssignedTo().getId().equals(currentUser.getId()))
                .orElse(false);
    }
}