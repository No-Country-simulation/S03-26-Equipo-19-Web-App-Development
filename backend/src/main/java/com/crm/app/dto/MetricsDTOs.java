package com.crm.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public class MetricsDTOs {

    // ==================== MÉTRICAS DE CONTACTOS ====================

    @Schema(description = "Métricas de contactos por estado del funnel")
    public record FunnelMetrics(
            @Schema(description = "Contactos por estado", example = "{\"NEW_LEAD\": 45, \"CONTACTED\": 38}")
            Map<String, Long> byStatus,
            @Schema(description = "Total de contactos activos", example = "156")
            long totalActive
    ) {}

    // ==================== MÉTRICAS DE MENSAJES ====================

    @Schema(description = "Métricas de mensajes")
    public record MessageMetrics(
            @Schema(description = "Mensajes enviados", example = "342")
            long sent,
            @Schema(description = "Mensajes recibidos", example = "234")
            long received,
            @Schema(description = "Tasa de respuesta (%)", example = "68.5")
            double responseRate,
            @Schema(description = "Por canal", example = "{\"WHATSAPP\": 210, \"EMAIL\": 132}")
            Map<String, Long> byChannel
    ) {}

    // ==================== MÉTRICAS DE TAREAS ====================

    @Schema(description = "Métricas de tareas")
    public record TaskMetrics(
            @Schema(description = "Tareas completadas", example = "45")
            long completed,
            @Schema(description = "Tareas vencidas", example = "12")
            long overdue,
            @Schema(description = "Tareas pendientes", example = "28")
            long pending
    ) {}

    // ==================== DASHBOARD PRINCIPAL ====================

    @Schema(description = "Dashboard completo de métricas")
    public record DashboardMetrics(
            @Schema(description = "Métricas de contactos")
            FunnelMetrics funnel,
            @Schema(description = "Métricas de mensajes")
            MessageMetrics messages,
            @Schema(description = "Métricas de tareas")
            TaskMetrics tasks,
            @Schema(description = "Período analizado", example = "2026-04-01 a 2026-04-07")
            String period,
            @Schema(description = "Vendedor (null = todos)", example = "alice@crm.com")
            String salespersonEmail
    ) {}

    // ==================== MÉTRICAS POR PERÍODO ====================

    @Schema(description = "Métricas filtradas por período")
    public record PeriodMetrics(
            @Schema(description = "Período (day/week/month)", example = "week")
            String period,
            @Schema(description = "Fecha de inicio", example = "2026-04-01")
            String startDate,
            @Schema(description = "Fecha de fin", example = "2026-04-07")
            String endDate,
            @Schema(description = "Mensajes enviados en el período", example = "89")
            long messagesSent,
            @Schema(description = "Mensajes recibidos en el período", example = "61")
            long messagesReceived,
            @Schema(description = "Nuevos contactos en el período", example = "23")
            long newContacts
    ) {}

    // ==================== EXPORTACIÓN ====================

    @Schema(description = "Exportación de métricas a CSV")
    public record ExportMetrics(
            @Schema(description = "Datos en formato CSV")
            String csvData,
            @Schema(description = "Nombre del archivo", example = "metrics_2026-04-01.csv")
            String filename
    ) {}
}