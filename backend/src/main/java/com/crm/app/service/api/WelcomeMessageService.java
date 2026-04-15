package com.crm.app.service.api;

import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.model.Contact;
import com.crm.app.model.Conversation;
import com.crm.app.model.Message;
import com.crm.app.model.Template;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.ConversationStatus;
import com.crm.app.model.enums.DeliveryStatus;
import com.crm.app.model.enums.MessageDirection;
import com.crm.app.repository.ConversationRepository;
import com.crm.app.repository.MessageRepository;
import com.crm.app.repository.TemplateRepository;
import com.crm.app.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeMessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final TemplateRepository templateRepository;
    private final TemplateService templateService;
    private final WhatsAppService whatsAppService;
    private final EmailService emailService;

    private static final String WHATSAPP_WELCOME_TEMPLATE = "WhatsApp - Bienvenida y propósito";
    private static final String EMAIL_WELCOME_TEMPLATE = "Email - Bienvenida y propósito";

    /**
     * Inicializa conversación y envía mensaje de bienvenida para un contacto nuevo
     * Esta transacción es independiente de la principal para no afectar la creación del contacto
     */
    @Transactional
    public void initializeContactChannels(Contact contact, User admin) {
        if (contact == null || admin == null) {
            log.warn("⚠️ No se puede inicializar: contact o admin es null");
            return;
        }

        // 1. Crear conversación y enviar bienvenida por WhatsApp si tiene teléfono
        if (contact.getPhone() != null && !contact.getPhone().isBlank()) {
            createConversationAndSendWelcome(contact, Channel.WHATSAPP, admin);
        }

        // 2. Crear conversación y enviar bienvenida por Email si tiene email
        if (contact.getEmail() != null && !contact.getEmail().isBlank()) {
            createConversationAndSendWelcome(contact, Channel.EMAIL, admin);
        }
    }

    /**
     * Crea una conversación y envía mensaje de bienvenida para un canal específico
     */
    private void createConversationAndSendWelcome(Contact contact, Channel channel, User admin) {
        try {
            // 1. Crear la conversación
            Conversation conversation = createConversation(contact, channel);

            // 2. Enviar mensaje de bienvenida
            sendWelcomeMessage(contact, conversation, channel, admin);

            log.info("✅ Conversación y bienvenida enviada para contacto {} por canal {}",
                    contact.getId(), channel);

        } catch (Exception e) {
            log.error("❌ Error inicializando canal {} para contacto {}: {}",
                    channel, contact.getId(), e.getMessage(), e);
        }
    }

    /**
     * Crea una conversación para el contacto y canal específico
     */
    private Conversation createConversation(Contact contact, Channel channel) {
        return conversationRepository.findByContactAndChannel(contact, channel)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .contact(contact)
                            .channel(channel)
                            .status(ConversationStatus.OPEN)
                            .assignedTo(contact.getOwner())
                            .lastInteraction(LocalDateTime.now())
                            .build();
                    log.info("💬 Conversación creada: contacto={}, canal={}", contact.getId(), channel);
                    return conversationRepository.save(newConv);
                });
    }

    /**
     * Envía mensaje de bienvenida usando la plantilla correspondiente
     */
    private void sendWelcomeMessage(Contact contact, Conversation conversation, Channel channel, User admin) {
        String templateName = channel == Channel.WHATSAPP ? WHATSAPP_WELCOME_TEMPLATE : EMAIL_WELCOME_TEMPLATE;

        Optional<Template> templateOpt = templateRepository.findByNameAndChannel(templateName, channel);

        if (templateOpt.isEmpty()) {
            log.warn("⚠️ Plantilla de bienvenida no encontrada: {} para canal {}", templateName, channel);
            return;
        }

        Template template = templateOpt.get();
        String contactName = (contact.getName() != null && !contact.getName().isBlank())
                ? contact.getName() : "cliente";

        String welcomeMessage;
        String providerId;

        try {
            Map<String, String> variables = new HashMap<>();
            variables.put("name", contactName);
            variables.put("salesperson", admin.getName());

            // ⚠️ IMPORTANTE: Verificar si la plantilla necesita la variable company
            // Si la plantilla tiene {{company}}, agregarla, si no, no la agregues
            if (template.getBody().contains("{{company}}")) {
                String companyName = (contact.getCompany() != null && !contact.getCompany().isBlank())
                        ? contact.getCompany() : "tu empresa";
                variables.put("company", companyName);
            }

            welcomeMessage = templateService.renderTemplate(template, variables);

            if (channel == Channel.WHATSAPP) {
                providerId = whatsAppService.sendMessage(contact.getPhone(), welcomeMessage);
                log.info("📱 Mensaje de bienvenida WhatsApp enviado a: {}", contact.getPhone());
            } else {
                providerId = emailService.sendMessage(contact.getEmail(), welcomeMessage, contactName);
                log.info("📧 Mensaje de bienvenida Email enviado a: {}", contact.getEmail());
            }

            Message welcomeMessageEntity = Message.builder()
                    .conversation(conversation)
                    .direction(MessageDirection.OUTBOUND)
                    .body(welcomeMessage)
                    .deliveryStatus(DeliveryStatus.SENT)
                    .sender(admin)
                    .template(template)
                    .providerId(providerId)
                    .sentAt(LocalDateTime.now())
                    .build();

            messageRepository.save(welcomeMessageEntity);
            updateConversationLastInteraction(conversation);

            log.info("🤖 Mensaje de bienvenida persistido: conversationId={}", conversation.getId());

        } catch (BusinessRuleViolationException e) {
            // Capturar específicamente el error de variable faltante y NO relanzar
            log.error("❌ Error de plantilla al enviar mensaje de bienvenida por {}: {}", channel, e.getMessage());
            // No relanzar la excepción para no marcar la transacción como rollback-only
        } catch (Exception e) {
            log.error("❌ Error enviando mensaje de bienvenida por {}: {}", channel, e.getMessage(), e);
            // No relanzar la excepción
        }
    }

    private void updateConversationLastInteraction(Conversation conversation) {
        conversation.setLastInteraction(LocalDateTime.now());
        conversationRepository.save(conversation);
    }
}