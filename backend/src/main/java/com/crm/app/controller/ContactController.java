package com.crm.app.controller;

import com.crm.app.dto.ContactDTOs;
import com.crm.app.model.Contact;
import com.crm.app.model.User;
import com.crm.app.model.enums.FunnelStatus;
import com.crm.app.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Tag(name = "Contactos", description = "Gestión de contactos del CRM")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @Operation(summary = "Crear contacto", description = "Admin o Vendedor crean contacto. El vendedor solo puede asignarse a sí mismo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Contact> createContact(
            @RequestBody @Valid ContactDTOs.CreateContactRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(contactService.createContact(request, currentUser));
    }

    @GetMapping
    @Operation(summary = "Listar mis contactos", description = "Vendedor ve sus contactos. Admin ve todos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Contact>> getMyContacts(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.getMyContacts(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener contacto por ID", description = "Acceso controlado por rol")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<Contact> getContact(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(contactService.findByIdAndCheckAccess(id, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar contacto", description = "Acceso: Admin o dueño del contacto")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<Contact> updateContact(
            @PathVariable Long id,
            @RequestBody @Valid ContactDTOs.CreateContactRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(contactService.updateContact(id, request, currentUser));
    }

    @PatchMapping("/{id}/funnel-status")
    @Operation(summary = "Actualizar estado del funnel", description = "Cambia el estado del contacto en el proceso de ventas")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<Contact> updateFunnelStatus(
            @PathVariable Long id,
            @RequestParam FunnelStatus status,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(contactService.updateFunnelStatus(id, status, currentUser));
    }

    @PatchMapping("/{id}/assign")
    @Operation(
            summary = "Reasignar contacto a vendedor",
            description = "Solo Admin puede reasignar contactos. Cambia el owner del contacto a otro vendedor."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Contact> reassignContact(
            @PathVariable Long id,
            @RequestParam Long newOwnerId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(contactService.reassignContact(id, newOwnerId, currentUser));
    }

}
