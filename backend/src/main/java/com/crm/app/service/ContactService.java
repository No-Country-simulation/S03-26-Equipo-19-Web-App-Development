package com.crm.app.service;

import com.crm.app.dto.ContactDTOs;
import com.crm.app.exception.*;
import com.crm.app.model.Contact;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.FunnelStatus;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.ContactRepository;
import com.crm.app.repository.UserRepository;
import com.crm.app.util.PhoneNumberNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final PhoneNumberNormalizer phoneNormalizer;

    /**
     * Crea un nuevo contacto
     */
    public Contact createContact(ContactDTOs.CreateContactRequest request, User currentUser) {
        ContactDTOs.ContactBase contactBase = request.contact();

        // Validar que tenga al menos un identificador
        if (!contactBase.hasAtLeastOneIdentifier()) {
            throw new BusinessRuleViolationException(
                    "Creación de contacto",
                    "El contacto debe tener al menos nombre, apellido, email o teléfono"
            );
        }

        // Si es Vendedor, no puede especificar ownerId
        if (currentUser.getRole() == Role.SALESPERSON && request.ownerId() != null) {
            throw new UnauthorizedAccessException("Los vendedores no pueden asignar contactos a otros usuarios");
        }

        // Resolver el owner del contacto
        User owner = resolveOwner(request, currentUser);

        // Normalizar el teléfono
        String normalizedPhone = null;
        if (contactBase.phone() != null) {
            try {
                normalizedPhone = phoneNormalizer.normalize(contactBase.phone());
                log.info("📞 Creando contacto: teléfono original={}, normalizado={}", contactBase.phone(), normalizedPhone);
            } catch (InvalidPhoneNumberException e) {
                log.warn("📞 Número inválido al crear contacto: {}", e.getMessage());
                throw e;
            }
        }

        // Validar duplicados con el número normalizado
        if (normalizedPhone != null && contactRepository.existsByPhoneAndOwner(normalizedPhone, owner)) {
            throw new DuplicateResourceException("contacto", "teléfono", normalizedPhone);
        }

        // Validar duplicados por email
        if (contactBase.email() != null && contactRepository.existsByEmailAndOwner(contactBase.email(), owner)) {
            throw new DuplicateResourceException("contacto", "email", contactBase.email());
        }

        // Construir el contacto
        Contact contact = Contact.builder()
                .name(contactBase.name())
                .lastName(contactBase.lastName())
                .email(contactBase.email())
                .phone(normalizedPhone)
                .company(contactBase.company())
                .source(request.source() != null ? request.source() : "manual")
                .preferredChannel(request.preferredChannel())
                .funnelStatus(FunnelStatus.NEW_LEAD)
                .owner(owner)
                .build();

        try {
            Contact saved = contactRepository.save(contact);
            String displayName = buildDisplayName(saved);
            log.info("✅ Contacto creado: ID={}, Nombre={}, Teléfono={}", saved.getId(), displayName, saved.getPhone());
            return saved;
        } catch (Exception e) {
            log.error("❌ Error al guardar contacto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar el contacto en la base de datos", e);
        }
    }

    /**
     * Resuelve el owner del contacto (Admin puede asignar, Vendedor se asigna a sí mismo)
     */
    private User resolveOwner(ContactDTOs.CreateContactRequest request, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN && request.ownerId() != null) {
            User owner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.ownerId()));
            if (owner.getRole() != Role.SALESPERSON) {
                throw new BusinessRuleViolationException(
                        "Asignación de contacto",
                        "Solo se pueden asignar contactos a vendedores. El usuario " + owner.getEmail() + " es " + owner.getRole()
                );
            }
            log.info("📌 Admin asignando contacto al vendedor: {}", owner.getEmail());
            return owner;
        }
        return currentUser;
    }

    /**
     * Busca un contacto por ID con validación de acceso según el rol
     */
    public Contact findByIdAndCheckAccess(Long id, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return contactRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Contacto", id));
        }
        return contactRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new UnauthorizedAccessException("contacto", id));
    }

    /**
     * Obtiene todos los contactos del usuario actual (Admin ve todos, Vendedor solo los suyos)
     */
    public List<Contact> getMyContacts(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            log.info("📋 Admin obteniendo todos los contactos");
            return contactRepository.findAll();
        }
        log.info("📋 Vendedor {} obteniendo sus contactos", currentUser.getEmail());
        return contactRepository.findByOwner(currentUser);
    }

    /**
     * Actualiza un contacto existente
     */
    public Contact updateContact(Long id, ContactDTOs.CreateContactRequest request, User currentUser) {
        Contact contact = findByIdAndCheckAccess(id, currentUser);
        ContactDTOs.ContactBase contactBase = request.contact();

        // Validar duplicados si se actualiza email
        if (contactBase.email() != null && !contactBase.email().equals(contact.getEmail())) {
            if (contactRepository.existsByEmailAndOwner(contactBase.email(), contact.getOwner())) {
                throw new DuplicateResourceException("contacto", "email", contactBase.email());
            }
            contact.setEmail(contactBase.email());
            log.info("📧 Email actualizado: {}", contactBase.email());
        }

        // Normalizar y validar duplicados si se actualiza teléfono
        if (contactBase.phone() != null) {
            String normalizedPhone = phoneNormalizer.normalize(contactBase.phone());
            if (!normalizedPhone.equals(contact.getPhone())) {
                if (contactRepository.existsByPhoneAndOwner(normalizedPhone, contact.getOwner())) {
                    throw new DuplicateResourceException("contacto", "teléfono", normalizedPhone);
                }
                contact.setPhone(normalizedPhone);
                log.info("📞 Teléfono actualizado: {}", normalizedPhone);
            }
        }

        // Actualizar otros campos
        if (contactBase.name() != null) contact.setName(contactBase.name());
        if (contactBase.lastName() != null) contact.setLastName(contactBase.lastName());
        if (contactBase.company() != null) contact.setCompany(contactBase.company());
        if (request.source() != null) contact.setSource(request.source());
        if (request.preferredChannel() != null) contact.setPreferredChannel(request.preferredChannel());

        try {
            Contact updated = contactRepository.save(contact);
            String displayName = buildDisplayName(updated);
            log.info("✅ Contacto actualizado: ID={}, Nombre={}", updated.getId(), displayName);
            return updated;
        } catch (Exception e) {
            log.error("❌ Error al actualizar contacto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar el contacto", e);
        }
    }

    /**
     * Verifica si el usuario es dueño del contacto o es ADMIN
     */
    public boolean isOwner(Long contactId, User currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole() == Role.ADMIN) return true;

        return contactRepository.findById(contactId)
                .map(contact -> contact.getOwner().getId().equals(currentUser.getId()))
                .orElse(false);
    }

    /**
     * Actualiza el estado del funnel de un contacto
     */
    public Contact updateFunnelStatus(Long id, FunnelStatus newStatus, User currentUser) {
        Contact contact = findByIdAndCheckAccess(id, currentUser);
        FunnelStatus oldStatus = contact.getFunnelStatus();
        contact.setFunnelStatus(newStatus);

        log.info("🔄 Contacto {} cambió estado: {} -> {}", id, oldStatus, newStatus);
        return contactRepository.save(contact);
    }

    /**
     * Crea un contacto automáticamente desde un webhook (WhatsApp o Email)
     */
    public Contact createContactFromWebhook(String identifier, Channel channel, User defaultOwner) {
        if (defaultOwner == null) {
            throw new BusinessRuleViolationException(
                    "Creación de contacto desde webhook",
                    "No hay un administrador disponible para asignar el contacto"
            );
        }

        String source = channel == Channel.WHATSAPP ? "whatsapp_inbound" : "email_inbound";
        String cleanIdentifier = identifier;

        if (channel == Channel.WHATSAPP) {
            try {
                cleanIdentifier = phoneNormalizer.normalize(identifier);
                log.info("📱 Webhook WhatsApp: original={}, normalizado={}", identifier, cleanIdentifier);
            } catch (InvalidPhoneNumberException e) {
                log.warn("⚠️ Número inválido en webhook: {}", e.getMessage());
                // Continuamos con el identificador original, pero se guardará como está
            }
        }

        Contact contact = Contact.builder()
                .name(null)
                .lastName(null)
                .email(channel == Channel.EMAIL ? identifier : null)
                .phone(channel == Channel.WHATSAPP ? cleanIdentifier : null)
                .company(null)
                .source(source)
                .preferredChannel(channel)
                .funnelStatus(FunnelStatus.NEW_LEAD)
                .owner(defaultOwner)
                .build();

        try {
            Contact saved = contactRepository.save(contact);
            log.info("✅ Contacto creado desde webhook: ID={}, Canal={}, Identificador={}",
                    saved.getId(), channel, identifier);
            return saved;
        } catch (Exception e) {
            log.error("❌ Error al guardar contacto desde webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar el contacto desde webhook", e);
        }
    }

    /**
     * Busca contacto por teléfono o email según el canal
     */
    public Contact findByExternalId(String externalId, Channel channel) {
        if (channel == Channel.WHATSAPP) {
            try {
                String normalizedPhone = phoneNormalizer.normalize(externalId);
                log.debug("🔍 Buscando contacto por teléfono: original={}, normalizado={}", externalId, normalizedPhone);
                return contactRepository.findByPhone(normalizedPhone).orElse(null);
            } catch (InvalidPhoneNumberException e) {
                log.warn("⚠️ Número inválido al buscar: {}", e.getMessage());
                return contactRepository.findByPhone(externalId).orElse(null);
            }
        }
        log.debug("🔍 Buscando contacto por email: {}", externalId);
        return contactRepository.findByEmail(externalId).orElse(null);
    }

    /**
     * Obtiene el administrador por defecto (primer admin activo)
     */
    public User getDefaultAdmin() {
        List<User> admins = userRepository.findByRoleAndActiveTrue(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Configuración del sistema",
                    "No hay un administrador activo en el sistema"
            );
        }
        return admins.getFirst();
    }

    /**
     * Construye un nombre para mostrar (nombre + apellido o email o teléfono)
     */
    private String buildDisplayName(Contact contact) {
        if (contact.getName() != null && contact.getLastName() != null) {
            return contact.getName() + " " + contact.getLastName();
        }
        if (contact.getName() != null) {
            return contact.getName();
        }
        if (contact.getLastName() != null) {
            return contact.getLastName();
        }
        if (contact.getEmail() != null) {
            return contact.getEmail();
        }
        if (contact.getPhone() != null) {
            return contact.getPhone();
        }
        return "Sin identificar";
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
            throw new BusinessRuleViolationException(
                    "Reasignación de contacto",
                    "Solo se pueden asignar contactos a vendedores. El usuario " + newOwner.getEmail() + " es " + newOwner.getRole()
            );
        }

        log.info("📌 Admin {} reasignando contacto ID={} de {} a {}",
                currentUser.getEmail(), id, contact.getOwner().getEmail(), newOwner.getEmail());

        contact.setOwner(newOwner);

        return contactRepository.save(contact);
    }
}