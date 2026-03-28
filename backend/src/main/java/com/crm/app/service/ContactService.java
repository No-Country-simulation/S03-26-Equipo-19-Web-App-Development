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

        // Validar que no exista contacto con mismo email para el mismo owner
        if (contactBase.email() != null && contactRepository.existsByEmailAndOwner(contactBase.email(), owner)) {
            throw new DuplicateResourceException("contacto", "email", contactBase.email());
        }

        // Validar que no exista contacto con mismo teléfono para el mismo owner
        if (contactBase.phone() != null && contactRepository.existsByPhoneAndOwner(contactBase.phone(), owner)) {
            throw new DuplicateResourceException("contacto", "teléfono", contactBase.phone());
        }

        Contact contact = Contact.builder()
                .name(contactBase.name())
                .lastName(contactBase.lastName())
                .email(contactBase.email())
                .phone(contactBase.phone())
                .company(contactBase.company())
                .source(request.source() != null ? request.source() : "manual")
                .preferredChannel(request.preferredChannel())
                .funnelStatus(FunnelStatus.NEW_LEAD)
                .owner(owner)
                .build();

        String displayName = buildDisplayName(contact);
        log.info("Contacto creado: {} - Asignado a: {}", displayName, owner.getEmail());
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

        // Validar duplicados si se actualiza teléfono
        if (contactBase.phone() != null && !contactBase.phone().equals(contact.getPhone())) {
            if (contactRepository.existsByPhoneAndOwner(contactBase.phone(), contact.getOwner())) {
                throw new DuplicateResourceException("contacto", "teléfono", contactBase.phone());
            }
            contact.setPhone(contactBase.phone());
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
     * name, lastName y source pueden ser null inicialmente
     */
    public Contact createContactFromWebhook(String identifier, Channel channel, User defaultOwner) {
        if (defaultOwner == null) {
            throw new BusinessRuleViolationException(
                    "Creación de contacto desde webhook",
                    "No hay un administrador disponible para asignar el contacto"
            );
        }

        String source;
        String tempIdentifier = identifier;

        if (channel == Channel.WHATSAPP) {
            source = "whatsapp_inbound";
        } else {
            source = "email_inbound";
        }

        Contact contact = Contact.builder()
                .name(null)           // ✅ Puede ser null inicialmente
                .lastName(null)       // ✅ Puede ser null inicialmente
                .email(channel == Channel.EMAIL ? identifier : null)
                .phone(channel == Channel.WHATSAPP ? identifier : null)
                .company(null)
                .source(source)       // ✅ source puede ser null? Según tu requerimiento sí, pero aquí lo seteamos
                .preferredChannel(channel)
                .funnelStatus(FunnelStatus.NEW_LEAD)
                .owner(defaultOwner)
                .build();

        log.info("Contacto creado automáticamente desde webhook: {} - Canal: {}",
                identifier, channel);

        return contactRepository.save(contact);
    }

    /**
     * Busca contacto por teléfono o email según el canal
     */
    public Contact findByExternalId(String externalId, Channel channel) {
        if (channel == Channel.WHATSAPP) {
            return contactRepository.findByPhone(externalId).orElse(null);
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