package com.crm.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public class MetricsDTOs {

    // ==================== METRIC VALUE WITH TREND ====================

    @Schema(description = "Valor métrico con tendencia")
    public record MetricValue(
            @Schema(description = "Valor actual")
            long value,
            @Schema(description = "Porcentaje de cambio vs período anterior")
            double changePercent,
            @Schema(description = "Tendencia: up, down, stable")
            String trend
    ) {}

    @Schema(description = "Valor métrico decimal con tendencia")
    public record MetricValueDouble(
            @Schema(description = "Valor actual")
            double value,
            @Schema(description = "Porcentaje de cambio vs período anterior")
            double changePercent,
            @Schema(description = "Tendencia: up, down, stable")
            String trend
    ) {}

    // ==================== MÉTRICAS DE CONTACTOS ====================

    @Schema(description = "Métricas de contactos por estado del funnel")
    public record FunnelMetrics(
            @Schema(description = "Contactos por estado con tendencia")
            Map<String, MetricValue> byStatus,
            @Schema(description = "Total de contactos activos con tendencia")
            MetricValue totalActive,
            @Schema(description = "Total de contactos (activos + inactivos) con tendencia")
            MetricValue total
    ) {}

    // ==================== MÉTRICAS DE CONTACTOS - ESTADOS ESPECÍFICOS ====================

    @Schema(description = "Métricas de contactos por estados específicos")
    public record ContactStatusMetrics(
            MetricValue inNegotiation,
            MetricValue proposalSent,
            MetricValue closedWon,
            MetricValue closedLost,
            MetricValueDouble conversionRate
    ) {}

    // ==================== MÉTRICAS DE TAREAS ====================

    @Schema(description = "Métricas de tareas")
    public record TaskMetrics(
            MetricValue completed,
            MetricValue overdue,
            MetricValue pending,
            MetricValue dueToday,
            @Schema(description = "Total de tareas (completadas + pendientes + vencidas) con tendencia")
            MetricValue total
    ) {}

    // ==================== MÉTRICAS DE USUARIOS ====================

    @Schema(description = "Métricas de usuarios")
    public record UserMetrics(
            MetricValue total,
            MetricValue active,
            MetricValue inactive,
            MetricValue newUsers
    ) {}

    // ==================== MÉTRICAS DE MENSAJES ====================

    @Schema(description = "Métricas de mensajes")
    public record MessageMetrics(
            MetricValue sent,
            MetricValue received,
            MetricValueDouble responseRate,
            @Schema(description = "Por canal con tendencia")
            Map<String, MetricValue> byChannel,
            @Schema(description = "Total de mensajes (enviados + recibidos) con tendencia")
            MetricValue total
    ) {}

    // ==================== DASHBOARD PRINCIPAL ====================

    @Schema(description = "Dashboard completo de métricas")
    public record DashboardMetrics(
            FunnelMetrics funnel,
            ContactStatusMetrics contactStatus,
            MessageMetrics messages,
            TaskMetrics tasks,
            UserMetrics users,
            String period,
            String salespersonEmail
    ) {}

    // ==================== MÉTRICAS POR PERÍODO ====================

    public record PeriodMetrics(
            String period,
            String startDate,
            String endDate,
            long messagesSent,
            long messagesReceived,
            long newContacts
    ) {}

    // ==================== EXPORTACIÓN ====================

    public record ExportMetrics(
            String data,
            String filename,
            String contentType
    ) {}

    // ==================== RESPUESTAS SEPARADAS ====================

    @Schema(description = "Respuesta solo de métricas de contactos")
    public record ContactsMetricsResponse(
            FunnelMetrics funnel
    ) {}

    @Schema(description = "Respuesta solo de métricas de mensajes")
    public record MessagesMetricsResponse(
            MessageMetrics messages
    ) {}

    @Schema(description = "Respuesta solo de métricas de tareas")
    public record TasksMetricsResponse(
            TaskMetrics tasks
    ) {}

    @Schema(description = "Respuesta solo de métricas de usuarios")
    public record UsersMetricsResponse(
            UserMetrics users
    ) {}
}