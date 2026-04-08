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

    // ==================== EXPORTACIÓN MEJORADA ====================

    @PreAuthorize("isAuthenticated()")
    public MetricsDTOs.ExportMetrics exportMetrics(User currentUser, String salespersonEmail,
                                                   String startDate, String endDate, String format) {
        log.info("📊 Exportando métricas en formato {} para usuario: {}", format, currentUser.getEmail());

        if (!format.equalsIgnoreCase("csv") && !format.equalsIgnoreCase("pdf")) {
            throw new BusinessRuleViolationException("Formato no soportado. Use 'csv' o 'pdf'");
        }

        var dashboard = getDashboardMetrics(currentUser, salespersonEmail, startDate, endDate);
        String filename = generateFilename(format);
        String contentType = format.equalsIgnoreCase("csv") ? "text/csv; charset=UTF-8" : "application/pdf";
        String data;

        if (format.equalsIgnoreCase("csv")) {
            data = generateCsvMetrics(dashboard, startDate, endDate, salespersonEmail);
        } else {
            data = generatePdfMetrics(dashboard, startDate, endDate, salespersonEmail);
        }

        return new MetricsDTOs.ExportMetrics(data, filename, contentType);
    }

    // ==================== GENERACIÓN CSV ====================

    private String generateCsvMetrics(MetricsDTOs.DashboardMetrics dashboard,
                                      String startDate, String endDate, String salespersonEmail) {
        StringBuilder csv = new StringBuilder();

        // Encabezado del reporte
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
        csv.append("Mensajes enviados,").append(dashboard.messages().sent()).append("\n");
        csv.append("Mensajes recibidos,").append(dashboard.messages().received()).append("\n");
        csv.append("Tasa de respuesta (%),").append(dashboard.messages().responseRate()).append("\n");
        csv.append("Tareas completadas,").append(dashboard.tasks().completed()).append("\n");
        csv.append("Tareas vencidas,").append(dashboard.tasks().overdue()).append("\n");
        csv.append("Tareas pendientes,").append(dashboard.tasks().pending()).append("\n");
        csv.append("\n");

        // 2. Contactos por estado del funnel
        csv.append("=== CONTACTOS POR ESTADO DEL FUNNEL ===\n");
        csv.append("Estado,Cantidad,Porcentaje\n");
        long totalContacts = dashboard.funnel().byStatus().values().stream().mapToLong(Long::longValue).sum();
        for (var entry : dashboard.funnel().byStatus().entrySet()) {
            double percentage = totalContacts > 0 ? (entry.getValue() * 100.0 / totalContacts) : 0;
            csv.append(entry.getKey()).append(",")
                    .append(entry.getValue()).append(",")
                    .append(String.format("%.1f", percentage)).append("%\n");
        }
        csv.append("\n");

        // 3. Mensajes por canal
        csv.append("=== MENSAJES POR CANAL ===\n");
        csv.append("Canal,Cantidad,Porcentaje\n");
        long totalMessages = dashboard.messages().byChannel().values().stream().mapToLong(Long::longValue).sum();
        for (var entry : dashboard.messages().byChannel().entrySet()) {
            double percentage = totalMessages > 0 ? (entry.getValue() * 100.0 / totalMessages) : 0;
            csv.append(entry.getKey()).append(",")
                    .append(entry.getValue()).append(",")
                    .append(String.format("%.1f", percentage)).append("%\n");
        }

        return csv.toString();
    }

    // ==================== GENERACIÓN PDF ====================

    private String generatePdfMetrics(MetricsDTOs.DashboardMetrics dashboard,
                                      String startDate, String endDate, String salespersonEmail) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título principal
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte de Métricas CRM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Información del reporte
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            document.add(new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), infoFont));
            document.add(new Paragraph("Período: " + formatPeriod(startDate, endDate), infoFont));
            if (salespersonEmail != null) {
                document.add(new Paragraph("Vendedor: " + salespersonEmail, infoFont));
            }
            document.add(new Paragraph(" "));

            // 1. Resumen General
            addSectionHeader(document, "Resumen General");
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{60f, 40f});

            addSummaryRow(summaryTable, "Contactos activos", String.valueOf(dashboard.funnel().totalActive()));
            addSummaryRow(summaryTable, "Mensajes enviados", String.valueOf(dashboard.messages().sent()));
            addSummaryRow(summaryTable, "Mensajes recibidos", String.valueOf(dashboard.messages().received()));
            addSummaryRow(summaryTable, "Tasa de respuesta", String.format("%.1f%%", dashboard.messages().responseRate()));
            addSummaryRow(summaryTable, "Tareas completadas", String.valueOf(dashboard.tasks().completed()));
            addSummaryRow(summaryTable, "Tareas vencidas", String.valueOf(dashboard.tasks().overdue()));
            addSummaryRow(summaryTable, "Tareas pendientes", String.valueOf(dashboard.tasks().pending()));

            document.add(summaryTable);
            document.add(new Paragraph(" "));

            // 2. Contactos por estado del funnel
            addSectionHeader(document, "Contactos por Estado del Funnel");
            PdfPTable funnelTable = new PdfPTable(3);
            funnelTable.setWidthPercentage(100);
            funnelTable.setWidths(new float[]{50f, 25f, 25f});

            addTableHeader(funnelTable, "Estado", "Cantidad", "Porcentaje");

            long totalContacts = dashboard.funnel().byStatus().values().stream().mapToLong(Long::longValue).sum();
            for (var entry : dashboard.funnel().byStatus().entrySet()) {
                double percentage = totalContacts > 0 ? (entry.getValue() * 100.0 / totalContacts) : 0;
                addTableCell(funnelTable, entry.getKey());
                addTableCell(funnelTable, String.valueOf(entry.getValue()));
                addTableCell(funnelTable, String.format("%.1f%%", percentage));
            }
            document.add(funnelTable);
            document.add(new Paragraph(" "));

            // 3. Mensajes por canal
            addSectionHeader(document, "Mensajes por Canal");
            PdfPTable channelTable = new PdfPTable(3);
            channelTable.setWidthPercentage(100);
            channelTable.setWidths(new float[]{50f, 25f, 25f});

            addTableHeader(channelTable, "Canal", "Cantidad", "Porcentaje");

            long totalMessages = dashboard.messages().byChannel().values().stream().mapToLong(Long::longValue).sum();
            for (var entry : dashboard.messages().byChannel().entrySet()) {
                double percentage = totalMessages > 0 ? (entry.getValue() * 100.0 / totalMessages) : 0;
                addTableCell(channelTable, entry.getKey());
                addTableCell(channelTable, String.valueOf(entry.getValue()));
                addTableCell(channelTable, String.format("%.1f%%", percentage));
            }
            document.add(channelTable);

            document.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            log.error("Error generando PDF de métricas: {}", e.getMessage(), e);
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

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String text) {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(text, normalFont));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    // ==================== MÉTODOS AUXILIARES ====================

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

}