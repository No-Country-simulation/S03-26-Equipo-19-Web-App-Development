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
    @Operation(summary = "Crear contacto")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContactDTOs.ContactSummaryResponse> createContact(
            @RequestBody @Valid ContactDTOs.CreateContactRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.createContact(request, currentUser));
    }

    @GetMapping
    @Operation(summary = "Listar mis contactos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContactDTOs.ContactDetailResponse>> getMyContacts(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.getMyContactsDetailResponse(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener contacto por ID")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<ContactDTOs.ContactDetailResponse> getContact(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.getContactDetailResponse(id, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar contacto")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<ContactDTOs.ContactDetailResponse> updateContact(
            @PathVariable Long id,
            @RequestBody @Valid ContactDTOs.CreateContactRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.updateContact(id, request, currentUser));
    }

    @PatchMapping("/{id}/funnel-status")
    @Operation(summary = "Actualizar estado del funnel")
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<Contact> updateFunnelStatus(
            @PathVariable Long id,
            @RequestParam FunnelStatus status,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.updateFunnelStatus(id, status, currentUser));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Reasignar contacto a vendedor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Contact> reassignContact(
            @PathVariable Long id,
            @RequestParam Long newOwnerId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.reassignContact(id, newOwnerId, currentUser));
    }
}