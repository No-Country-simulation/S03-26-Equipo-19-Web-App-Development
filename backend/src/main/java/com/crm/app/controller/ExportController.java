package com.crm.app.controller;

import com.crm.app.dto.ExportDTOs;
import com.crm.app.model.User;
import com.crm.app.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Tag(name = "Exportación", description = "Exportación de datos a CSV/PDF (respeta filtros y roles)")
public class ExportController {

    private final ExportService exportService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Exportar datos",
            description = """
                    Exporta datos del sistema a CSV o PDF.
                    
                    **Permisos:**
                    - **Admin**: puede exportar cualquier dato
                    - **Vendedor**: solo puede exportar sus propios datos
                    
                    **Tipos de entidad:** contacts, conversations, messages, tasks
                    
                    **Formatos:** csv, pdf
                    
                    **Filtros:** se pueden aplicar usando savedViewId o filters JSON
                    """
    )
    public ResponseEntity<?> export(@Valid @RequestBody ExportDTOs.ExportRequest request,
                                    @AuthenticationPrincipal User currentUser) {

        var response = exportService.exportData(request, currentUser);

        if (request.format().equalsIgnoreCase("csv")) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.filename())
                    .header(HttpHeaders.CONTENT_TYPE, response.contentType())
                    .body(response.data());
        } else {
            // PDF: decodificar Base64 y devolver binario
            byte[] pdfBytes = Base64.getDecoder().decode(response.data());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + response.filename())
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdfBytes);
        }
    }
}