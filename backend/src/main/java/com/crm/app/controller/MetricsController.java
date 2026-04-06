package com.crm.app.controller;

import com.crm.app.dto.MetricsDTOs;
import com.crm.app.model.User;
import com.crm.app.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            summary = "Exportar métricas a CSV",
            description = "Exporta todas las métricas visibles según el rol y filtros aplicados"
    )
    public ResponseEntity<String> exportMetrics(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String salespersonEmail,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        var export = metricsService.exportMetrics(currentUser, salespersonEmail, startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + export.filename())
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(export.csvData());
    }
}