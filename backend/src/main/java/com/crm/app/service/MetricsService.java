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

        // 1. Métricas de contactos por funnel
        MetricsDTOs.FunnelMetrics funnelMetrics = getFunnelMetrics(owner);

        // 2. Métricas de contactos específicos (negociación y cerrados)
        MetricsDTOs.ContactStatusMetrics contactStatusMetrics = getContactStatusMetrics(owner);

        // 3. Métricas de mensajes
        MetricsDTOs.MessageMetrics messageMetrics = getMessageMetrics(owner, start, end);

        // 4. Métricas de tareas (con tareas para hoy)
        MetricsDTOs.TaskMetrics taskMetrics = getTaskMetrics(owner);

        // 5. Métricas de usuarios (solo para ADMIN)
        MetricsDTOs.UserMetrics userMetrics = getUserMetrics(currentUser);

        String salespersonInfo = owner != null ? owner.getEmail() : null;

        return new MetricsDTOs.DashboardMetrics(
                funnelMetrics,
                contactStatusMetrics,
                messageMetrics,
                taskMetrics,
                userMetrics,
                periodDesc,
                salespersonInfo
        );
    }

    // ==================== MÉTRICAS DE CONTACTOS ESPECÍFICOS ====================

    private MetricsDTOs.ContactStatusMetrics getContactStatusMetrics(User owner) {
        long inNegotiation = 0;
        long proposalSent = 0;
        long closedWon = 0;
        long closedLost = 0;

        if (owner != null) {
            // Para un vendedor específico
            List<Object[]> stats = contactRepository.countByFunnelStatusAndOwner(owner);
            for (Object[] stat : stats) {
                FunnelStatus status = (FunnelStatus) stat[0];
                Long count = (Long) stat[1];
                switch (status) {
                    case IN_NEGOTIATION -> inNegotiation = count;
                    case PROPOSAL_SENT -> proposalSent = count;
                    case CLOSED_WON -> closedWon = count;
                    case CLOSED_LOST -> closedLost = count;
                    default -> {}
                }
            }
        } else {
            // Para todos los vendedores
            List<Object[]> stats = contactRepository.countByFunnelStatus();
            for (Object[] stat : stats) {
                FunnelStatus status = (FunnelStatus) stat[0];
                Long count = (Long) stat[1];
                switch (status) {
                    case IN_NEGOTIATION -> inNegotiation = count;
                    case PROPOSAL_SENT -> proposalSent = count;
                    case CLOSED_WON -> closedWon = count;
                    case CLOSED_LOST -> closedLost = count;
                    default -> {}
                }
            }
        }

        long totalClosed = closedWon + closedLost;
        double conversionRate = totalClosed > 0 ? Math.round((closedWon * 100.0 / totalClosed) * 10) / 10.0 : 0.0;

        return new MetricsDTOs.ContactStatusMetrics(inNegotiation, proposalSent, closedWon, closedLost, conversionRate);
    }

    // ==================== MÉTRICAS DE USUARIOS ====================

    private MetricsDTOs.UserMetrics getUserMetrics(User currentUser) {
        // Solo ADMIN puede ver métricas de usuarios
        if (currentUser.getRole() != Role.ADMIN) {
            return new MetricsDTOs.UserMetrics(0, 0, 0, 0);
        }

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long inactiveUsers = totalUsers - activeUsers;

        // Nuevos usuarios en los últimos 30 días
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newUsers = userRepository.countByCreatedAtAfter(thirtyDaysAgo);

        log.info("👥 Métricas de usuarios: total={}, activos={}, inactivos={}, nuevos={}",
                totalUsers, activeUsers, inactiveUsers, newUsers);

        return new MetricsDTOs.UserMetrics(totalUsers, activeUsers, inactiveUsers, newUsers);
    }

    // ==================== MÉTRICAS DE TAREAS (ACTUALIZADO) ====================

    private MetricsDTOs.TaskMetrics getTaskMetrics(User owner) {
        long completed, overdue, pending, dueToday;

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        if (owner != null) {
            completed = taskRepository.countByAssignedToAndStatus(owner, TaskStatus.COMPLETED);
            overdue = taskRepository.countByAssignedToAndStatus(owner, TaskStatus.OVERDUE);
            pending = taskRepository.countByAssignedToAndStatus(owner, TaskStatus.PENDING);
            dueToday = taskRepository.countByAssignedToAndDueDateBetween(owner, todayStart, todayEnd);
        } else {
            completed = taskRepository.countByStatus(TaskStatus.COMPLETED);
            overdue = taskRepository.countByStatus(TaskStatus.OVERDUE);
            pending = taskRepository.countByStatus(TaskStatus.PENDING);
            dueToday = taskRepository.countByDueDateBetween(todayStart, todayEnd);
        }

        return new MetricsDTOs.TaskMetrics(completed, overdue, pending, dueToday);
    }

    // ==================== MÉTRICAS DE CONTACTOS POR FUNNEL ====================

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

    // ==================== MÉTRICAS DE MENSAJES ====================

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

    // ==================== MÉTODOS EXISTENTES (sin cambios) ====================

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

        // 1. Resumen general
        csv.append("=== RESUMEN GENERAL ===\n");
        csv.append("Métrica,Valor\n");
        csv.append("Contactos activos,").append(dashboard.funnel().totalActive()).append("\n");
        csv.append("En negociación,").append(dashboard.contactStatus().inNegotiation()).append("\n");
        csv.append("Propuesta enviada,").append(dashboard.contactStatus().proposalSent()).append("\n");
        csv.append("Cerrados ganados,").append(dashboard.contactStatus().closedWon()).append("\n");
        csv.append("Cerrados perdidos,").append(dashboard.contactStatus().closedLost()).append("\n");
        csv.append("Tasa de conversión (%),").append(dashboard.contactStatus().conversionRate()).append("\n");
        csv.append("Mensajes enviados,").append(dashboard.messages().sent()).append("\n");
        csv.append("Mensajes recibidos,").append(dashboard.messages().received()).append("\n");
        csv.append("Tasa de respuesta (%),").append(dashboard.messages().responseRate()).append("\n");
        csv.append("Tareas completadas,").append(dashboard.tasks().completed()).append("\n");
        csv.append("Tareas vencidas,").append(dashboard.tasks().overdue()).append("\n");
        csv.append("Tareas pendientes,").append(dashboard.tasks().pending()).append("\n");
        csv.append("Tareas para hoy,").append(dashboard.tasks().dueToday()).append("\n");
        csv.append("Total usuarios,").append(dashboard.users().total()).append("\n");
        csv.append("Usuarios activos,").append(dashboard.users().active()).append("\n");
        csv.append("Usuarios inactivos,").append(dashboard.users().inactive()).append("\n");
        csv.append("Nuevos usuarios (30d),").append(dashboard.users().newUsers()).append("\n");
        csv.append("\n");

        return csv.toString();
    }

    private String generatePdfMetrics(MetricsDTOs.DashboardMetrics dashboard,
                                      String startDate, String endDate, String salespersonEmail) {
        // Implementación similar a la existente pero con los nuevos campos
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

            // Tabla resumen
            addSectionHeader(document, "Resumen General");
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            addSummaryRow(summaryTable, "Contactos activos", String.valueOf(dashboard.funnel().totalActive()));
            addSummaryRow(summaryTable, "En negociación", String.valueOf(dashboard.contactStatus().inNegotiation()));
            addSummaryRow(summaryTable, "Propuesta enviada", String.valueOf(dashboard.contactStatus().proposalSent()));
            addSummaryRow(summaryTable, "Cerrados ganados", String.valueOf(dashboard.contactStatus().closedWon()));
            addSummaryRow(summaryTable, "Cerrados perdidos", String.valueOf(dashboard.contactStatus().closedLost()));
            addSummaryRow(summaryTable, "Tasa de conversión", String.format("%.1f%%", dashboard.contactStatus().conversionRate()));
            addSummaryRow(summaryTable, "Mensajes enviados", String.valueOf(dashboard.messages().sent()));
            addSummaryRow(summaryTable, "Mensajes recibidos", String.valueOf(dashboard.messages().received()));
            addSummaryRow(summaryTable, "Tasa de respuesta", String.format("%.1f%%", dashboard.messages().responseRate()));
            addSummaryRow(summaryTable, "Tareas completadas", String.valueOf(dashboard.tasks().completed()));
            addSummaryRow(summaryTable, "Tareas vencidas", String.valueOf(dashboard.tasks().overdue()));
            addSummaryRow(summaryTable, "Tareas pendientes", String.valueOf(dashboard.tasks().pending()));
            addSummaryRow(summaryTable, "Tareas para hoy", String.valueOf(dashboard.tasks().dueToday()));
            addSummaryRow(summaryTable, "Total usuarios", String.valueOf(dashboard.users().total()));
            addSummaryRow(summaryTable, "Usuarios activos", String.valueOf(dashboard.users().active()));
            addSummaryRow(summaryTable, "Usuarios inactivos", String.valueOf(dashboard.users().inactive()));
            addSummaryRow(summaryTable, "Nuevos usuarios (30d)", String.valueOf(dashboard.users().newUsers()));

            document.add(summaryTable);
            document.close();

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage(), e);
            throw new BusinessRuleViolationException("Error al generar el PDF: " + e.getMessage());
        }
    }

    private void addSectionHeader(Document document, String title) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph header = new Paragraph(title, headerFont);
        header.setSpacingBefore(15);
        header.setSpacingAfter(5);
        document.add(header);
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBorder(Rectangle.BOX);
        labelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setBorder(Rectangle.BOX);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatPeriod(String startDate, String endDate) {
        if (startDate == null && endDate == null) {
            return "Últimos 365 días";
        }
        return (startDate != null ? startDate : "inicio") + " a " + (endDate != null ? endDate : "hoy");
    }

    private String generateFilename(String format) {
        String date = LocalDate.now().toString();
        return String.format("metrics_report_%s.%s", date, format);
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now().minusDays(365).atStartOfDay();
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