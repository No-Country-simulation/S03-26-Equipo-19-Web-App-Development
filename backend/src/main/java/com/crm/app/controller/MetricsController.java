package com.crm.app.controller;

import com.crm.app.dto.MetricsDTOs;
import com.crm.app.model.User;
import com.crm.app.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "Métricas", description = "Panel de métricas y KPIs (Admin: global, Vendedor: solo sus datos)")
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Dashboard de métricas",
            description = """
                    Retorna KPIs principales: contactos por funnel, mensajes, tasa respuesta, tareas.
                    
                    **Permisos:**
                    - **Admin**: ve métricas globales, puede filtrar por vendedor y fecha
                    - **Vendedor**: solo ve sus propias métricas
                    
                    **Filtros (solo Admin):**
                    - `salespersonEmail`: filtrar por vendedor específico
                    - `startDate`: fecha inicio (YYYY-MM-DD)
                    - `endDate`: fecha fin (YYYY-MM-DD)
                    """
    )
    public ResponseEntity<MetricsDTOs.DashboardMetrics> getDashboard(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @Parameter(description = "Email del vendedor (solo Admin)") String salespersonEmail,
            @RequestParam(required = false) @Parameter(description = "Fecha inicio (YYYY-MM-DD)") String startDate,
            @RequestParam(required = false) @Parameter(description = "Fecha fin (YYYY-MM-DD)") String endDate
    ) {
        return ResponseEntity.ok(metricsService.getDashboardMetrics(currentUser, salespersonEmail, startDate, endDate));
    }

    @GetMapping("/period")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Métricas por período",
            description = "Filtra métricas por rango de fechas"
    )
    public ResponseEntity<MetricsDTOs.PeriodMetrics> getPeriodMetrics(
            @AuthenticationPrincipal User currentUser,
            @RequestParam @Parameter(description = "Período (day/week/month)", example = "week") String period,
            @RequestParam @Parameter(description = "Fecha inicio (YYYY-MM-DD)", example = "2026-04-01") String startDate,
            @RequestParam @Parameter(description = "Fecha fin (YYYY-MM-DD)", example = "2026-04-07") String endDate
    ) {
        return ResponseEntity.ok(metricsService.getPeriodMetrics(currentUser, period, startDate, endDate));
    }

    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Exportar métricas",
            description = """
                    Exporta las métricas en formato CSV o PDF.
                    
                    **Formatos soportados:** csv, pdf
                    
                    **Permisos:**
                    - **Admin**: exporta métricas globales
                    - **Vendedor**: exporta solo sus métricas
                    
                    **Filtros:**
                    - `salespersonEmail`: filtrar por vendedor (solo Admin)
                    - `startDate`: fecha inicio (YYYY-MM-DD)
                    - `endDate`: fecha fin (YYYY-MM-DD)
                    - `format`: csv o pdf
                    """
    )
    public ResponseEntity<byte[]> exportMetrics(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String salespersonEmail,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "csv") String format
    ) {
        var export = metricsService.exportMetrics(currentUser, salespersonEmail, startDate, endDate, format);

        byte[] content;
        MediaType mediaType;

        if (format.equalsIgnoreCase("csv")) {
            content = export.data().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            mediaType = MediaType.parseMediaType("text/csv");
        } else {
            content = Base64.getDecoder().decode(export.data());
            mediaType = MediaType.APPLICATION_PDF;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + export.filename())
                .contentType(mediaType)
                .body(content);
    }

    // En MetricsController.java - Reemplaza los endpoints

    @GetMapping("/contacts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Métricas de contactos", description = "Retorna solo métricas de contactos (funnel)")
    public ResponseEntity<MetricsDTOs.ContactsMetricsResponse> getContactsMetrics(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(metricsService.getContactsMetrics(currentUser));
    }

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Métricas de mensajes", description = "Retorna solo métricas de mensajes")
    public ResponseEntity<MetricsDTOs.MessagesMetricsResponse> getMessagesMetrics(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(metricsService.getMessagesMetrics(currentUser));
    }

    @GetMapping("/tasks")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Métricas de tareas", description = "Retorna solo métricas de tareas")
    public ResponseEntity<MetricsDTOs.TasksMetricsResponse> getTasksMetrics(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(metricsService.getTasksMetrics(currentUser));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Métricas de usuarios", description = "Retorna solo métricas de usuarios (solo ADMIN)")
    public ResponseEntity<MetricsDTOs.UsersMetricsResponse> getUsersMetrics(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(metricsService.getUsersMetrics(currentUser));
    }
}