package com.crm.app.service;

import com.crm.app.dto.MetricsDTOs;
import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.FunnelStatus;
import com.crm.app.model.enums.Role;
import com.crm.app.model.enums.TaskStatus;
import com.crm.app.repository.ContactRepository;
import com.crm.app.repository.MessageRepository;
import com.crm.app.repository.TaskRepository;
import com.crm.app.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricsService {

    private final ContactRepository contactRepository;
    private final MessageRepository messageRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private static final List<FunnelStatus> ACTIVE_STATUSES = Arrays.asList(
            FunnelStatus.NEW_LEAD,
            FunnelStatus.CONTACTED,
            FunnelStatus.IN_NEGOTIATION,
            FunnelStatus.PROPOSAL_SENT
    );

    // ==================== MÉTODOS DE CÁLCULO DE TENDENCIA ====================

    private MetricsDTOs.MetricValue calculateMetric(long currentValue, long previousValue) {
        double changePercent = 0;
        String trend = "stable";

        if (previousValue > 0) {
            // Hay datos en ambos períodos - cálculo normal
            changePercent = ((currentValue - previousValue) * 100.0) / previousValue;
            changePercent = Math.round(changePercent * 10) / 10.0;
        } else if (currentValue > 0 && previousValue == 0) {
            // No había datos antes, pero ahora sí
            changePercent = 100;  // Crecimiento del 100%
            trend = "up";
        } else if (currentValue == 0 && previousValue > 0) {
            // Había datos antes, pero ahora no
            changePercent = -100;  // Caída del 100%
            trend = "down";
        } else {
            // Ambos son 0
            changePercent = 0;
            trend = "stable";
        }

        if (changePercent > 0) trend = "up";
        else if (changePercent < 0) trend = "down";
        else trend = "stable";

        return new MetricsDTOs.MetricValue(currentValue, changePercent, trend);
    }

    private MetricsDTOs.MetricValueDouble calculateMetricDouble(double currentValue, double previousValue) {
        double changePercent = 0;
        String trend = "stable";

        if (previousValue > 0) {
            changePercent = ((currentValue - previousValue) * 100.0) / previousValue;
            changePercent = Math.round(changePercent * 10) / 10.0;
        } else if (currentValue > 0) {
            changePercent = 100;
            trend = "up";
        }

        if (changePercent > 0) trend = "up";
        else if (changePercent < 0) trend = "down";

        return new MetricsDTOs.MetricValueDouble(currentValue, changePercent, trend);
    }

    private LocalDateTime getPreviousPeriodStart(LocalDateTime currentStart, LocalDateTime currentEnd) {
        long durationDays = java.time.Duration.between(currentStart, currentEnd).toDays();
        if (durationDays <= 0) durationDays = 30;
        return currentStart.minusDays(durationDays);
    }

    private LocalDateTime getPreviousPeriodEnd(LocalDateTime currentStart) {
        return currentStart.minusSeconds(1);
    }

    // ==================== DASHBOARD PRINCIPAL CON CACHÉ ====================

    @Cacheable(value = "dashboardMetrics", key = "#currentUser.id + '-' + #salespersonEmail + '-' + (#startDate != null ? #startDate : 'null') + '-' + (#endDate != null ? #endDate : 'null')")
    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.DashboardMetrics getDashboardMetrics(User currentUser, String salespersonEmail,
                                                            String startDate, String endDate) {
        log.info("📊 Calculando métricas para usuario: {} (rol={})", currentUser.getEmail(), currentUser.getRole());

        LocalDateTime currentStart = parseStartDate(startDate);
        LocalDateTime currentEnd = parseEndDate(endDate);
        LocalDateTime previousStart = getPreviousPeriodStart(currentStart, currentEnd);
        LocalDateTime previousEnd = getPreviousPeriodEnd(currentStart);

        String periodDesc = formatPeriod(startDate, endDate);

        User owner = resolveOwner(currentUser, salespersonEmail);

        // 1. Métricas de contactos por funnel (con filtro de fecha)
        MetricsDTOs.FunnelMetrics funnelMetrics = getFunnelMetrics(owner, currentEnd, previousEnd);

        // 2. Métricas de tareas (con filtro de fecha)
        MetricsDTOs.TaskMetrics taskMetrics = getTaskMetrics(owner, currentEnd, previousEnd);

        // 3. Métricas de usuarios (con filtro de fecha)
        MetricsDTOs.UserMetrics userMetrics = getUserMetrics(currentUser, currentEnd, previousEnd, currentStart, previousStart);

        // 4. Métricas de mensajes (ya funcionan correctamente)
        MetricsDTOs.MessageMetrics messageMetrics = getMessageMetrics(owner, currentStart, currentEnd, previousStart, previousEnd);

        String salespersonInfo = owner != null ? owner.getEmail() : null;

        log.info("✅ Métricas calculadas exitosamente");

        return new MetricsDTOs.DashboardMetrics(
                funnelMetrics,
                null, // contactStatus removido para evitar duplicación
                messageMetrics,
                taskMetrics,
                userMetrics,
                periodDesc,
                salespersonInfo
        );
    }

    // ==================== MÉTRICAS DE CONTACTOS POR FUNNEL ====================

    private MetricsDTOs.FunnelMetrics getFunnelMetrics(User owner, LocalDateTime currentEnd, LocalDateTime previousEnd) {
        Map<String, Long> currentStats = getFunnelStatsForPeriod(owner, currentEnd);
        Map<String, Long> previousStats = getFunnelStatsForPeriod(owner, previousEnd);

        Map<String, MetricsDTOs.MetricValue> byStatus = new LinkedHashMap<>();
        long currentActive = 0;
        long previousActive = 0;

        for (FunnelStatus status : FunnelStatus.values()) {
            String statusName = status.name();
            long currentValue = currentStats.getOrDefault(statusName, 0L);
            long previousValue = previousStats.getOrDefault(statusName, 0L);
            byStatus.put(statusName, calculateMetric(currentValue, previousValue));

            if (ACTIVE_STATUSES.contains(status)) {
                currentActive += currentValue;
                previousActive += previousValue;
            }
        }

        MetricsDTOs.MetricValue totalActive = calculateMetric(currentActive, previousActive);

        return new MetricsDTOs.FunnelMetrics(byStatus, totalActive);
    }

    private Map<String, Long> getFunnelStatsForPeriod(User owner, LocalDateTime endDate) {
        Map<String, Long> stats = new HashMap<>();

        for (FunnelStatus status : FunnelStatus.values()) {
            stats.put(status.name(), 0L);
        }

        List<Object[]> results = contactRepository.countByFunnelStatusWithDate(owner, endDate);

        for (Object[] stat : results) {
            String status = ((FunnelStatus) stat[0]).name();
            Long count = (Long) stat[1];
            stats.put(status, count);
        }

        log.debug("📊 Contact stats until {}: {}", endDate, stats);
        return stats;
    }

    // ==================== MÉTRICAS DE TAREAS ====================

    private MetricsDTOs.TaskMetrics getTaskMetrics(User owner, LocalDateTime currentEnd, LocalDateTime previousEnd) {
        long currentCompleted = getTaskCountByStatusWithDate(owner, TaskStatus.COMPLETED, currentEnd);
        long previousCompleted = getTaskCountByStatusWithDate(owner, TaskStatus.COMPLETED, previousEnd);

        long currentOverdue = getTaskOverdueCountWithDate(owner, currentEnd);
        long previousOverdue = getTaskOverdueCountWithDate(owner, previousEnd);

        long currentPending = getTaskCountByStatusWithDate(owner, TaskStatus.PENDING, currentEnd);
        long previousPending = getTaskCountByStatusWithDate(owner, TaskStatus.PENDING, previousEnd);

        long currentDueToday = getTaskDueTodayCount(owner);
        long previousDueToday = 0; // No hay período anterior para "hoy"

        return new MetricsDTOs.TaskMetrics(
                calculateMetric(currentCompleted, previousCompleted),
                calculateMetric(currentOverdue, previousOverdue),
                calculateMetric(currentPending, previousPending),
                calculateMetric(currentDueToday, previousDueToday)
        );
    }

    private long getTaskCountByStatusWithDate(User owner, TaskStatus status, LocalDateTime endDate) {
        if (owner != null) {
            return taskRepository.countByAssignedToAndStatusWithDate(owner, status, endDate);
        }
        return taskRepository.countByStatusWithDate(status, endDate);
    }

    private long getTaskOverdueCountWithDate(User owner, LocalDateTime endDate) {
        if (owner != null) {
            return taskRepository.countOverdueByUserWithDate(owner, endDate);
        }
        return taskRepository.countOverdueWithDate(endDate);
    }

    private long getTaskDueTodayCount(User owner) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        if (owner != null) {
            return taskRepository.countByAssignedToAndDueDateBetween(owner, todayStart, todayEnd);
        }
        return taskRepository.countByDueDateBetween(todayStart, todayEnd);
    }

    // ==================== MÉTRICAS DE USUARIOS ====================

    private MetricsDTOs.UserMetrics getUserMetrics(User currentUser, LocalDateTime currentEnd, LocalDateTime previousEnd,
                                                   LocalDateTime currentStart, LocalDateTime previousStart) {
        if (currentUser.getRole() != Role.ADMIN) {
            return new MetricsDTOs.UserMetrics(
                    calculateMetric(0, 0),
                    calculateMetric(0, 0),
                    calculateMetric(0, 0),
                    calculateMetric(0, 0)
            );
        }

        long currentTotal = userRepository.countTotalWithDate(currentEnd);
        long previousTotal = userRepository.countTotalWithDate(previousEnd);

        long currentActive = userRepository.countActiveWithDate(currentEnd);
        long previousActive = userRepository.countActiveWithDate(previousEnd);

        long currentInactive = currentTotal - currentActive;
        long previousInactive = previousTotal - previousActive;

        long currentNewUsers = userRepository.countByCreatedAtAfter(currentStart);
        long previousNewUsers = userRepository.countByCreatedAtAfter(previousStart);

        return new MetricsDTOs.UserMetrics(
                calculateMetric(currentTotal, previousTotal),
                calculateMetric(currentActive, previousActive),
                calculateMetric(currentInactive, previousInactive),
                calculateMetric(currentNewUsers, previousNewUsers)
        );
    }

    // ==================== MÉTRICAS DE MENSAJES ====================

    private MetricsDTOs.MessageMetrics getMessageMetrics(User owner, LocalDateTime currentStart, LocalDateTime currentEnd,
                                                         LocalDateTime previousStart, LocalDateTime previousEnd) {
        long currentSent = getMessageCount(owner, currentStart, currentEnd, true);
        long previousSent = getMessageCount(owner, previousStart, previousEnd, true);

        long currentReceived = getMessageCount(owner, currentStart, currentEnd, false);
        long previousReceived = getMessageCount(owner, previousStart, previousEnd, false);

        double currentResponseRate = currentSent > 0 ? Math.round((currentReceived * 100.0 / currentSent) * 10) / 10.0 : 0.0;
        double previousResponseRate = previousSent > 0 ? Math.round((previousReceived * 100.0 / previousSent) * 10) / 10.0 : 0.0;

        Map<String, MetricsDTOs.MetricValue> byChannel = getChannelMetrics(owner, currentStart, currentEnd, previousStart, previousEnd);

        return new MetricsDTOs.MessageMetrics(
                calculateMetric(currentSent, previousSent),
                calculateMetric(currentReceived, previousReceived),
                calculateMetricDouble(currentResponseRate, previousResponseRate),
                byChannel
        );
    }

    private long getMessageCount(User owner, LocalDateTime start, LocalDateTime end, boolean isOutbound) {
        if (owner != null) {
            if (isOutbound) {
                return messageRepository.countOutboundBySenderAndPeriod(owner, start, end);
            } else {
                return messageRepository.countInboundByContactOwnerAndPeriod(owner, start, end);
            }
        } else {
            if (isOutbound) {
                return messageRepository.countOutboundMessagesInPeriod(start, end);
            } else {
                return messageRepository.countInboundMessagesInPeriod(start, end);
            }
        }
    }

    private Map<String, MetricsDTOs.MetricValue> getChannelMetrics(User owner, LocalDateTime currentStart, LocalDateTime currentEnd,
                                                                   LocalDateTime previousStart, LocalDateTime previousEnd) {
        Map<String, Long> currentChannel = new LinkedHashMap<>();
        Map<String, Long> previousChannel = new LinkedHashMap<>();

        currentChannel.put("WHATSAPP", 0L);
        currentChannel.put("EMAIL", 0L);
        previousChannel.put("WHATSAPP", 0L);
        previousChannel.put("EMAIL", 0L);

        if (owner != null) {
            List<Object[]> currentStats = messageRepository.countOutboundByChannelAndSender(owner, currentStart, currentEnd);
            for (Object[] stat : currentStats) {
                currentChannel.put(((Channel) stat[0]).name(), (Long) stat[1]);
            }

            List<Object[]> previousStats = messageRepository.countOutboundByChannelAndSender(owner, previousStart, previousEnd);
            for (Object[] stat : previousStats) {
                previousChannel.put(((Channel) stat[0]).name(), (Long) stat[1]);
            }
        } else {
            List<Object[]> currentStats = messageRepository.countOutboundByChannelInPeriod(currentStart, currentEnd);
            for (Object[] stat : currentStats) {
                currentChannel.put(((Channel) stat[0]).name(), (Long) stat[1]);
            }

            List<Object[]> previousStats = messageRepository.countOutboundByChannelInPeriod(previousStart, previousEnd);
            for (Object[] stat : previousStats) {
                previousChannel.put(((Channel) stat[0]).name(), (Long) stat[1]);
            }
        }

        Map<String, MetricsDTOs.MetricValue> result = new LinkedHashMap<>();
        for (String channel : currentChannel.keySet()) {
            long current = currentChannel.get(channel);
            long previous = previousChannel.get(channel);
            result.put(channel, calculateMetric(current, previous));
        }

        return result;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private User resolveOwner(User currentUser, String salespersonEmail) {
        if (currentUser.getRole() != Role.ADMIN) {
            return currentUser;
        }

        if (salespersonEmail != null && !salespersonEmail.isEmpty()) {
            User owner = userRepository.findByEmail(salespersonEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendedor", salespersonEmail));
            if (owner.getRole() != Role.SALESPERSON) {
                throw new BusinessRuleViolationException("El usuario no es un vendedor");
            }
            return owner;
        }
        return null;
    }

    @CacheEvict(value = "dashboardMetrics", allEntries = true)
    public void clearCache() {
        log.info("🗑️ Caché de métricas limpiado");
    }

    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.PeriodMetrics getPeriodMetrics(User currentUser, String period,
                                                      String startDate, String endDate) {
        log.info("📊 Generando métricas por período: {} - {} a {}", period, startDate, endDate);

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        long messagesSent, messagesReceived, newContacts;

        if (currentUser.getRole() != Role.ADMIN) {
            messagesSent = messageRepository.countOutboundBySenderAndPeriod(currentUser, start, end);
            messagesReceived = messageRepository.countInboundByContactOwnerAndPeriod(currentUser, start, end);
            newContacts = contactRepository.countNewContactsInPeriod(start, end);
        } else {
            messagesSent = messageRepository.countOutboundMessagesInPeriod(start, end);
            messagesReceived = messageRepository.countInboundMessagesInPeriod(start, end);
            newContacts = contactRepository.countNewContactsInPeriod(start, end);
        }

        return new MetricsDTOs.PeriodMetrics(period, startDate, endDate, messagesSent, messagesReceived, newContacts);
    }

    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.ExportMetrics exportMetrics(User currentUser, String salespersonEmail,
                                                   String startDate, String endDate, String format) {
        if (!format.equalsIgnoreCase("csv") && !format.equalsIgnoreCase("pdf")) {
            throw new BusinessRuleViolationException("Formato no soportado. Use 'csv' o 'pdf'");
        }

        var dashboard = getDashboardMetrics(currentUser, salespersonEmail, startDate, endDate);
        String filename = generateFilename(format);
        String data;

        if (format.equalsIgnoreCase("csv")) {
            data = generateCsvMetrics(dashboard, startDate, endDate, salespersonEmail);
        } else {
            data = generatePdfMetrics(dashboard, startDate, endDate, salespersonEmail);
        }

        return new MetricsDTOs.ExportMetrics(data, filename, "text/csv; charset=UTF-8");
    }

    private String generateCsvMetrics(MetricsDTOs.DashboardMetrics dashboard,
                                      String startDate, String endDate, String salespersonEmail) {
        StringBuilder csv = new StringBuilder();

        csv.append("# REPORTE DE MÉTRICAS DEL CRM\n");
        csv.append("# Generado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        csv.append("# Período: ").append(formatPeriod(startDate, endDate)).append("\n");
        if (salespersonEmail != null) {
            csv.append("# Vendedor: ").append(salespersonEmail).append("\n");
        }
        csv.append("\n");

        csv.append("=== RESUMEN GENERAL ===\n");
        csv.append("Métrica,Valor,Cambio %,Tendencia\n");
        csv.append("Contactos activos,").append(dashboard.funnel().totalActive().value()).append(",")
                .append(dashboard.funnel().totalActive().changePercent()).append(",")
                .append(dashboard.funnel().totalActive().trend()).append("\n");
        csv.append("Tareas para hoy,").append(dashboard.tasks().dueToday().value()).append(",")
                .append(dashboard.tasks().dueToday().changePercent()).append(",")
                .append(dashboard.tasks().dueToday().trend()).append("\n");
        csv.append("Tareas vencidas,").append(dashboard.tasks().overdue().value()).append(",")
                .append(dashboard.tasks().overdue().changePercent()).append(",")
                .append(dashboard.tasks().overdue().trend()).append("\n");

        return csv.toString();
    }

    private String generatePdfMetrics(MetricsDTOs.DashboardMetrics dashboard,
                                      String startDate, String endDate, String salespersonEmail) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte de Métricas CRM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            document.add(new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), infoFont));
            document.add(new Paragraph("Período: " + formatPeriod(startDate, endDate), infoFont));
            if (salespersonEmail != null) {
                document.add(new Paragraph("Vendedor: " + salespersonEmail, infoFont));
            }
            document.add(new Paragraph(" "));

            addSectionHeader(document, "Resumen General");
            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            addMetricRow(summaryTable, "Contactos activos", dashboard.funnel().totalActive());
            addMetricRow(summaryTable, "Tareas para hoy", dashboard.tasks().dueToday());
            addMetricRow(summaryTable, "Tareas vencidas", dashboard.tasks().overdue());

            document.add(summaryTable);
            document.close();

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage(), e);
            throw new BusinessRuleViolationException("Error al generar el PDF: " + e.getMessage());
        }
    }

    private void addMetricRow(PdfPTable table, String label, MetricsDTOs.MetricValue metric) {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        table.addCell(new PdfPCell(new Phrase(label, normalFont)));
        table.addCell(new PdfPCell(new Phrase(String.valueOf(metric.value()), normalFont)));
        String trendSymbol = metric.trend().equals("up") ? "↑" : (metric.trend().equals("down") ? "↓" : "→");
        table.addCell(new PdfPCell(new Phrase(trendSymbol + " " + metric.changePercent() + "%", normalFont)));
    }

    private void addSectionHeader(Document document, String title) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph header = new Paragraph(title, headerFont);
        header.setSpacingBefore(15);
        header.setSpacingAfter(5);
        document.add(header);
    }

    private String formatPeriod(String startDate, String endDate) {
        if (startDate == null && endDate == null) {
            return "Últimos 30 días";
        }
        return (startDate != null ? startDate : "inicio") + " a " + (endDate != null ? endDate : "hoy");
    }

    private String generateFilename(String format) {
        String date = LocalDate.now().toString();
        return String.format("metrics_report_%s.%s", date, format);
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now().minusDays(30).atStartOfDay();
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDateTime.now();
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);
    }
}