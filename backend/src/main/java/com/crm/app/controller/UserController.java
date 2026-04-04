package com.crm.app.controller;

import com.crm.app.dto.UserDTOs;
import com.crm.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salespersons")
@RequiredArgsConstructor
@Tag(name = "Vendedores", description = "Endpoints para gestión de vendedores (Solo ADMIN)")
@SecurityRequirement(name = "TOKEN")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService service;

    @GetMapping
    @Operation(
            summary = "Listar vendedores",
            description = "Obtiene la lista de todos los vendedores activos en el sistema. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    [
                        {
                            "id": 2,
                            "name": "Alice Johnson",
                            "email": "alice@crm.com",
                            "role": "SELLER",
                            "active": true,
                            "createdAt": "2024-01-15T10:30:00"
                        },
                        {
                            "id": 3,
                            "name": "Bob Martinez",
                            "email": "bob@crm.com",
                            "role": "SELLER",
                            "active": true,
                            "createdAt": "2024-01-15T10:30:00"
                        }
                    ]
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<List<UserDTOs.UserResponse>> list() {
        return ResponseEntity.ok(service.listSalespersons());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener vendedor por ID",
            description = "Devuelve los detalles de un vendedor específico por su ID. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vendedor encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "id": 2,
                        "name": "Alice Johnson",
                        "email": "alice@crm.com",
                        "role": "SELLER",
                        "active": true,
                        "createdAt": "2024-01-15T10:30:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<UserDTOs.UserResponse> get(
            @Parameter(description = "ID del vendedor", example = "2", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getSalesperson(id));
    }

    @PostMapping
    @Operation(
            summary = "Crear vendedor",
            description = "Crea un nuevo vendedor en el sistema. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Vendedor creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "id": 6,
                        "name": "New Seller",
                        "email": "newseller@crm.com",
                        "role": "SELLER",
                        "active": true,
                        "createdAt": "2024-03-20T14:30:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<UserDTOs.UserResponse> create(
            @Valid @RequestBody UserDTOs.CreateSalespersonRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createSalesperson(request));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar vendedor",
            description = "Actualiza los datos de un vendedor existente. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vendedor actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "id": 2,
                        "name": "Alice Updated",
                        "email": "alice@crm.com",
                        "role": "SELLER",
                        "active": true,
                        "createdAt": "2024-01-15T10:30:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<UserDTOs.UserResponse> update(
            @Parameter(description = "ID del vendedor", example = "2", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserDTOs.UpdateSalespersonRequest request
    ) {
        return ResponseEntity.ok(service.updateSalesperson(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Desactivar vendedor",
            description = "Realiza un soft delete del vendedor (active = false). Sus contactos deben ser reasignados. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vendedor desactivado exitosamente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "ID del vendedor a desactivar", example = "2", required = true)
            @PathVariable Long id
    ) {
        service.deactivateSalesperson(id);
        return ResponseEntity.noContent().build();
    }
}