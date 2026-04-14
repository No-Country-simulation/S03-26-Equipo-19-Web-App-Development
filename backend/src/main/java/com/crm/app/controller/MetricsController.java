package com.crm.app.controller;

import com.crm.app.dto.MetricsDTOs;
import com.crm.app.model.User;
import com.crm.app.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            summary = "Dashboard completo de métricas",
            description = """
                    Retorna KPIs principales: contactos por funnel, mensajes, tasa respuesta, tareas.
                    
                    **Permisos:**
                    - **ADMIN**: ve métricas globales, puede filtrar por vendedor y fecha
                    - **VENDEDOR**: solo ve sus propias métricas
                    
                    **Filtros (solo Admin):**
                    - `salespersonEmail`: filtrar por vendedor específico
                    - `startDate`: fecha inicio (YYYY-MM-DD)
                    - `endDate`: fecha fin (YYYY-MM-DD)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<MetricsDTOs.DashboardMetrics> getDashboard(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @Parameter(description = "Email del vendedor (solo Admin)", example = "alice@crm.com") String salespersonEmail,
            @RequestParam(required = false) @Parameter(description = "Fecha inicio (YYYY-MM-DD)", example = "2026-03-01") String startDate,
            @RequestParam(required = false) @Parameter(description = "Fecha fin (YYYY-MM-DD)", example = "2026-04-14") String endDate
    ) {
        return ResponseEntity.ok(metricsService.getDashboardMetrics(currentUser, salespersonEmail, startDate, endDate));
    }

    @GetMapping("/contacts")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Métricas de contactos",
            description = """
                    Retorna métricas de contactos incluyendo:
                    - Distribución por estado del funnel (NEW_LEAD, CONTACTED, IN_NEGOTIATION, PROPOSAL_SENT, CLOSED_WON, CLOSED_LOST)
                    - Total de contactos activos (NEW_LEAD + CONTACTED + IN_NEGOTIATION + PROPOSAL_SENT)
                    - Total de contactos (todos los estados)
                    
                    Cada métrica incluye valor actual, porcentaje de cambio y tendencia (up/down/stable).
                    
                    **Permisos:**
                    - **ADMIN**: ve todos los contactos
                    - **VENDEDOR**: solo ve sus propios contactos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<MetricsDTOs.ContactsMetricsResponse> getContactsMetrics(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(metricsService.getContactsMetrics(currentUser));
    }

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Métricas de mensajes",
            description = """
                    Retorna métricas de mensajes incluyendo:
                    - Mensajes enviados (OUTBOUND)
                    - Mensajes recibidos (INBOUND)
                    - Tasa de respuesta (%)
                    - Distribución por canal (WHATSAPP / EMAIL)
                    - Total de mensajes (enviados + recibidos)
                    
                    Cada métrica incluye valor actual, porcentaje de cambio y tendencia (up/down/stable).
                    
                    **Permisos:**
                    - **ADMIN**: ve todos los mensajes
                    - **VENDEDOR**: solo ve mensajes de sus conversaciones
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<MetricsDTOs.MessagesMetricsResponse> getMessagesMetrics(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(metricsService.getMessagesMetrics(currentUser));
    }

    @GetMapping("/tasks")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Métricas de tareas",
            description = """
                    Retorna métricas de tareas incluyendo:
                    - Tareas completadas
                    - Tareas vencidas (OVERDUE)
                    - Tareas pendientes (PENDING)
                    - Tareas para hoy
                    - Total de tareas (completadas + pendientes + vencidas)
                    
                    Cada métrica incluye valor actual, porcentaje de cambio y tendencia (up/down/stable).
                    
                    **Permisos:**
                    - **ADMIN**: ve todas las tareas
                    - **VENDEDOR**: solo ve sus propias tareas
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<MetricsDTOs.TasksMetricsResponse> getTasksMetrics(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(metricsService.getTasksMetrics(currentUser));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Métricas de usuarios",
            description = """
                    Retorna métricas de usuarios incluyendo:
                    - Total de usuarios
                    - Usuarios activos
                    - Usuarios inactivos
                    - Nuevos usuarios (últimos 30 días)
                    
                    Cada métrica incluye valor actual, porcentaje de cambio y tendencia (up/down/stable).
                    
                    **Permisos:** Solo disponible para ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    public ResponseEntity<MetricsDTOs.UsersMetricsResponse> getUsersMetrics(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(metricsService.getUsersMetrics(currentUser));
    }

    @GetMapping("/period")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Métricas por período",
            description = """
                    Retorna métricas filtradas por rango de fechas.
                    
                    **Permisos:**
                    - **ADMIN**: puede ver cualquier período
                    - **VENDEDOR**: solo ve sus propios datos en el período
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
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
                    - **ADMIN**: exporta métricas globales
                    - **VENDEDOR**: exporta solo sus métricas
                    
                    **Filtros (solo Admin):**
                    - `salespersonEmail`: filtrar por vendedor específico
                    - `startDate`: fecha inicio (YYYY-MM-DD)
                    - `endDate`: fecha fin (YYYY-MM-DD)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exportación exitosa"),
            @ApiResponse(responseCode = "400", description = "Formato no soportado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<byte[]> exportMetrics(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @Parameter(description = "Email del vendedor (solo Admin)", example = "alice@crm.com") String salespersonEmail,
            @RequestParam(required = false) @Parameter(description = "Fecha inicio (YYYY-MM-DD)", example = "2026-03-01") String startDate,
            @RequestParam(required = false) @Parameter(description = "Fecha fin (YYYY-MM-DD)", example = "2026-04-14") String endDate,
            @RequestParam(defaultValue = "csv") @Parameter(description = "Formato de exportación: csv o pdf", example = "csv") String format
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

    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Limpiar caché de métricas",
            description = "Limpia la caché del dashboard de métricas. Útil después de seedear nuevos datos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Caché limpiada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN")
    })
    public ResponseEntity<Void> clearCache() {
        metricsService.clearCache();
        return ResponseEntity.noContent().build();
    }
}