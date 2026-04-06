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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    // ==================== DASHBOARD PRINCIPAL ====================

    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.DashboardMetrics getDashboardMetrics(User currentUser, String salespersonEmail,
                                                            String startDate, String endDate) {
        log.info("📊 Generando métricas para usuario: {} (rol={})", currentUser.getEmail(), currentUser.getRole());

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);
        String periodDesc = formatPeriod(startDate, endDate);

        // Determinar owner según rol
        User owner = resolveOwner(currentUser, salespersonEmail);

        // 1. Métricas de contactos
        MetricsDTOs.FunnelMetrics funnelMetrics = getFunnelMetrics(owner);

        // 2. Métricas de mensajes
        MetricsDTOs.MessageMetrics messageMetrics = getMessageMetrics(owner, start, end);

        // 3. Métricas de tareas
        MetricsDTOs.TaskMetrics taskMetrics = getTaskMetrics(owner);

        String salespersonInfo = owner != null ? owner.getEmail() : null;

        return new MetricsDTOs.DashboardMetrics(funnelMetrics, messageMetrics, taskMetrics, periodDesc, salespersonInfo);
    }

    private User resolveOwner(User currentUser, String salespersonEmail) {
        if (currentUser.getRole() != Role.ADMIN) {
            // Vendedor solo ve sus propios datos
            return currentUser;
        }

        // Admin puede filtrar por vendedor
        if (salespersonEmail != null && !salespersonEmail.isEmpty()) {
            User owner = userRepository.findByEmail(salespersonEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendedor", salespersonEmail));
            if (owner.getRole() != Role.SALESPERSON) {
                throw new BusinessRuleViolationException("El usuario no es un vendedor");
            }
            return owner;
        }
        return null; // null = todos los vendedores
    }

    private MetricsDTOs.FunnelMetrics getFunnelMetrics(User owner) {
        List<Object[]> stats;
        if (owner != null) {
            stats = contactRepository.countByFunnelStatusAndOwner(owner);
        } else {
            stats = contactRepository.countByFunnelStatus();
        }

        Map<String, Long> byStatus = new LinkedHashMap<>();
        long totalActive = 0;

        for (Object[] stat : stats) {
            String status = ((FunnelStatus) stat[0]).name();
            Long count = (Long) stat[1];
            byStatus.put(status, count);
            if (ACTIVE_STATUSES.contains(FunnelStatus.valueOf(status))) {
                totalActive += count;
            }
        }

        return new MetricsDTOs.FunnelMetrics(byStatus, totalActive);
    }

    private MetricsDTOs.MessageMetrics getMessageMetrics(User owner, LocalDateTime start, LocalDateTime end) {
        long sent, received;
        Map<String, Long> byChannel = new LinkedHashMap<>();

        if (owner != null) {
            sent = messageRepository.countOutboundBySenderAndPeriod(owner, start, end);
            received = messageRepository.countInboundByContactOwnerAndPeriod(owner, start, end);

            List<Object[]> channelStats = messageRepository.countOutboundByChannelAndSender(owner, start, end);
            for (Object[] stat : channelStats) {
                byChannel.put(((Channel) stat[0]).name(), (Long) stat[1]);
            }
        } else {
            sent = messageRepository.countOutboundMessagesInPeriod(start, end);
            received = messageRepository.countInboundMessagesInPeriod(start, end);

            List<Object[]> channelStats = messageRepository.countOutboundByChannelInPeriod(start, end);
            for (Object[] stat : channelStats) {
                byChannel.put(((Channel) stat[0]).name(), (Long) stat[1]);
            }
        }

        double responseRate = sent > 0 ? Math.round((received * 100.0 / sent) * 10) / 10.0 : 0.0;

        return new MetricsDTOs.MessageMetrics(sent, received, responseRate, byChannel);
    }

    private MetricsDTOs.TaskMetrics getTaskMetrics(User owner) {
        long completed, overdue, pending;

        if (owner != null) {
            completed = taskRepository.countByAssignedToAndStatus(owner, TaskStatus.COMPLETED);
            overdue = taskRepository.countByAssignedToAndStatus(owner, TaskStatus.OVERDUE);
            pending = taskRepository.countByAssignedToAndStatus(owner, TaskStatus.PENDING);
        } else {
            completed = taskRepository.countByStatus(TaskStatus.COMPLETED);
            overdue = taskRepository.countByStatus(TaskStatus.OVERDUE);
            pending = taskRepository.countByStatus(TaskStatus.PENDING);
        }

        return new MetricsDTOs.TaskMetrics(completed, overdue, pending);
    }

    // ==================== MÉTRICAS POR PERÍODO ====================

    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.PeriodMetrics getPeriodMetrics(User currentUser, String period,
                                                      String startDate, String endDate) {
        log.info("📊 Generando métricas por período: {} - {} a {}", period, startDate, endDate);

        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        long messagesSent, messagesReceived, newContacts;

        if (currentUser.getRole() != Role.ADMIN) {
            // Vendedor solo ve sus datos
            messagesSent = messageRepository.countOutboundBySenderAndPeriod(currentUser, start, end);
            messagesReceived = messageRepository.countInboundByContactOwnerAndPeriod(currentUser, start, end);
            newContacts = contactRepository.countNewContactsInPeriod(start, end); // Esto necesita filtro por owner
        } else {
            messagesSent = messageRepository.countOutboundMessagesInPeriod(start, end);
            messagesReceived = messageRepository.countInboundMessagesInPeriod(start, end);
            newContacts = contactRepository.countNewContactsInPeriod(start, end);
        }

        return new MetricsDTOs.PeriodMetrics(period, startDate, endDate, messagesSent, messagesReceived, newContacts);
    }

    // ==================== EXPORTACIÓN ====================

    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.ExportMetrics exportMetrics(User currentUser, String salespersonEmail,
                                                   String startDate, String endDate) {
        var dashboard = getDashboardMetrics(currentUser, salespersonEmail, startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("Métrica,Valor\n");
        csv.append("Contactos activos,").append(dashboard.funnel().totalActive()).append("\n");
        csv.append("Mensajes enviados,").append(dashboard.messages().sent()).append("\n");
        csv.append("Mensajes recibidos,").append(dashboard.messages().received()).append("\n");
        csv.append("Tasa de respuesta (%),").append(dashboard.messages().responseRate()).append("\n");
        csv.append("Tareas completadas,").append(dashboard.tasks().completed()).append("\n");
        csv.append("Tareas vencidas,").append(dashboard.tasks().overdue()).append("\n");
        csv.append("Tareas pendientes,").append(dashboard.tasks().pending()).append("\n");
        csv.append("\nContactos por estado del funnel\n");
        csv.append("Estado,Cantidad\n");
        for (var entry : dashboard.funnel().byStatus().entrySet()) {
            csv.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
        csv.append("\nMensajes por canal\n");
        csv.append("Canal,Cantidad\n");
        for (var entry : dashboard.messages().byChannel().entrySet()) {
            csv.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }

        String filename = "metrics_" + LocalDate.now().toString() + ".csv";
        return new MetricsDTOs.ExportMetrics(csv.toString(), filename);
    }

    // ==================== MÉTODOS AUXILIARES ====================

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

    private String formatPeriod(String startDate, String endDate) {
        if (startDate == null && endDate == null) {
            return "Últimos 30 días";
        }
        return (startDate != null ? startDate : "inicio") + " a " + (endDate != null ? endDate : "hoy");
    }
}