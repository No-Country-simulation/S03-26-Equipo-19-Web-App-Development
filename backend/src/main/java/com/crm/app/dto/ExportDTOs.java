package com.crm.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ExportDTOs {

    @Schema(description = "Solicitud de exportación")
    public record ExportRequest(
            @Schema(description = "Tipo de entidad a exportar", example = "contacts")
            String entityType,

            @Schema(description = "ID de la vista guardada (opcional)", example = "1")
            Long savedViewId,

            @Schema(description = "Formato de exportación", example = "csv", allowableValues = {"csv", "pdf"})
            String format,

            @Schema(description = "Filtros adicionales (JSON)")
            String filters
    ) {}

    @Schema(description = "Respuesta de exportación")
    public record ExportResponse(
            @Schema(description = "Datos exportados (CSV o PDF en Base64)")
            String data,

            @Schema(description = "Nombre del archivo", example = "contacts_2026-04-05.csv")
            String filename,

            @Schema(description = "Tipo de contenido", example = "text/csv")
            String contentType
    ) {}
}