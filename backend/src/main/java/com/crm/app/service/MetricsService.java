package com.crm.app.service;

import com.crm.app.dto.MetricsDTOs;
import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.model.Conversation;
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
import java.util.concurrent.ConcurrentHashMap;
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

    private static final List<FunnelStatus> ACTIVE_STATUSES = Arrays.asList(
            FunnelStatus.NEW_LEAD,
            FunnelStatus.CONTACTED,
            FunnelStatus.IN_NEGOTIATION,
            FunnelStatus.PROPOSAL_SENT
    );

    // Cache para almacenar resultados de períodos
    private final Map<String, Map<String, Long>> funnelStatsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> taskCountCache = new ConcurrentHashMap<>();
    private final Map<String, Long> messageCountCache = new ConcurrentHashMap<>();

    // ==================== MÉTODOS DE CÁLCULO DE TENDENCIA ====================

    private MetricsDTOs.MetricValue calculateMetric(long currentValue, long previousValue) {
        double changePercent = 0;
        String trend = "stable";

        if (previousValue > 0) {
            changePercent = ((currentValue - previousValue) * 100.0) / previousValue;
            changePercent = Math.round(changePercent * 10) / 10.0;
        } else if (currentValue > 0 && previousValue == 0) {
            changePercent = 100;
            trend = "up";
        } else if (currentValue == 0 && previousValue > 0) {
            changePercent = -100;
            trend = "down";
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

    // ==================== MÉTODOS OPTIMIZADOS CON CACHÉ INTERNO ====================

    private String getCacheKey(User owner, LocalDateTime endDate) {
        Long ownerId = owner != null ? owner.getId() : 0;
        return ownerId + "_" + endDate.toLocalDate().toString();
    }

    private Map<String, Long> getFunnelStatsForPeriodCached(User owner, LocalDateTime endDate) {
        String key = getCacheKey(owner, endDate);
        return funnelStatsCache.computeIfAbsent(key, k -> {
            Map<String, Long> stats = new HashMap<>();
            for (FunnelStatus status : FunnelStatus.values()) {
                stats.put(status.name(), 0L);
            }
            List<Object[]> results = contactRepository.countByFunnelStatusWithDate(owner, endDate);
            for (Object[] stat : results) {
                stats.put(((FunnelStatus) stat[0]).name(), (Long) stat[1]);
            }
            return stats;
        });
    }

    private long getTaskCountCached(User owner, TaskStatus status, LocalDateTime endDate, String type) {
        String key = getCacheKey(owner, endDate) + "_" + status + "_" + type;
        return taskCountCache.computeIfAbsent(key, k -> {
            if ("overdue".equals(type)) {
                return getTaskOverdueCountWithDate(owner, endDate);
            }
            return getTaskCountByStatusWithDate(owner, status, endDate);
        });
    }

    // ==================== DASHBOARD PRINCIPAL ====================

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

        // Obtener todas las métricas en paralelo usando caché
        MetricsDTOs.FunnelMetrics funnelMetrics = getFunnelMetrics(owner, currentEnd, previousEnd);
        MetricsDTOs.TaskMetrics taskMetrics = getTaskMetrics(owner, currentEnd, previousEnd);
        MetricsDTOs.UserMetrics userMetrics = getUserMetrics(currentUser, currentEnd, previousEnd, currentStart, previousStart);
        MetricsDTOs.MessageMetrics messageMetrics = getMessageMetrics(owner, currentStart, currentEnd, previousStart, previousEnd);

        String salespersonInfo = owner != null ? owner.getEmail() : null;

        return new MetricsDTOs.DashboardMetrics(
                funnelMetrics, null, messageMetrics, taskMetrics, userMetrics, periodDesc, salespersonInfo
        );
    }

    // ==================== MÉTRICAS DE CONTACTOS ====================

    private MetricsDTOs.FunnelMetrics getFunnelMetrics(User owner, LocalDateTime currentEnd, LocalDateTime previousEnd) {
        Map<String, Long> currentStats = getFunnelStatsForPeriodCached(owner, currentEnd);
        Map<String, Long> previousStats = getFunnelStatsForPeriodCached(owner, previousEnd);

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

    // ==================== MÉTRICAS DE TAREAS ====================

    private MetricsDTOs.TaskMetrics getTaskMetrics(User owner, LocalDateTime currentEnd, LocalDateTime previousEnd) {
        long currentCompleted = getTaskCountCached(owner, TaskStatus.COMPLETED, currentEnd, "completed");
        long previousCompleted = getTaskCountCached(owner, TaskStatus.COMPLETED, previousEnd, "completed");

        long currentOverdue = getTaskCountCached(owner, TaskStatus.OVERDUE, currentEnd, "overdue");
        long previousOverdue = getTaskCountCached(owner, TaskStatus.OVERDUE, previousEnd, "overdue");

        long currentPending = getTaskCountCached(owner, TaskStatus.PENDING, currentEnd, "pending");
        long previousPending = getTaskCountCached(owner, TaskStatus.PENDING, previousEnd, "pending");

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
            return isOutbound ? messageRepository.countOutboundBySenderAndPeriod(owner, start, end)
                    : messageRepository.countInboundByContactOwnerAndPeriod(owner, start, end);
        }
        return isOutbound ? messageRepository.countOutboundMessagesInPeriod(start, end)
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

        Function<User, List<Object[]>> currentFetcher = u -> u != null ?
                messageRepository.countOutboundByChannelAndSender(u, currentStart, currentEnd) :
                messageRepository.countOutboundByChannelInPeriod(currentStart, currentEnd);

        Function<User, List<Object[]>> previousFetcher = u -> u != null ?
                messageRepository.countOutboundByChannelAndSender(u, previousStart, previousEnd) :
                messageRepository.countOutboundByChannelInPeriod(previousStart, previousEnd);

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
                getFunnelMetrics(resolveOwner(currentUser, null), currentEnd, currentEnd.minusDays(30)));
    }

    public MetricsDTOs.MessagesMetricsResponse getMessagesMetrics(User currentUser) {
        LocalDateTime currentStart = LocalDateTime.now().minusDays(30);
        LocalDateTime currentEnd = LocalDateTime.now();
        return new MetricsDTOs.MessagesMetricsResponse(
                getMessageMetrics(resolveOwner(currentUser, null), currentStart, currentEnd,
                        currentStart.minusDays(30), currentStart.minusSeconds(1)));
    }

    public MetricsDTOs.TasksMetricsResponse getTasksMetrics(User currentUser) {
        LocalDateTime currentEnd = LocalDateTime.now();
        return new MetricsDTOs.TasksMetricsResponse(
                getTaskMetrics(resolveOwner(currentUser, null), currentEnd, currentEnd.minusDays(30)));
    }

    public MetricsDTOs.UsersMetricsResponse getUsersMetrics(User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            return new MetricsDTOs.UsersMetricsResponse(new MetricsDTOs.UserMetrics(
                    calculateMetric(0, 0), calculateMetric(0, 0), calculateMetric(0, 0), calculateMetric(0, 0)));
        }
        LocalDateTime currentEnd = LocalDateTime.now();
        LocalDateTime previousEnd = currentEnd.minusDays(30);
        LocalDateTime currentStart = currentEnd.minusDays(30);
        LocalDateTime previousStart = currentStart.minusDays(30);

        return new MetricsDTOs.UsersMetricsResponse(new MetricsDTOs.UserMetrics(
                calculateMetric(userRepository.countTotalWithDate(currentEnd), userRepository.countTotalWithDate(previousEnd)),
                calculateMetric(userRepository.countActiveWithDate(currentEnd), userRepository.countActiveWithDate(previousEnd)),
                calculateMetric(0, 0),
                calculateMetric(userRepository.countByCreatedAtAfter(currentStart), userRepository.countByCreatedAtAfter(previousStart))));
    }

    // ==================== PANEL METRICS ====================

    public MetricsDTOs.PanelMetrics getPanelMetrics(User currentUser) {
        LocalDateTime currentEnd = LocalDateTime.now();
        LocalDateTime previousEnd = currentEnd.minusDays(30);
        User owner = resolveOwner(currentUser, null);

        Map<String, Long> currentContactStats = getFunnelStatsForPeriodCached(owner, currentEnd);
        Map<String, Long> previousContactStats = getFunnelStatsForPeriodCached(owner, previousEnd);

        long currentTotalContacts = currentContactStats.values().stream().mapToLong(Long::longValue).sum();
        long previousTotalContacts = previousContactStats.values().stream().mapToLong(Long::longValue).sum();

        LocalDateTime currentStart = currentEnd.minusDays(30);
        long currentTotalMessages = getMessageCount(owner, currentStart, currentEnd, true) + getMessageCount(owner, currentStart, currentEnd, false);
        long previousTotalMessages = getMessageCount(owner, currentStart.minusDays(30), currentStart.minusSeconds(1), true) +
                getMessageCount(owner, currentStart.minusDays(30), currentStart.minusSeconds(1), false);

        long currentUpcomingTasks = getTaskCountCached(owner, TaskStatus.PENDING, currentEnd, "pending") + getTaskDueTodayCount(owner);
        long previousUpcomingTasks = getTaskCountCached(owner, TaskStatus.PENDING, previousEnd, "pending");

        return new MetricsDTOs.PanelMetrics(
                calculateMetric(currentTotalContacts, previousTotalContacts),
                calculateMetric(currentTotalMessages, previousTotalMessages),
                calculateMetric(currentUpcomingTasks, previousUpcomingTasks));
    }

    // ==================== GLOBAL METRICS ====================

    public MetricsDTOs.GlobalMetricsResponse getGlobalMetrics(User currentUser) {
        LocalDateTime currentStart = LocalDateTime.now().minusDays(30);
        LocalDateTime currentEnd = LocalDateTime.now();
        LocalDateTime previousStart = currentStart.minusDays(30);
        LocalDateTime previousEnd = currentStart.minusSeconds(1);
        User owner = resolveOwner(currentUser, null);

        long currentConversations = getTotalConversations(owner, currentEnd);
        long previousConversations = getTotalConversations(owner, previousEnd);

        long currentSent = getMessageCount(owner, currentStart, currentEnd, true);
        long previousSent = getMessageCount(owner, previousStart, previousEnd, true);
        long currentReceived = getMessageCount(owner, currentStart, currentEnd, false);
        double currentResponseRate = currentSent > 0 ? Math.round((currentReceived * 100.0 / currentSent) * 10) / 10.0 : 0.0;
        double previousResponseRate = previousSent > 0 ? Math.round((getMessageCount(owner, previousStart, previousEnd, false) * 100.0 / previousSent) * 10) / 10.0 : 0.0;

        long currentCompletedTasks = getTaskCountCached(owner, TaskStatus.COMPLETED, currentEnd, "completed");
        long previousCompletedTasks = getTaskCountCached(owner, TaskStatus.COMPLETED, previousEnd, "completed");

        return new MetricsDTOs.GlobalMetricsResponse(
                calculateMetric(currentConversations, previousConversations),
                calculateMetricDouble(currentResponseRate, previousResponseRate),
                calculateMetric(currentCompletedTasks, previousCompletedTasks),
                getTopSalesperson(currentUser, currentStart, currentEnd));
    }

    private long getTotalConversations(User owner, LocalDateTime endDate) {
        if (owner != null) {
            return conversationRepository.findByAssignedTo(owner).stream()
                    .filter(c -> c.getCreatedAt().isBefore(endDate) || c.getCreatedAt().isEqual(endDate))
                    .count();
        }
        return conversationRepository.countByCreatedAtBefore(endDate);
    }

    private MetricsDTOs.TopSalespersonInfo getTopSalesperson(User currentUser, LocalDateTime start, LocalDateTime end) {
        if (currentUser.getRole() != Role.ADMIN) return null;

        List<User> salespersons = userRepository.findByRole(Role.SALESPERSON);
        if (salespersons.isEmpty()) return null;

        // Calcular métricas para todos los vendedores en una sola pasada
        List<SellerMetrics> sellerMetricsList = salespersons.parallelStream().map(seller -> {
            long sent = messageRepository.countOutboundBySenderAndPeriod(seller, start, end);
            long received = messageRepository.countInboundByContactOwnerAndPeriod(seller, start, end);
            return new SellerMetrics(seller, sent, received,
                    contactRepository.countByOwnerAndFunnelStatus(seller, FunnelStatus.CLOSED_WON),
                    contactRepository.countByOwnerAndFunnelStatus(seller, FunnelStatus.NEW_LEAD),
                    taskRepository.countByAssignedToAndStatus(seller, TaskStatus.COMPLETED));
        }).collect(Collectors.toList());

        // Calcular máximos para normalización
        long maxSent = sellerMetricsList.stream().mapToLong(SellerMetrics::sent).max().orElse(1);
        long maxClosedWon = sellerMetricsList.stream().mapToLong(SellerMetrics::closedWon).max().orElse(1);
        long maxNewLeads = sellerMetricsList.stream().mapToLong(SellerMetrics::newLeads).max().orElse(1);
        long maxCompletedTasks = sellerMetricsList.stream().mapToLong(SellerMetrics::completedTasks).max().orElse(1);
        double maxResponseRate = sellerMetricsList.stream()
                .mapToDouble(sm -> sm.sent() > 0 ? (sm.received() * 100.0 / sm.sent()) : 0)
                .max().orElse(1);

        // Calcular scores y encontrar el mejor
        Object[] best = sellerMetricsList.stream()
                .map(sm -> {
                    double responseRate = sm.sent() > 0 ? (sm.received() * 100.0 / sm.sent()) : 0;
                    double score = ((double) sm.closedWon() / maxClosedWon) * 0.35 +
                            ((double) sm.newLeads() / maxNewLeads) * 0.25 +
                            ((double) sm.sent() / maxSent) * 0.20 +
                            (responseRate / maxResponseRate) * 0.10 +
                            ((double) sm.completedTasks() / maxCompletedTasks) * 0.10;
                    return new Object[]{sm.seller(), score, sm.sent()};
                })
                .max(Comparator.comparingDouble(o -> (double) o[1]))
                .orElse(null);

        if (best == null) return null;

        User bestSeller = (User) best[0];
        double bestScore = (double) best[1];
        long bestMessages = (long) best[2];

        return new MetricsDTOs.TopSalespersonInfo(bestSeller.getId(), bestSeller.getName(),
                bestSeller.getEmail(), bestMessages, Math.round(bestScore * 100) / 100.0);
    }

    private record SellerMetrics(User seller, long sent, long received, long closedWon, long newLeads, long completedTasks) {}

    // ==================== MÉTODOS AUXILIARES ====================

    private User resolveOwner(User currentUser, String salespersonEmail) {
        if (currentUser.getRole() != Role.ADMIN) return currentUser;
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
        funnelStatsCache.clear();
        taskCountCache.clear();
        messageCountCache.clear();
        log.info("🗑️ Caché de métricas limpiado");
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
        csv.append("# Generado: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        csv.append("# Período: ").append(formatPeriod(startDate, endDate)).append("\n");
        if (salespersonEmail != null) csv.append("# Vendedor: ").append(salespersonEmail).append("\n");
        csv.append("\n=== RESUMEN GENERAL ===\n");
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
            if (salespersonEmail != null) document.add(new Paragraph("Vendedor: " + salespersonEmail, infoFont));
            document.add(new Paragraph(" "));

            addSectionHeader(document);
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

    private void addSectionHeader(Document document) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph header = new Paragraph("Resumen General", headerFont);
        header.setSpacingBefore(15);
        header.setSpacingAfter(5);
        document.add(header);
    }

    private String formatPeriod(String startDate, String endDate) {
        if (startDate == null && endDate == null) return "Últimos 30 días";
        return (startDate != null ? startDate : "inicio") + " a " + (endDate != null ? endDate : "hoy");
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return LocalDate.now().minusDays(30).atStartOfDay();
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return LocalDateTime.now();
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).atTime(23, 59, 59);
    }

    public MetricsDTOs.TemplatesMetricsResponse getTemplatesMetrics(User currentUser) {
        log.info("📊 Generando métricas de plantillas para usuario: {}", currentUser.getEmail());

        LocalDateTime currentEnd = LocalDateTime.now();
        LocalDateTime previousEnd = currentEnd.minusDays(30);

        // Calcular fechas para hoy
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // Calcular fechas para este mes
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime monthEnd = LocalDateTime.now();
        LocalDateTime previousMonthStart = monthStart.minusMonths(1);
        LocalDateTime previousMonthEnd = monthStart.minusSeconds(1);

        // Calcular fecha 30 días atrás
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime previousThirtyDaysAgo = thirtyDaysAgo.minusDays(30);

        User owner = resolveOwner(currentUser, null);

        // 1. Total de plantillas (vs hace 30 días)
        long currentTotal = getTotalTemplates(owner, currentEnd);
        long previousTotal = getTotalTemplates(owner, previousEnd);
        MetricsDTOs.MetricValue total = calculateMetric(currentTotal, previousTotal);

        // 2. Plantillas creadas este mes (vs mes anterior)
        long currentMonthTemplates = getTemplatesCreatedInPeriod(owner, monthStart, monthEnd);
        long previousMonthTemplates = getTemplatesCreatedInPeriod(owner, previousMonthStart, previousMonthEnd);
        MetricsDTOs.MetricValue createdThisMonth = calculateMetric(currentMonthTemplates, previousMonthTemplates);

        // 3. Plantillas creadas hoy (vs ayer o promedio diario)
        long createdToday = getTemplatesCreatedToday(owner, todayStart, todayEnd);

        // Calcular promedio diario del mes anterior para comparación realista
        long previousMonthTotal = getTemplatesCreatedInPeriod(owner, previousMonthStart, previousMonthEnd);
        long daysInPreviousMonth = java.time.temporal.ChronoUnit.DAYS.between(previousMonthStart, previousMonthEnd) + 1;
        double previousDailyAverage = daysInPreviousMonth > 0 ? (double) previousMonthTotal / daysInPreviousMonth : 0;

        // Para un cambio realista (ej: 1.5%), el valor anterior debe ser similar
        // Si createdToday = 4 y previousDailyAverage = 3.94, entonces cambio ≈ 1.5%
        long previousValueForToday = (long) previousDailyAverage;
        MetricsDTOs.MetricValue createdTodayMetric = calculateMetric(createdToday, previousValueForToday);

        // 4. Promedio diario (últimos 30 días vs período anterior)
        long totalLast30Days = getTemplatesCreatedInPeriod(owner, thirtyDaysAgo, LocalDateTime.now());
        long totalPrevious30Days = getTemplatesCreatedInPeriod(owner, previousThirtyDaysAgo, thirtyDaysAgo);

        double currentDailyAverage = totalLast30Days / 30.0;
        double previousDailyAverage30 = totalPrevious30Days / 30.0;

        // Redondear a 1 decimal
        double currentDailyAverageRounded = Math.round(currentDailyAverage * 10) / 10.0;
        double previousDailyAverageRounded = Math.round(previousDailyAverage30 * 10) / 10.0;

        // Calcular cambio porcentual para dailyAverage
        double dailyChangePercent = 0;
        String dailyTrend = "stable";
        if (previousDailyAverageRounded > 0) {
            dailyChangePercent = ((currentDailyAverageRounded - previousDailyAverageRounded) * 100.0) / previousDailyAverageRounded;
            dailyChangePercent = Math.round(dailyChangePercent * 10) / 10.0;
        } else if (currentDailyAverageRounded > 0) {
            dailyChangePercent = 100;
        }

        if (dailyChangePercent > 0) dailyTrend = "up";
        else if (dailyChangePercent < 0) dailyTrend = "down";
        else dailyTrend = "stable";

        MetricsDTOs.MetricValue dailyAverageMetric = new MetricsDTOs.MetricValue(
                (long) (currentDailyAverageRounded * 10), // 0.8 → 8
                dailyChangePercent,
                dailyTrend
        );

        log.info("✅ Templates metrics: total={} ({}%), thisMonth={} ({}%), today={} ({}%), dailyAvg={} ({})",
                currentTotal, total.changePercent(),
                currentMonthTemplates, createdThisMonth.changePercent(),
                createdToday, createdTodayMetric.changePercent(),
                currentDailyAverageRounded, dailyChangePercent);

        return new MetricsDTOs.TemplatesMetricsResponse(
                new MetricsDTOs.TemplateMetrics(total, createdThisMonth, createdTodayMetric, currentDailyAverageRounded, dailyAverageMetric)
        );
    }

    // Método auxiliar con 3 parámetros
    private long getTemplatesCreatedToday(User owner, LocalDateTime todayStart, LocalDateTime todayEnd) {
        if (owner != null && owner.getRole() == Role.SALESPERSON) {
            return templateRepository.countByCreatedByRoleAndCreatedAtBetween(Role.ADMIN, todayStart, todayEnd) +
                    templateRepository.countByCreatedByAndCreatedAtBetween(owner, todayStart, todayEnd);
        }
        return templateRepository.countByCreatedAtBetween(todayStart, todayEnd);
    }

    // En MetricsService.java - Agrega/actualiza estos métodos

    private long getTotalTemplates(User owner, LocalDateTime endDate) {
        if (owner != null && owner.getRole() == Role.SALESPERSON) {
            // Vendedor ve: globales (de ADMIN) + sus personales
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