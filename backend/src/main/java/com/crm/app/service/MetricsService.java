package com.crm.app.service;

import com.crm.app.dto.MetricsDTOs;
import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.FunnelStatus;
import com.crm.app.model.enums.Role;
import com.crm.app.model.enums.TaskStatus;
import com.crm.app.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricsService {

    private final ContactRepository contactRepository;
    private final MessageRepository messageRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final TemplateRepository templateRepository;

    private static final List<FunnelStatus> ACTIVE_STATUSES = List.of(
            FunnelStatus.NEW_LEAD,
            FunnelStatus.CONTACTED,
            FunnelStatus.IN_NEGOTIATION,
            FunnelStatus.PROPOSAL_SENT
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_DAYS = 30;

    // ==================== MÉTODOS DE CÁLCULO DE TENDENCIA ====================

    private MetricsDTOs.MetricValue calculateMetric(long currentValue, long previousValue) {
        if (currentValue == previousValue) {
            return new MetricsDTOs.MetricValue(currentValue, 0, "stable");
        }

        if (previousValue == 0) {
            return new MetricsDTOs.MetricValue(currentValue, 100, currentValue > 0 ? "up" : "stable");
        }

        if (currentValue == 0) {
            return new MetricsDTOs.MetricValue(0, -100, "down");
        }

        double changePercent = ((currentValue - previousValue) * 100.0) / previousValue;
        changePercent = Math.round(changePercent * 10) / 10.0;
        String trend = changePercent > 0 ? "up" : "down";

        return new MetricsDTOs.MetricValue(currentValue, changePercent, trend);
    }

    private MetricsDTOs.MetricValueDouble calculateMetricDouble(double currentValue, double previousValue) {
        if (currentValue == previousValue) {
            return new MetricsDTOs.MetricValueDouble(currentValue, 0, "stable");
        }

        if (previousValue == 0) {
            return new MetricsDTOs.MetricValueDouble(currentValue, 100, currentValue > 0 ? "up" : "stable");
        }

        if (currentValue == 0) {
            return new MetricsDTOs.MetricValueDouble(0, -100, "down");
        }

        double changePercent = ((currentValue - previousValue) * 100.0) / previousValue;
        changePercent = Math.round(changePercent * 10) / 10.0;
        String trend = changePercent > 0 ? "up" : "down";

        return new MetricsDTOs.MetricValueDouble(currentValue, changePercent, trend);
    }

    private LocalDateTime getPreviousPeriodStart(LocalDateTime currentStart, LocalDateTime currentEnd) {
        long durationDays = java.time.Duration.between(currentStart, currentEnd).toDays();
        return currentStart.minusDays(durationDays <= 0 ? DEFAULT_DAYS : durationDays);
    }

    private LocalDateTime getPreviousPeriodEnd(LocalDateTime currentStart) {
        return currentStart.minusSeconds(1);
    }

    // ==================== DASHBOARD PRINCIPAL ====================

    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.DashboardMetrics getDashboardMetrics(User currentUser, String salespersonEmail,
                                                            String startDate, String endDate) {
        log.info("📊 Calculando métricas para usuario: {} (rol={})", currentUser.getEmail(), currentUser.getRole());

        LocalDateTime currentStart = parseStartDate(startDate);
        LocalDateTime currentEnd = parseEndDate(endDate);
        LocalDateTime previousStart = getPreviousPeriodStart(currentStart, currentEnd);
        LocalDateTime previousEnd = getPreviousPeriodEnd(currentStart);

        User owner = resolveOwner(currentUser, salespersonEmail);

        return new MetricsDTOs.DashboardMetrics(
                getFunnelMetrics(owner, currentEnd, previousEnd),
                null,
                getMessageMetrics(owner, currentStart, currentEnd, previousStart, previousEnd),
                getTaskMetrics(owner, currentEnd, previousEnd),
                getUserMetrics(currentUser, currentEnd, previousEnd, currentStart, previousStart),
                formatPeriod(startDate, endDate),
                owner != null ? owner.getEmail() : null
        );
    }

    // ==================== MÉTRICAS DE CONTACTOS ====================

    private MetricsDTOs.FunnelMetrics getFunnelMetrics(User owner, LocalDateTime currentEnd, LocalDateTime previousEnd) {
        Map<String, Long> currentStats = getFunnelStatsForPeriod(owner, currentEnd);
        Map<String, Long> previousStats = getFunnelStatsForPeriod(owner, previousEnd);

        Map<String, MetricsDTOs.MetricValue> byStatus = new LinkedHashMap<>();
        long currentActive = 0, previousActive = 0, currentTotal = 0, previousTotal = 0;

        for (FunnelStatus status : FunnelStatus.values()) {
            String statusName = status.name();
            long currentValue = currentStats.getOrDefault(statusName, 0L);
            long previousValue = previousStats.getOrDefault(statusName, 0L);
            byStatus.put(statusName, calculateMetric(currentValue, previousValue));

            if (ACTIVE_STATUSES.contains(status)) {
                currentActive += currentValue;
                previousActive += previousValue;
            }
            currentTotal += currentValue;
            previousTotal += previousValue;
        }

        return new MetricsDTOs.FunnelMetrics(byStatus,
                calculateMetric(currentActive, previousActive),
                calculateMetric(currentTotal, previousTotal));
    }

    private Map<String, Long> getFunnelStatsForPeriod(User owner, LocalDateTime endDate) {
        Map<String, Long> stats = new HashMap<>();
        for (FunnelStatus status : FunnelStatus.values()) {
            stats.put(status.name(), 0L);
        }

        List<Object[]> results = contactRepository.countByFunnelStatusWithDate(owner, endDate);
        for (Object[] stat : results) {
            stats.put(((FunnelStatus) stat[0]).name(), (Long) stat[1]);
        }
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

        long currentTotal = currentCompleted + currentOverdue + currentPending;
        long previousTotal = previousCompleted + previousOverdue + previousPending;

        return new MetricsDTOs.TaskMetrics(
                calculateMetric(currentCompleted, previousCompleted),
                calculateMetric(currentOverdue, previousOverdue),
                calculateMetric(currentPending, previousPending),
                calculateMetric(currentDueToday, 0),
                calculateMetric(currentTotal, previousTotal));
    }

    private long getTaskCountByStatusWithDate(User owner, TaskStatus status, LocalDateTime endDate) {
        return owner != null
                ? taskRepository.countByAssignedToAndStatusWithDate(owner, status, endDate)
                : taskRepository.countByStatusWithDate(status, endDate);
    }

    private long getTaskOverdueCountWithDate(User owner, LocalDateTime endDate) {
        return owner != null
                ? taskRepository.countOverdueByUserWithDate(owner, endDate)
                : taskRepository.countOverdueWithDate(endDate);
    }

    private long getTaskDueTodayCount(User owner) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        return owner != null
                ? taskRepository.countByAssignedToAndDueDateBetween(owner, todayStart, todayEnd)
                : taskRepository.countByDueDateBetween(todayStart, todayEnd);
    }

    // ==================== MÉTRICAS DE USUARIOS ====================

    private MetricsDTOs.UserMetrics getUserMetrics(User currentUser, LocalDateTime currentEnd, LocalDateTime previousEnd,
                                                   LocalDateTime currentStart, LocalDateTime previousStart) {
        if (currentUser.getRole() != Role.ADMIN) {
            return new MetricsDTOs.UserMetrics(
                    calculateMetric(0, 0), calculateMetric(0, 0),
                    calculateMetric(0, 0), calculateMetric(0, 0));
        }

        long currentTotal = userRepository.countTotalWithDate(currentEnd);
        long previousTotal = userRepository.countTotalWithDate(previousEnd);
        long currentActive = userRepository.countActiveWithDate(currentEnd);
        long previousActive = userRepository.countActiveWithDate(previousEnd);
        long currentNewUsers = userRepository.countByCreatedAtAfter(currentStart);
        long previousNewUsers = userRepository.countByCreatedAtAfter(previousStart);

        return new MetricsDTOs.UserMetrics(
                calculateMetric(currentTotal, previousTotal),
                calculateMetric(currentActive, previousActive),
                calculateMetric(currentTotal - currentActive, previousTotal - previousActive),
                calculateMetric(currentNewUsers, previousNewUsers));
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

        return new MetricsDTOs.MessageMetrics(
                calculateMetric(currentSent, previousSent),
                calculateMetric(currentReceived, previousReceived),
                calculateMetricDouble(currentResponseRate, previousResponseRate),
                getChannelMetrics(owner, currentStart, currentEnd, previousStart, previousEnd),
                calculateMetric(currentSent + currentReceived, previousSent + previousReceived));
    }

    private long getMessageCount(User owner, LocalDateTime start, LocalDateTime end, boolean isOutbound) {
        if (owner != null) {
            return isOutbound
                    ? messageRepository.countOutboundBySenderAndPeriod(owner, start, end)
                    : messageRepository.countInboundByContactOwnerAndPeriod(owner, start, end);
        }
        return isOutbound
                ? messageRepository.countOutboundMessagesInPeriod(start, end)
                : messageRepository.countInboundMessagesInPeriod(start, end);
    }

    private Map<String, MetricsDTOs.MetricValue> getChannelMetrics(User owner, LocalDateTime currentStart, LocalDateTime currentEnd,
                                                                   LocalDateTime previousStart, LocalDateTime previousEnd) {
        Map<String, Long> currentChannel = new LinkedHashMap<>();
        Map<String, Long> previousChannel = new LinkedHashMap<>();
        currentChannel.put("WHATSAPP", 0L);
        currentChannel.put("EMAIL", 0L);
        previousChannel.put("WHATSAPP", 0L);
        previousChannel.put("EMAIL", 0L);

        Function<User, List<Object[]>> currentFetcher = u -> u != null
                ? messageRepository.countOutboundByChannelAndSender(u, currentStart, currentEnd)
                : messageRepository.countOutboundByChannelInPeriod(currentStart, currentEnd);

        Function<User, List<Object[]>> previousFetcher = u -> u != null
                ? messageRepository.countOutboundByChannelAndSender(u, previousStart, previousEnd)
                : messageRepository.countOutboundByChannelInPeriod(previousStart, previousEnd);

        for (Object[] stat : currentFetcher.apply(owner)) {
            currentChannel.put(((Channel) stat[0]).name(), (Long) stat[1]);
        }
        for (Object[] stat : previousFetcher.apply(owner)) {
            previousChannel.put(((Channel) stat[0]).name(), (Long) stat[1]);
        }

        Map<String, MetricsDTOs.MetricValue> result = new LinkedHashMap<>();
        for (String channel : currentChannel.keySet()) {
            result.put(channel, calculateMetric(currentChannel.get(channel), previousChannel.get(channel)));
        }
        return result;
    }

    // ==================== MÉTRICAS SEPARADAS ====================

    public MetricsDTOs.ContactsMetricsResponse getContactsMetrics(User currentUser) {
        LocalDateTime currentEnd = LocalDateTime.now();
        return new MetricsDTOs.ContactsMetricsResponse(
                getFunnelMetrics(resolveOwner(currentUser, null), currentEnd, currentEnd.minusDays(DEFAULT_DAYS)));
    }

    public MetricsDTOs.MessagesMetricsResponse getMessagesMetrics(User currentUser) {
        LocalDateTime currentStart = LocalDateTime.now().minusDays(DEFAULT_DAYS);
        LocalDateTime currentEnd = LocalDateTime.now();
        return new MetricsDTOs.MessagesMetricsResponse(
                getMessageMetrics(resolveOwner(currentUser, null), currentStart, currentEnd,
                        currentStart.minusDays(DEFAULT_DAYS), currentStart.minusSeconds(1)));
    }

    public MetricsDTOs.TasksMetricsResponse getTasksMetrics(User currentUser) {
        LocalDateTime currentEnd = LocalDateTime.now();
        return new MetricsDTOs.TasksMetricsResponse(
                getTaskMetrics(resolveOwner(currentUser, null), currentEnd, currentEnd.minusDays(DEFAULT_DAYS)));
    }

    public MetricsDTOs.UsersMetricsResponse getUsersMetrics(User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            return new MetricsDTOs.UsersMetricsResponse(new MetricsDTOs.UserMetrics(
                    calculateMetric(0, 0), calculateMetric(0, 0), calculateMetric(0, 0), calculateMetric(0, 0)));
        }
        LocalDateTime currentEnd = LocalDateTime.now();
        return new MetricsDTOs.UsersMetricsResponse(new MetricsDTOs.UserMetrics(
                calculateMetric(userRepository.countTotalWithDate(currentEnd), userRepository.countTotalWithDate(currentEnd.minusDays(DEFAULT_DAYS))),
                calculateMetric(userRepository.countActiveWithDate(currentEnd), userRepository.countActiveWithDate(currentEnd.minusDays(DEFAULT_DAYS))),
                calculateMetric(0, 0),
                calculateMetric(userRepository.countByCreatedAtAfter(currentEnd.minusDays(DEFAULT_DAYS)),
                        userRepository.countByCreatedAtAfter(currentEnd.minusDays(DEFAULT_DAYS * 2)))));
    }

    // ==================== PANEL METRICS ====================

    public MetricsDTOs.PanelMetrics getPanelMetrics(User currentUser) {
        LocalDateTime currentEnd = LocalDateTime.now();
        LocalDateTime previousEnd = currentEnd.minusDays(DEFAULT_DAYS);
        User owner = resolveOwner(currentUser, null);

        Map<String, Long> currentContactStats = getFunnelStatsForPeriod(owner, currentEnd);
        Map<String, Long> previousContactStats = getFunnelStatsForPeriod(owner, previousEnd);

        long currentTotalContacts = currentContactStats.values().stream().mapToLong(Long::longValue).sum();
        long previousTotalContacts = previousContactStats.values().stream().mapToLong(Long::longValue).sum();

        LocalDateTime currentStart = currentEnd.minusDays(DEFAULT_DAYS);
        long currentTotalMessages = getMessageCount(owner, currentStart, currentEnd, true) + getMessageCount(owner, currentStart, currentEnd, false);
        long previousTotalMessages = getMessageCount(owner, currentStart.minusDays(DEFAULT_DAYS), currentStart.minusSeconds(1), true) +
                getMessageCount(owner, currentStart.minusDays(DEFAULT_DAYS), currentStart.minusSeconds(1), false);

        long currentUpcomingTasks = getTaskCountByStatusWithDate(owner, TaskStatus.PENDING, currentEnd) + getTaskDueTodayCount(owner);
        long previousUpcomingTasks = getTaskCountByStatusWithDate(owner, TaskStatus.PENDING, previousEnd);

        return new MetricsDTOs.PanelMetrics(
                calculateMetric(currentTotalContacts, previousTotalContacts),
                calculateMetric(currentTotalMessages, previousTotalMessages),
                calculateMetric(currentUpcomingTasks, previousUpcomingTasks));
    }

    // ==================== GLOBAL METRICS ====================

    public MetricsDTOs.GlobalMetricsResponse getGlobalMetrics(User currentUser) {
        LocalDateTime currentStart = LocalDateTime.now().minusDays(DEFAULT_DAYS);
        LocalDateTime currentEnd = LocalDateTime.now();
        LocalDateTime previousStart = currentStart.minusDays(DEFAULT_DAYS);
        LocalDateTime previousEnd = currentStart.minusSeconds(1);
        User owner = resolveOwner(currentUser, null);

        long currentConversations = getTotalConversations(owner, currentEnd);
        long previousConversations = getTotalConversations(owner, previousEnd);
        long currentSent = getMessageCount(owner, currentStart, currentEnd, true);
        long previousSent = getMessageCount(owner, previousStart, previousEnd, true);
        long currentReceived = getMessageCount(owner, currentStart, currentEnd, false);
        double currentResponseRate = currentSent > 0 ? Math.round((currentReceived * 100.0 / currentSent) * 10) / 10.0 : 0.0;
        double previousResponseRate = previousSent > 0 ? Math.round((getMessageCount(owner, previousStart, previousEnd, false) * 100.0 / previousSent) * 10) / 10.0 : 0.0;
        long currentCompletedTasks = getTaskCountByStatusWithDate(owner, TaskStatus.COMPLETED, currentEnd);
        long previousCompletedTasks = getTaskCountByStatusWithDate(owner, TaskStatus.COMPLETED, previousEnd);

        return new MetricsDTOs.GlobalMetricsResponse(
                calculateMetric(currentConversations, previousConversations),
                calculateMetricDouble(currentResponseRate, previousResponseRate),
                calculateMetric(currentCompletedTasks, previousCompletedTasks),
                getTopSalesperson(currentUser, currentStart, currentEnd));
    }

    private long getTotalConversations(User owner, LocalDateTime endDate) {
        if (owner != null) {
            return conversationRepository.findByAssignedTo(owner).stream()
                    .filter(c -> !c.getCreatedAt().isAfter(endDate))
                    .count();
        }
        return conversationRepository.countByCreatedAtBefore(endDate);
    }

    private MetricsDTOs.TopSalespersonInfo getTopSalesperson(User currentUser, LocalDateTime start, LocalDateTime end) {
        if (currentUser.getRole() != Role.ADMIN) return null;

        List<User> salespersons = userRepository.findByRole(Role.SALESPERSON);
        if (salespersons.isEmpty()) return null;

        // Una sola consulta que trae todo junto
        List<Object[]> results = messageRepository.countOutboundPerSenderInPeriod(start, end);

        if (results == null || results.isEmpty()) return null;

        // Crear mapa de mensajes enviados por vendedor
        Map<Long, Long> messagesBySeller = results.stream()
                .collect(Collectors.toMap(
                        r -> ((User) r[0]).getId(),
                        r -> (Long) r[1]
                ));

        // Calcular CLOSED_WON por vendedor
        Map<Long, Long> closedWonBySeller = new HashMap<>();
        for (User seller : salespersons) {
            long closedWon = contactRepository.countByOwnerAndFunnelStatus(seller, FunnelStatus.CLOSED_WON);
            closedWonBySeller.put(seller.getId(), closedWon);
        }

        // Encontrar el mejor vendedor (60% mensajes, 40% CLOSED_WON)
        User bestSeller = null;
        double bestScore = 0;
        long bestMessages = 0;

        // Calcular máximos para normalización
        long maxMessages = messagesBySeller.values().stream().max(Long::compare).orElse(1L);
        long maxClosedWon = closedWonBySeller.values().stream().max(Long::compare).orElse(1L);

        for (User seller : salespersons) {
            long messages = messagesBySeller.getOrDefault(seller.getId(), 0L);
            long closedWon = closedWonBySeller.getOrDefault(seller.getId(), 0L);

            // Score simplificado: 60% mensajes, 40% ventas cerradas
            double score = ((double) messages / maxMessages) * 0.6 +
                    ((double) closedWon / maxClosedWon) * 0.4;

            if (score > bestScore) {
                bestScore = score;
                bestSeller = seller;
                bestMessages = messages;
            }
        }

        if (bestSeller == null) return null;

        return new MetricsDTOs.TopSalespersonInfo(
                bestSeller.getId(),
                bestSeller.getName(),
                bestSeller.getEmail(),
                bestMessages,
                Math.round(bestScore * 100) / 100.0
        );
    }

    private double calculateSellerScore(SellerMetrics sm, long maxSent, long maxClosedWon, long maxNewLeads, long maxCompletedTasks, double maxResponseRate) {
        double responseRate = sm.sent() > 0 ? (sm.received() * 100.0 / sm.sent()) : 0;
        return ((double) sm.closedWon() / maxClosedWon) * 0.35 +
                ((double) sm.newLeads() / maxNewLeads) * 0.25 +
                ((double) sm.sent() / maxSent) * 0.20 +
                (responseRate / maxResponseRate) * 0.10 +
                ((double) sm.completedTasks() / maxCompletedTasks) * 0.10;
    }

    private record SellerMetrics(User seller, long sent, long received, long closedWon, long newLeads, long completedTasks) {}

    // ==================== MÉTODOS AUXILIARES ====================

    private User resolveOwner(User currentUser, String salespersonEmail) {
        if (currentUser.getRole() != Role.ADMIN) return currentUser;
        if (salespersonEmail == null || salespersonEmail.isEmpty()) return null;

        User owner = userRepository.findByEmail(salespersonEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor", salespersonEmail));
        if (owner.getRole() != Role.SALESPERSON) {
            throw new BusinessRuleViolationException("El usuario no es un vendedor");
        }
        return owner;
    }

    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.PeriodMetrics getPeriodMetrics(User currentUser, String period,
                                                      String startDate, String endDate) {
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
        String filename = "metrics_report_" + LocalDate.now() + "." + format;
        String data = format.equalsIgnoreCase("csv") ?
                generateCsvMetrics(dashboard, startDate, endDate, salespersonEmail) :
                generatePdfMetrics(dashboard, startDate, endDate, salespersonEmail);
        return new MetricsDTOs.ExportMetrics(data, filename, "text/csv; charset=UTF-8");
    }

    private String generateCsvMetrics(MetricsDTOs.DashboardMetrics dashboard,
                                      String startDate, String endDate, String salespersonEmail) {
        StringBuilder csv = new StringBuilder();
        csv.append("# REPORTE DE MÉTRICAS DEL CRM\n");
        csv.append("# Generado: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        csv.append("# Período: ").append(formatPeriod(startDate, endDate)).append("\n");
        if (salespersonEmail != null) csv.append("# Vendedor: ").append(salespersonEmail).append("\n");
        csv.append("\n=== RESUMEN GENERAL ===\n");
        csv.append("Métrica,Valor,Cambio %,Tendencia\n");
        appendCsvRow(csv, "Contactos activos", dashboard.funnel().totalActive());
        appendCsvRow(csv, "Tareas para hoy", dashboard.tasks().dueToday());
        appendCsvRow(csv, "Tareas vencidas", dashboard.tasks().overdue());
        return csv.toString();
    }

    private void appendCsvRow(StringBuilder csv, String label, MetricsDTOs.MetricValue metric) {
        csv.append(label).append(",")
                .append(metric.value()).append(",")
                .append(metric.changePercent()).append(",")
                .append(metric.trend()).append("\n");
    }

    private String generatePdfMetrics(MetricsDTOs.DashboardMetrics dashboard,
                                      String startDate, String endDate, String salespersonEmail) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document);
            addInfo(document, startDate, endDate, salespersonEmail);
            addSummaryTable(document, dashboard);

            document.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage(), e);
            throw new BusinessRuleViolationException("Error al generar el PDF: " + e.getMessage());
        }
    }

    private void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Reporte de Métricas CRM", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));
    }

    private void addInfo(Document document, String startDate, String endDate, String salespersonEmail) throws DocumentException {
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        document.add(new Paragraph("Generado: " + LocalDateTime.now().format(DATE_FORMATTER), infoFont));
        document.add(new Paragraph("Período: " + formatPeriod(startDate, endDate), infoFont));
        if (salespersonEmail != null) document.add(new Paragraph("Vendedor: " + salespersonEmail, infoFont));
        document.add(new Paragraph(" "));
    }

    private void addSummaryTable(Document document, MetricsDTOs.DashboardMetrics dashboard) throws DocumentException {
        addSectionHeader(document, "Resumen General");
        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        addMetricRow(summaryTable, "Contactos activos", dashboard.funnel().totalActive());
        addMetricRow(summaryTable, "Tareas para hoy", dashboard.tasks().dueToday());
        addMetricRow(summaryTable, "Tareas vencidas", dashboard.tasks().overdue());
        document.add(summaryTable);
    }

    private void addMetricRow(PdfPTable table, String label, MetricsDTOs.MetricValue metric) {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        String trendSymbol = switch (metric.trend()) {
            case "up" -> "↑";
            case "down" -> "↓";
            default -> "→";
        };

        table.addCell(new PdfPCell(new Phrase(label, normalFont)));
        table.addCell(new PdfPCell(new Phrase(String.valueOf(metric.value()), normalFont)));
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
        if (startDate == null && endDate == null) return "Últimos " + DEFAULT_DAYS + " días";
        return (startDate != null ? startDate : "inicio") + " a " + (endDate != null ? endDate : "hoy");
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now().minusDays(DEFAULT_DAYS).atStartOfDay();
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDateTime.now();
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);
    }

    // ==================== MÉTRICAS DE PLANTILLAS ====================

    public MetricsDTOs.TemplatesMetricsResponse getTemplatesMetrics(User currentUser) {
        log.info("📊 Generando métricas de plantillas para usuario: {}", currentUser.getEmail());

        LocalDateTime currentEnd = LocalDateTime.now();
        LocalDateTime previousEnd = currentEnd.minusDays(DEFAULT_DAYS);
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime previousMonthStart = monthStart.minusMonths(1);
        LocalDateTime previousMonthEnd = monthStart.minusSeconds(1);
        LocalDateTime thirtyDaysAgo = currentEnd.minusDays(DEFAULT_DAYS);
        LocalDateTime previousThirtyDaysAgo = thirtyDaysAgo.minusDays(DEFAULT_DAYS);

        User owner = resolveOwner(currentUser, null);

        long currentTotal = getTotalTemplates(owner, currentEnd);
        long previousTotal = getTotalTemplates(owner, previousEnd);
        long currentMonthTemplates = getTemplatesCreatedInPeriod(owner, monthStart, currentEnd);
        long previousMonthTemplates = getTemplatesCreatedInPeriod(owner, previousMonthStart, previousMonthEnd);
        long createdToday = getTemplatesCreatedToday(owner);
        long totalLast30Days = getTemplatesCreatedInPeriod(owner, thirtyDaysAgo, currentEnd);
        long totalPrevious30Days = getTemplatesCreatedInPeriod(owner, previousThirtyDaysAgo, thirtyDaysAgo);

        double currentDailyAverage = totalLast30Days / (double) DEFAULT_DAYS;
        double previousDailyAverage = totalPrevious30Days / (double) DEFAULT_DAYS;
        double currentDailyAverageRounded = Math.round(currentDailyAverage * 10) / 10.0;

        long previousMonthTotal = getTemplatesCreatedInPeriod(owner, previousMonthStart, previousMonthEnd);
        long daysInPreviousMonth = java.time.temporal.ChronoUnit.DAYS.between(previousMonthStart, previousMonthEnd) + 1;
        double previousDailyAverageForToday = daysInPreviousMonth > 0 ? (double) previousMonthTotal / daysInPreviousMonth : 0;
        long previousValueForToday = (long) previousDailyAverageForToday;

        return new MetricsDTOs.TemplatesMetricsResponse(
                new MetricsDTOs.TemplateMetrics(
                        calculateMetric(currentTotal, previousTotal),
                        calculateMetric(currentMonthTemplates, previousMonthTemplates),
                        calculateMetric(createdToday, previousValueForToday),
                        currentDailyAverageRounded,
                        calculateMetric((long) (currentDailyAverageRounded * 10), (long) (Math.round(previousDailyAverage * 10) / 10.0 * 10))
                ));
    }

    private long getTemplatesCreatedToday(User owner) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        if (owner != null && owner.getRole() == Role.SALESPERSON) {
            return templateRepository.countByCreatedByRoleAndCreatedAtBetween(Role.ADMIN, todayStart, todayEnd) +
                    templateRepository.countByCreatedByAndCreatedAtBetween(owner, todayStart, todayEnd);
        }
        return templateRepository.countByCreatedAtBetween(todayStart, todayEnd);
    }

    private long getTotalTemplates(User owner, LocalDateTime endDate) {
        if (owner != null && owner.getRole() == Role.SALESPERSON) {
            return templateRepository.countByCreatedByRoleAndCreatedAtBefore(Role.ADMIN, endDate) +
                    templateRepository.countByCreatedByAndCreatedAtBefore(owner, endDate);
        }
        return templateRepository.countByCreatedAtBefore(endDate);
    }

    private long getTemplatesCreatedInPeriod(User owner, LocalDateTime start, LocalDateTime end) {
        if (owner != null && owner.getRole() == Role.SALESPERSON) {
            return templateRepository.countByCreatedByRoleAndCreatedAtBetween(Role.ADMIN, start, end) +
                    templateRepository.countByCreatedByAndCreatedAtBetween(owner, start, end);
        }
        return templateRepository.countByCreatedAtBetween(start, end);
    }
}