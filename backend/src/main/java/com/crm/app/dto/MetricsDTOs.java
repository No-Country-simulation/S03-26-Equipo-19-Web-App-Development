package com.crm.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public class MetricsDTOs {

    @Schema(description = "Métricas generales del dashboard")
    public record DashboardMetrics(
            @Schema(description = "Total de contactos activos", example = "156")
            long activeContacts,

            @Schema(description = "Total de mensajes enviados", example = "342")
            long totalMessagesSent,

            @Schema(description = "Tasa de respuesta (%)", example = "68.5")
            double responseRate,

            @Schema(description = "Contactos por estado del funnel")
            Map<String, Long> contactsByFunnelStatus,

            @Schema(description = "Mensajes por canal")
            Map<String, Long> messagesByChannel,

            @Schema(description = "Top vendedores por actividad")
            Map<String, Long> topSalespersons
    ) {}

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
}