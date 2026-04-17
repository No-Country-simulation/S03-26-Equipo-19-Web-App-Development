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


    @Schema(description = "Métricas resumidas para el panel principal")
    public record PanelMetrics(
            @Schema(description = "Total de contactos con tendencia")
            MetricValue totalContacts,

            @Schema(description = "Total de mensajes con tendencia")
            MetricValue totalMessages,

            @Schema(description = "Próximas tareas (pendientes + para hoy) con tendencia")
            MetricValue upcomingTasks,

            @Schema(description = "Tasa de respuesta (%) con tendencia")
            MetricValueDouble responseRate
    ) {}

    // En MetricsDTOs.java - Agrega estos records

    @Schema(description = "Información del mejor vendedor")
    public record TopSalespersonInfo(
            @Schema(description = "ID del vendedor", example = "2")
            Long id,

            @Schema(description = "Nombre del vendedor", example = "Alice")
            String name,

            @Schema(description = "Email del vendedor", example = "alice@crm.com")
            String email,

            @Schema(description = "Cantidad de mensajes enviados en el período", example = "45")
            long messagesSent,

            @Schema(description = "Score de desempeño (0-100)", example = "85.5")
            double performanceScore
    ) {}

    @Schema(description = "Métricas globales para el dashboard principal")
    public record GlobalMetricsResponse(
            @Schema(description = "Total de conversaciones con tendencia")
            MetricValue totalConversations,

            @Schema(description = "Tasa de respuesta (%) con tendencia")
            MetricValueDouble responseRate,

            @Schema(description = "Tareas completadas con tendencia")
            MetricValue completedTasks,

            @Schema(description = "Mejor vendedor del período (últimos 30 días)")
            TopSalespersonInfo topSalesperson
    ) {}

    // En MetricsDTOs.java - Agrega estos records

    // En MetricsDTOs.java - Actualiza TemplateMetrics

    @Schema(description = "Métricas de plantillas")
    public record TemplateMetrics(
            @Schema(description = "Total de plantillas con tendencia")
            MetricValue total,

            @Schema(description = "Plantillas creadas este mes con tendencia")
            MetricValue createdThisMonth,

            @Schema(description = "Plantillas creadas hoy con tendencia")
            MetricValue createdToday,

            @Schema(description = "Promedio diario de plantillas creadas")
            double dailyAverage,

            @Schema(description = "Tendencia del promedio diario")
            MetricValue dailyAverageTrend
    ) {}

    @Schema(description = "Respuesta de métricas de plantillas")
    public record TemplatesMetricsResponse(
            @Schema(description = "Métricas de plantillas")
            TemplateMetrics templates
    ) {}
}