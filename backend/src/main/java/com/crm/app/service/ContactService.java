package com.crm.app.service;

import com.crm.app.dto.ContactDTOs;
import com.crm.app.exception.*;
import com.crm.app.mapper.ContactMapper;
import com.crm.app.model.Contact;
import com.crm.app.model.Conversation;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.FunnelStatus;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.ContactRepository;
import com.crm.app.repository.ConversationRepository;
import com.crm.app.repository.UserRepository;
import com.crm.app.util.PhoneNumberNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final PhoneNumberNormalizer phoneNormalizer;
    private final ContactMapper contactMapper;
    private final ConversationRepository conversationRepository;

    // ==================== CREATE MANUAL ====================

    public ContactDTOs.ContactSummaryResponse createContact(ContactDTOs.CreateContactRequest request, User currentUser) {
        ContactDTOs.ContactBase contactBase = request.contact();

        if (!contactBase.hasAtLeastOneIdentifier()) {
            throw new BusinessRuleViolationException(
                    "Creación de contacto",
                    "El contacto debe tener al menos nombre, apellido, email o teléfono"
            );
        }

        if (currentUser.getRole() == Role.SALESPERSON && request.ownerId() != null) {
            throw new UnauthorizedAccessException("Los vendedores no pueden asignar contactos a otros usuarios");
        }

        User owner;
        if (currentUser.getRole() == Role.ADMIN && request.ownerId() != null && request.ownerId() > 0) {
            owner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.ownerId()));
            if (owner.getRole() != Role.SALESPERSON) {
                throw new BusinessRuleViolationException("Solo se pueden asignar contactos a vendedores");
            }
            log.info("📌 Admin asignando contacto al vendedor: {}", owner.getEmail());
        } else {
            owner = currentUser;
        }

        String normalizedPhone = null;
        if (contactBase.phone() != null) {
            normalizedPhone = phoneNormalizer.normalize(contactBase.phone());
            log.info("📞 Teléfono normalizado: {} -> {}", contactBase.phone(), normalizedPhone);
        }

        if (normalizedPhone != null && contactRepository.existsByPhoneAndOwner(normalizedPhone, owner)) {
            throw new DuplicateResourceException("contacto", "teléfono", normalizedPhone);
        }

        if (contactBase.email() != null && contactRepository.existsByEmailAndOwner(contactBase.email(), owner)) {
            throw new DuplicateResourceException("contacto", "email", contactBase.email());
        }

        Contact contact = Contact.builder()
                .name(contactBase.name())
                .lastName(contactBase.lastName())
                .email(contactBase.email())
                .phone(normalizedPhone)
                .company(contactBase.company())
                .preferredChannel(request.preferredChannel())
                .funnelStatus(FunnelStatus.NEW_LEAD)
                .owner(owner)
                .build();

        Contact saved = contactRepository.save(contact);
        log.info("✅ Contacto creado: ID={}, asignado a {}", saved.getId(), owner.getEmail());

        return contactMapper.toSummaryResponse(saved);
    }

    // ==================== CREATE DESDE WEBHOOK ====================

    public Contact createContactFromWebhook(String identifier, Channel channel, User defaultOwner) {
        if (defaultOwner == null) {
            throw new BusinessRuleViolationException(
                    "Creación de contacto desde webhook",
                    "No hay un administrador disponible"
            );
        }

        String cleanIdentifier = identifier;
        if (channel == Channel.WHATSAPP) {
            cleanIdentifier = phoneNormalizer.normalize(identifier);
            log.info("📱 Webhook WhatsApp: normalizado {} -> {}", identifier, cleanIdentifier);
        }

        Contact contact = Contact.builder()
                .name(null)
                .lastName(null)
                .email(channel == Channel.EMAIL ? identifier : null)
                .phone(channel == Channel.WHATSAPP ? cleanIdentifier : null)
                .company(null)
                .preferredChannel(channel)
                .funnelStatus(FunnelStatus.NEW_LEAD)
                .owner(defaultOwner)
                .build();

        Contact saved = contactRepository.save(contact);
        log.info("✅ Contacto creado desde webhook: ID={}, Canal={}, asignado a ADMIN", saved.getId(), channel);
        return saved;
    }

    // ==================== UPDATE ====================

    public ContactDTOs.ContactDetailResponse updateContact(Long id, ContactDTOs.CreateContactRequest request, User currentUser) {
        Contact contact = findByIdAndCheckAccess(id, currentUser);
        ContactDTOs.ContactBase contactBase = request.contact();

        if (currentUser.getRole() == Role.SALESPERSON && request.ownerId() != null
                && !request.ownerId().equals(contact.getOwner().getId())) {
            throw new UnauthorizedAccessException("Los vendedores no pueden reasignar contactos");
        }

        if (currentUser.getRole() == Role.ADMIN && request.ownerId() != null
                && !request.ownerId().equals(contact.getOwner().getId())) {
            User newOwner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.ownerId()));
            if (newOwner.getRole() != Role.SALESPERSON) {
                throw new BusinessRuleViolationException("Solo se pueden asignar contactos a vendedores");
            }
            contact.setOwner(newOwner);
            log.info("📌 Admin reasignó contacto {} a {}", id, newOwner.getEmail());
        }

        if (contactBase.email() != null && !contactBase.email().equals(contact.getEmail())) {
            if (contactRepository.existsByEmailAndOwner(contactBase.email(), contact.getOwner())) {
                throw new DuplicateResourceException("contacto", "email", contactBase.email());
            }
            contact.setEmail(contactBase.email());
        }

        if (contactBase.phone() != null) {
            String normalizedPhone = phoneNormalizer.normalize(contactBase.phone());
            if (!normalizedPhone.equals(contact.getPhone())) {
                if (contactRepository.existsByPhoneAndOwner(normalizedPhone, contact.getOwner())) {
                    throw new DuplicateResourceException("contacto", "teléfono", normalizedPhone);
                }
                contact.setPhone(normalizedPhone);
            }
        }

        if (contactBase.name() != null) contact.setName(contactBase.name());
        if (contactBase.lastName() != null) contact.setLastName(contactBase.lastName());
        if (contactBase.company() != null) contact.setCompany(contactBase.company());
        if (request.preferredChannel() != null) contact.setPreferredChannel(request.preferredChannel());

        Contact updated = contactRepository.save(contact);
        log.info("✅ Contacto actualizado: ID={}", updated.getId());

        return contactMapper.toDetailResponse(updated);
    }

    // ==================== GET ====================

    public ContactDTOs.ContactDetailResponse getContactDetailResponse(Long id, User currentUser) {
        Contact contact = findByIdAndCheckAccess(id, currentUser);
        return contactMapper.toDetailResponse(contact);
    }

    public List<ContactDTOs.ContactDetailResponse> getMyContactsDetailResponse(User currentUser) {
        List<Contact> contacts = getMyContacts(currentUser);
        return contactMapper.toDetailResponseList(contacts);
    }

    // ==================== MÉTODOS INTERNOS ====================

    public Contact findByIdAndCheckAccess(Long id, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return contactRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Contacto", id));
        }
        return contactRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new UnauthorizedAccessException("contacto", id));
    }

    public List<Contact> getMyContacts(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return contactRepository.findAll();
        }
        return contactRepository.findByOwner(currentUser);
    }

    public boolean isOwner(Long contactId, User currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;
        return contactRepository.findById(contactId)
                .map(contact -> contact.getOwner().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    public Contact updateFunnelStatus(Long id, FunnelStatus newStatus, User currentUser) {
        Contact contact = findByIdAndCheckAccess(id, currentUser);
        contact.setFunnelStatus(newStatus);
        log.info("🔄 Contacto {} cambió estado: {}", id, newStatus);
        return contactRepository.save(contact);
    }

    public Contact findByExternalId(String externalId, Channel channel) {
        if (channel == Channel.WHATSAPP) {
            try {
                String normalizedPhone = phoneNormalizer.normalize(externalId);
                return contactRepository.findByPhone(normalizedPhone).orElse(null);
            } catch (InvalidPhoneNumberException e) {
                log.warn("⚠️ Número inválido: {}", e.getMessage());
                return contactRepository.findByPhone(externalId).orElse(null);
            }
        }
        return contactRepository.findByEmail(externalId).orElse(null);
    }

    public User getDefaultAdmin() {
        List<User> admins = userRepository.findByRoleAndActiveTrue(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new BusinessRuleViolationException("No hay un administrador activo en el sistema");
        }
        return admins.getFirst();
    }

    public Contact reassignContact(Long id, Long newOwnerId, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccessException("Solo el Administrador puede reasignar contactos");
        }

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contacto", id));

        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", newOwnerId));

        if (newOwner.getRole() != Role.SALESPERSON) {
            throw new BusinessRuleViolationException("Solo se pueden asignar contactos a vendedores");
        }

        log.info("📌 Admin {} reasignando contacto {} de {} a {}",
                currentUser.getEmail(), id, contact.getOwner().getEmail(), newOwner.getEmail());

        contact.setOwner(newOwner);
        return contactRepository.save(contact);
    }

    // En ContactService.java - Agrega estos métodos

    // ==================== DASHBOARD DE CONTACTOS CON MÉTRICAS ====================

    public ContactDTOs.ContactDashboardListResponse getContactDashboard(User currentUser) {
        // 1. Obtener contactos del usuario
        List<Contact> contacts;
        if (currentUser.getRole() == Role.ADMIN) {
            contacts = contactRepository.findAllWithConversations();
        } else {
            contacts = contactRepository.findByOwnerWithConversations(currentUser);
        }

        // 2. Obtener estadísticas de mensajes no leídos por contacto
        List<Object[]> unreadStats = contactRepository.countUnreadMessagesByContact(currentUser);
        Map<Long, Long> unreadByContactId = new HashMap<>();
        for (Object[] stat : unreadStats) {
            Long contactId = ((Number) stat[0]).longValue();
            Long count = ((Number) stat[1]).longValue();
            unreadByContactId.put(contactId, count);
        }

        // 3. Obtener conversaciones para cada contacto
        Map<Long, List<Conversation>> conversationsByContactId = new HashMap<>();
        for (Contact contact : contacts) {
            List<Conversation> conversations = conversationRepository.findByContact(contact);
            conversationsByContactId.put(contact.getId(), conversations);
        }

        // 4. Construir respuesta para cada contacto
        List<ContactDTOs.ContactDashboardResponse> contactResponses = new ArrayList<>();
        for (Contact contact : contacts) {
            List<Conversation> conversations = conversationsByContactId.getOrDefault(contact.getId(), List.of());

            // Calcular no leídos por contacto
            Long totalUnread = unreadByContactId.getOrDefault(contact.getId(), 0L);

            // Mapear conversaciones
            List<ContactDTOs.ConversationBriefInfo> conversationInfos = conversations.stream()
                    .map(this::toConversationBriefInfo)
                    .collect(Collectors.toList());

            contactResponses.add(new ContactDTOs.ContactDashboardResponse(
                    contact.getId(),
                    contact.getName(),
                    contact.getLastName(),
                    contact.getEmail(),
                    contact.getPhone(),
                    contact.getCompany(),
                    contact.getFunnelStatus(),
                    contact.getPreferredChannel(),
                    contactMapper.toOwnerInfo(contact.getOwner()),
                    contact.getTags().stream().map(contactMapper::toTagInfo).collect(Collectors.toList()),
                    contact.getCreatedAt(),
                    conversationInfos,
                    totalUnread
            ));
        }

        // 5. Calcular métricas globales
        long totalContacts = contacts.size();
        long totalUnreadMessages = contactRepository.countTotalUnreadMessages(currentUser);

        // No leídos por canal
        List<Object[]> channelStats = contactRepository.countUnreadMessagesByChannel(currentUser);
        long whatsappUnread = 0;
        long emailUnread = 0;
        for (Object[] stat : channelStats) {
            Channel channel = (Channel) stat[0];
            long count = ((Number) stat[1]).longValue();
            if (channel == Channel.WHATSAPP) {
                whatsappUnread = count;
            } else if (channel == Channel.EMAIL) {
                emailUnread = count;
            }
        }

        ContactDTOs.DashboardMetrics metrics = new ContactDTOs.DashboardMetrics(
                totalContacts,
                totalUnreadMessages,
                new ContactDTOs.UnreadByChannel(whatsappUnread, emailUnread)
        );

        // 6. Ordenar contactos: los que tienen NO LEÍDOS primero, luego por fecha de creación
        contactResponses.sort((a, b) -> {
            // Primero por no leídos (descendente)
            int unreadCompare = Long.compare(b.totalUnreadCount(), a.totalUnreadCount());
            if (unreadCompare != 0) return unreadCompare;
            // Luego por fecha de creación (más reciente primero)
            return b.createdAt().compareTo(a.createdAt());
        });

        return new ContactDTOs.ContactDashboardListResponse(metrics, contactResponses);
    }

    private ContactDTOs.ConversationBriefInfo toConversationBriefInfo(Conversation conv) {
        return new ContactDTOs.ConversationBriefInfo(
                conv.getId(),
                conv.getChannel(),
                conv.getStatus(),
                conv.getLastInteraction(),
                null // El unreadCount por conversación se puede calcular si es necesario
        );
    }
}
