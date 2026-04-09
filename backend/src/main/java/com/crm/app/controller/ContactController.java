package com.crm.app.controller;

import com.crm.app.dto.ContactDTOs;
import com.crm.app.model.Contact;
import com.crm.app.model.User;
import com.crm.app.model.enums.FunnelStatus;
import com.crm.app.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Crear contacto",
            description = """
                    Crea un nuevo contacto en el sistema.
                    
                    **Permisos:**
                    - **ADMIN**: Puede asignar el contacto a cualquier vendedor (enviando ownerId)
                    - **VENDEDOR**: El contacto se asigna automáticamente a sí mismo
                    
                    **Reglas:**
                    - El contacto debe tener al menos nombre, apellido, email o teléfono
                    - El email y teléfono deben ser únicos por vendedor
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del contacto a crear",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Contacto básico",
                                            value = """
                                            {
                                                "contact": {
                                                    "name": "Juan",
                                                    "lastName": "Pérez",
                                                    "email": "juan@gmail.com",
                                                    "phone": "5491123456789",
                                                    "company": "Tech Solutions"
                                                },
                                                "preferredChannel": "WHATSAPP"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Admin asignando a vendedor",
                                            value = """
                                            {
                                                "contact": {
                                                    "name": "Juan",
                                                    "lastName": "Pérez",
                                                    "email": "juan@gmail.com",
                                                    "phone": "5491123456789",
                                                    "company": "Tech Solutions"
                                                },
                                                "preferredChannel": "EMAIL",
                                                "ownerId": 2
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contacto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o contacto duplicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContactDTOs.ContactSummaryResponse> createContact(
            @RequestBody @Valid ContactDTOs.CreateContactRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.createContact(request, currentUser));
    }

    @GetMapping
    @Operation(
            summary = "Listar mis contactos",
            description = """
                    Retorna la lista de contactos del usuario autenticado.
                    
                    **Permisos:**
                    - **ADMIN**: Ve todos los contactos del sistema
                    - **VENDEDOR**: Solo ve sus propios contactos
                    
                    **Incluye:** Información del vendedor asignado y etiquetas del contacto
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContactDTOs.ContactDetailResponse>> getMyContacts(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.getMyContactsDetailResponse(currentUser));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener contacto por ID",
            description = """
                    Retorna los detalles completos de un contacto específico.
                    
                    **Permisos:**
                    - **ADMIN**: Puede ver cualquier contacto
                    - **VENDEDOR**: Solo puede ver sus propios contactos
                    
                    **Incluye:** Vendedor asignado y etiquetas del contacto
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contacto encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - No eres el dueño del contacto"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<ContactDTOs.ContactDetailResponse> getContact(
            @Parameter(description = "ID del contacto", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.getContactDetailResponse(id, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar contacto",
            description = """
                    Actualiza los datos de un contacto existente.
                    
                    **Permisos:**
                    - **ADMIN**: Puede actualizar cualquier contacto y reasignar el vendedor
                    - **VENDEDOR**: Solo puede actualizar sus propios contactos (no puede cambiar el vendedor)
                    
                    **Campos actualizables:** nombre, apellido, email, teléfono, empresa, canal preferido
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contacto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicados"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<ContactDTOs.ContactDetailResponse> updateContact(
            @Parameter(description = "ID del contacto", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody @Valid ContactDTOs.CreateContactRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.updateContact(id, request, currentUser));
    }

    @PatchMapping("/{id}/funnel-status")
    @Operation(
            summary = "Actualizar estado del funnel",
            description = """
                    Cambia el estado del contacto en el proceso de ventas.
                    
                    **Estados disponibles:**
                    - `NEW_LEAD`: Nuevo lead
                    - `CONTACTED`: Contactado
                    - `IN_NEGOTIATION`: En negociación
                    - `PROPOSAL_SENT`: Propuesta enviada
                    - `CLOSED_WON`: Ganado
                    - `CLOSED_LOST`: Perdido
                    
                    **Permisos:**
                    - **ADMIN**: Puede actualizar cualquier contacto
                    - **VENDEDOR**: Solo puede actualizar sus propios contactos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Contacto no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN') or @contactService.isOwner(#id, principal)")
    public ResponseEntity<Contact> updateFunnelStatus(
            @Parameter(description = "ID del contacto", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado del funnel", example = "CONTACTED", required = true)
            @RequestParam FunnelStatus status,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.updateFunnelStatus(id, status, currentUser));
    }

    @PatchMapping("/{id}/assign")
    @Operation(
            summary = "Reasignar contacto a vendedor",
            description = """
                    Cambia el vendedor responsable de un contacto.
                    
                    **Permisos:** Solo disponible para ADMIN
                    
                    **Uso:** Enviar el ID del nuevo vendedor en el parámetro `newOwnerId`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contacto reasignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "ID de vendedor inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Contacto o vendedor no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Contact> reassignContact(
            @Parameter(description = "ID del contacto", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID del nuevo vendedor", example = "2", required = true)
            @RequestParam Long newOwnerId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(contactService.reassignContact(id, newOwnerId, currentUser));
    }
}