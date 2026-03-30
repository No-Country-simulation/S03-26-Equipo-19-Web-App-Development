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

    /**
     * Normaliza un número de teléfono al formato que espera WhatsApp.
     *
     * Reglas:
     * - Elimina todo excepto dígitos
     * - Para Argentina: convierte 549XXXXXXXXX -> 54XXXXXXXXXX (elimina el 9)
     *
     * Ejemplos:
     * - +5491122540454 -> 541122540454
     * - 5491122540454 -> 541122540454
     * - 54991122540454 -> 541122540454
     * - 541122540454 -> 541122540454 (se mantiene)
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) return null;

        // Eliminar todo excepto dígitos
        String digitsOnly = phone.replaceAll("[^0-9]", "");

        log.info("📞 Normalizando teléfono: original={}, soloDigitos={}", phone, digitsOnly);

        // Si el número es argentino (código 54)
        if (digitsOnly.startsWith("54")) {
            // Caso: 549XXXXXXXXX (13 dígitos) -> eliminar el 9 después del 54
            if (digitsOnly.length() == 13 && digitsOnly.startsWith("549")) {
                String normalized = "54" + digitsOnly.substring(3);
                log.info("✅ Argentina (13 dígitos con 9): {} -> {}", digitsOnly, normalized);
                return normalized;
            }
            // Caso: 5499XXXXXXXXX (14 dígitos con 9 extra)
            if (digitsOnly.length() == 14 && digitsOnly.startsWith("5499")) {
                String normalized = "54" + digitsOnly.substring(4);
                log.info("✅ Argentina (14 dígitos con 9 extra): {} -> {}", digitsOnly, normalized);
                return normalized;
            }
            // Caso: 54XXXXXXXXXX (12 dígitos, ya correcto)
            if (digitsOnly.length() == 12) {
                log.info("✅ Argentina (formato correcto): {}", digitsOnly);
                return digitsOnly;
            }
        }

        // Para otros países o formatos, devolver solo dígitos
        log.info("✅ Número normalizado (sin cambios específicos): {}", digitsOnly);
        return digitsOnly;
    }

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

        User owner;
        if (currentUser.getRole() == Role.ADMIN && request.ownerId() != null) {
            owner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", request.ownerId()));
            if (owner.getRole() != Role.SALESPERSON) {
                throw new BusinessRuleViolationException(
                        "Asignación de contacto",
                        "Solo se pueden asignar contactos a vendedores. El usuario " + owner.getEmail() + " es " + owner.getRole()
                );
            }
        } else {
            owner = currentUser;
        }

        // ✅ FORZAR normalización del teléfono
        String originalPhone = contactBase.phone();
        String normalizedPhone = normalizePhoneNumber(originalPhone);

        log.info("📞 Creando contacto: teléfono original={}, normalizado={}", originalPhone, normalizedPhone);

        // Validar duplicados con el número normalizado
        if (normalizedPhone != null && contactRepository.existsByPhoneAndOwner(normalizedPhone, owner)) {
            throw new DuplicateResourceException("contacto", "teléfono", normalizedPhone);
        }

        // Validar duplicados por email
        if (contactBase.email() != null && contactRepository.existsByEmailAndOwner(contactBase.email(), owner)) {
            throw new DuplicateResourceException("contacto", "email", contactBase.email());
        }

        Contact contact = Contact.builder()
                .name(contactBase.name())
                .lastName(contactBase.lastName())
                .email(contactBase.email())
                .phone(normalizedPhone)  // ✅ Guardar número normalizado
                .company(contactBase.company())
                .source(request.source() != null ? request.source() : "manual")
                .preferredChannel(request.preferredChannel())
                .funnelStatus(FunnelStatus.NEW_LEAD)
                .owner(owner)
                .build();

        String displayName = buildDisplayName(contact);
        log.info("✅ Contacto guardado: {} - Teléfono en BD: {}", displayName, normalizedPhone);
        return contactRepository.save(contact);
    }

    public Contact findByIdAndCheckAccess(Long id, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return contactRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Contacto", id));
        } else {
            return contactRepository.findByIdAndOwner(id, currentUser)
                    .orElseThrow(() -> new UnauthorizedAccessException("contacto", id));
        }
    }

    public List<Contact> getMyContacts(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return contactRepository.findAll();
        } else {
            return contactRepository.findByOwner(currentUser);
        }
    }

    public Contact updateContact(Long id, ContactDTOs.CreateContactRequest request, User currentUser) {
        Contact contact = findByIdAndCheckAccess(id, currentUser);
        ContactDTOs.ContactBase contactBase = request.contact();

        // Validar duplicados si se actualiza email
        if (contactBase.email() != null && !contactBase.email().equals(contact.getEmail())) {
            if (contactRepository.existsByEmailAndOwner(contactBase.email(), contact.getOwner())) {
                throw new DuplicateResourceException("contacto", "email", contactBase.email());
            }
            contact.setEmail(contactBase.email());
        }

        // ✅ Normalizar y validar duplicados si se actualiza teléfono
        String originalPhone = contactBase.phone();
        String normalizedPhone = normalizePhoneNumber(originalPhone);

        if (normalizedPhone != null && !normalizedPhone.equals(contact.getPhone())) {
            if (contactRepository.existsByPhoneAndOwner(normalizedPhone, contact.getOwner())) {
                throw new DuplicateResourceException("contacto", "teléfono", normalizedPhone);
            }
            contact.setPhone(normalizedPhone);
            log.info("📞 Teléfono actualizado: original={}, normalizado={}", originalPhone, normalizedPhone);
        }

        if (contactBase.name() != null) contact.setName(contactBase.name());
        if (contactBase.lastName() != null) contact.setLastName(contactBase.lastName());
        if (contactBase.company() != null) contact.setCompany(contactBase.company());
        if (request.source() != null) contact.setSource(request.source());
        if (request.preferredChannel() != null) contact.setPreferredChannel(request.preferredChannel());

        String displayName = buildDisplayName(contact);
        log.info("Contacto actualizado: id={}, name={}", id, displayName);
        return contactRepository.save(contact);
    }

    public Contact updateFunnelStatus(Long id, FunnelStatus newStatus, User currentUser) {
        Contact contact = findByIdAndCheckAccess(id, currentUser);
        FunnelStatus oldStatus = contact.getFunnelStatus();
        contact.setFunnelStatus(newStatus);

        log.info("Contacto {} cambió estado: {} -> {}", id, oldStatus, newStatus);
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

        String source;
        String cleanIdentifier = identifier;

        if (channel == Channel.WHATSAPP) {
            source = "whatsapp_inbound";
            // ✅ FORZAR normalización del número del webhook
            cleanIdentifier = normalizePhoneNumber(identifier);
            log.info("📱 Webhook WhatsApp: original={}, normalizado={}", identifier, cleanIdentifier);
        } else {
            source = "email_inbound";
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

        log.info("✅ Contacto creado desde webhook: {} - Teléfono en BD: {}", identifier, cleanIdentifier);

        return contactRepository.save(contact);
    }

    /**
     * Busca contacto por teléfono o email según el canal
     */
    public Contact findByExternalId(String externalId, Channel channel) {
        if (channel == Channel.WHATSAPP) {
            // ✅ Normalizar el número antes de buscar
            String normalizedPhone = normalizePhoneNumber(externalId);
            log.info("🔍 Buscando contacto por teléfono: original={}, normalizado={}", externalId, normalizedPhone);
            return contactRepository.findByPhone(normalizedPhone).orElse(null);
        } else {
            return contactRepository.findByEmail(externalId).orElse(null);
        }
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
        } else if (contact.getName() != null) {
            return contact.getName();
        } else if (contact.getLastName() != null) {
            return contact.getLastName();
        } else if (contact.getEmail() != null) {
            return contact.getEmail();
        } else if (contact.getPhone() != null) {
            return contact.getPhone();
        }
        return "Sin identificar";
    }
}