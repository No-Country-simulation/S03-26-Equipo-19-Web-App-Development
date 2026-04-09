package com.crm.app.controller;

import com.crm.app.dto.TemplateDTOs;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = "Plantillas", description = "Gestión de plantillas de mensajes (WhatsApp y Email)")
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Crear plantilla",
            description = """
                    Crea una nueva plantilla de mensaje.
                    
                    **Reglas de negocio:**
                    - **ADMIN**: Crea plantillas GLOBALES (visibles para todos los usuarios)
                    - **VENDEDOR**: Crea plantillas PERSONALES (solo visibles para él)
                    - El nombre debe ser único dentro del contexto (global o personal)
                    - Todas las variables en el cuerpo (ej: {{name}}) deben declararse en `variables`
                    
                    **Ejemplo de variables:**
                    Si el cuerpo es "Hola {{name}}, bienvenido a {{company}}"
                    Entonces `variables` debe ser: {"name": "", "company": ""}
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la plantilla",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Plantilla WhatsApp",
                                            description = "Plantilla para WhatsApp sin subject",
                                            value = """
                                            {
                                                "name": "Bienvenida WhatsApp",
                                                "channel": "WHATSAPP",
                                                "body": "Hola {{name}}, gracias por contactarnos. ¿En qué podemos ayudarte?",
                                                "variables": {
                                                    "name": "Cliente"
                                                }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Plantilla Email",
                                            description = "Plantilla para Email con subject",
                                            value = """
                                            {
                                                "name": "Propuesta Comercial",
                                                "channel": "EMAIL",
                                                "subject": "Propuesta para {{company}}",
                                                "body": "Estimado {{name}},\\n\\nAdjunto encontrarás la propuesta comercial para {{company}}.\\n\\nSaludos cordiales,\\n{{salesperson}}",
                                                "variables": {
                                                    "name": "Juan Pérez",
                                                    "company": "TechCorp",
                                                    "salesperson": "Equipo de Ventas"
                                                }
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plantilla creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o variables no declaradas"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "Ya existe una plantilla con ese nombre en este contexto")
    })
    public ResponseEntity<TemplateDTOs.TemplateResponse> create(
            @Valid @RequestBody TemplateDTOs.TemplateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(templateService.createTemplateResponse(request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Actualizar plantilla",
            description = """
                    Actualiza una plantilla existente.
                    
                    **Permisos:** 
                    - Solo el creador de la plantilla puede actualizarla
                    - ADMIN puede actualizar cualquier plantilla (incluyendo las de otros)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plantilla actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (no eres el creador y no eres ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Plantilla no encontrada")
    })
    public ResponseEntity<TemplateDTOs.TemplateResponse> update(
            @Parameter(description = "ID de la plantilla", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TemplateDTOs.TemplateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(templateService.updateTemplateResponse(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Eliminar plantilla",
            description = """
                    Elimina una plantilla permanentemente.
                    
                    **Permisos:** 
                    - Solo el creador de la plantilla puede eliminarla
                    - ADMIN puede eliminar cualquier plantilla (incluyendo las de otros)
                    
                    **Nota:** Esta acción no se puede deshacer.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plantilla eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (no eres el creador y no eres ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Plantilla no encontrada")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la plantilla", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        templateService.deleteTemplate(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar plantillas",
            description = """
                    Lista las plantillas según el rol del usuario autenticado:
                    
                    **ADMIN**: 
                    - Ve TODAS las plantillas del sistema (globales y personales de todos los usuarios)
                    
                    **VENDEDOR**: 
                    - Ve las plantillas GLOBALES (creadas por ADMIN)
                    - Ve sus propias plantillas PERSONALES
                    - NO ve plantillas personales de otros vendedores
                    
                    **Filtros opcionales:**
                    - `channel`: Filtra por canal (WHATSAPP o EMAIL)
                    """,
            parameters = {
                    @Parameter(name = "channel", description = "Filtrar por canal",
                            example = "WHATSAPP", schema = @Schema(implementation = Channel.class))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<List<TemplateDTOs.TemplateResponse>> list(
            @RequestParam(required = false) Channel channel,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(templateService.listTemplatesResponse(currentUser, channel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Obtener plantilla por ID",
            description = """
                    Obtiene los detalles completos de una plantilla específica.
                    
                    **Permisos de acceso:**
                    - **ADMIN**: Puede ver CUALQUIER plantilla
                    - **VENDEDOR**: Solo puede ver:
                        - Plantillas GLOBALES (creadas por ADMIN)
                        - Sus propias plantillas PERSONALES
                    
                    Si un VENDEDOR intenta acceder a la plantilla personal de otro vendedor, 
                    recibirá un error 403 (No autorizado).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plantilla encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (sin acceso a esta plantilla)"),
            @ApiResponse(responseCode = "404", description = "Plantilla no encontrada")
    })
    public ResponseEntity<TemplateDTOs.TemplateResponse> get(
            @Parameter(description = "ID de la plantilla", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(templateService.getTemplateResponse(id, currentUser));
    }

    @GetMapping("/channel/{channel}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar plantillas por canal",
            description = """
                    Endpoint alternativo para filtrar plantillas por canal.
                    
                    **Ejemplos:**
                    - `/api/v1/templates/channel/WHATSAPP` - Solo plantillas de WhatsApp
                    - `/api/v1/templates/channel/EMAIL` - Solo plantillas de Email
                    
                    Las reglas de visibilidad (ADMIN vs VENDEDOR) son las mismas que en el listado general.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<List<TemplateDTOs.TemplateResponse>> listByChannel(
            @Parameter(description = "Canal (WHATSAPP o EMAIL)", required = true,
                    example = "WHATSAPP", schema = @Schema(implementation = Channel.class))
            @PathVariable Channel channel,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(templateService.listTemplatesResponse(currentUser, channel));
    }
}