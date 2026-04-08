package com.crm.app.service;

import com.crm.app.dto.ExportDTOs;
import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.UnauthorizedAccessException;
import com.crm.app.model.*;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportService {

    private final ContactRepository contactRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final TaskRepository taskRepository;
    private final SavedViewRepository savedViewRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);

    @PreAuthorize("isAuthenticated()")
    public ExportDTOs.ExportResponse exportData(ExportDTOs.ExportRequest request, User currentUser) {
        log.info("📁 Exportando {} en formato {} para usuario: {}",
                request.entityType(), request.format(), currentUser.getEmail());

        if (!request.format().equalsIgnoreCase("csv") && !request.format().equalsIgnoreCase("pdf")) {
            throw new BusinessRuleViolationException("Formato no soportado. Use 'csv' o 'pdf'");
        }

        List<?> data = getDataByEntityType(request, currentUser);

        if (data.isEmpty()) {
            throw new BusinessRuleViolationException("No hay datos para exportar con los filtros seleccionados");
        }

        String filename = generateFilename(request.entityType(), request.format());
        String contentType = request.format().equalsIgnoreCase("csv") ? "text/csv" : "application/pdf";
        String dataEncoded;

        if (request.format().equalsIgnoreCase("csv")) {
            dataEncoded = generateCsv(data, request.entityType());
        } else {
            dataEncoded = generatePdf(data, request.entityType());
        }

        return new ExportDTOs.ExportResponse(dataEncoded, filename, contentType);
    }

    // ==================== CSV ====================

    private String generateCsv(List<?> data, String entityType) {
        StringBuilder csv = new StringBuilder();
        csv.append(getCsvHeaders(entityType)).append("\n");

        for (Object item : data) {
            csv.append(getCsvRow(item, entityType)).append("\n");
        }

        return csv.toString();
    }

    // ==================== PDF ====================

    private String generatePdf(List<?> data, String entityType) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Exportación de " + capitalize(entityType), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Fecha de exportación
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph date = new Paragraph("Generado: " + LocalDate.now().toString(), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph(" "));

            // Tabla
            PdfPTable table = createTable(entityType, data);
            document.add(table);
            document.close();

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            log.error("Error generando PDF: {}", e.getMessage(), e);
            throw new BusinessRuleViolationException("Error al generar el PDF: " + e.getMessage());
        }
    }

    private PdfPTable createTable(String entityType, List<?> data) {
        PdfPTable table;

        switch (entityType) {
            case "contacts":
                table = new PdfPTable(12);
                addHeaderCell(table, "ID", "Nombre", "Apellido", "Email", "Teléfono", "Empresa",
                        "Estado", "Source", "Creado", "Actualizado", "Owner ID", "Owner");
                for (Object item : data) {
                    Contact c = (Contact) item;
                    addCell(table,
                            String.valueOf(c.getId()),
                            c.getName() != null ? c.getName() : "",
                            c.getLastName() != null ? c.getLastName() : "",
                            c.getEmail() != null ? c.getEmail() : "",
                            c.getPhone() != null ? c.getPhone() : "",
                            c.getCompany() != null ? c.getCompany() : "",
                            c.getFunnelStatus() != null ? c.getFunnelStatus().name() : "",
                            c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FORMATTER) : "",
                            c.getUpdatedAt() != null ? c.getUpdatedAt().format(DATE_FORMATTER) : "",
                            c.getOwner() != null ? String.valueOf(c.getOwner().getId()) : "",
                            c.getOwner() != null ? c.getOwner().getName() : ""
                    );
                }
                break;

            case "conversations":
                table = new PdfPTable(9);
                addHeaderCell(table, "ID", "Contacto ID", "Contacto", "Canal", "Estado",
                        "Asignado ID", "Asignado", "Última Interacción", "Creado");
                for (Object item : data) {
                    Conversation conv = (Conversation) item;
                    addCell(table,
                            String.valueOf(conv.getId()),
                            String.valueOf(conv.getContact().getId()),
                            conv.getContact().getName(),
                            conv.getChannel() != null ? conv.getChannel().name() : "",
                            conv.getStatus() != null ? conv.getStatus().name() : "",
                            String.valueOf(conv.getAssignedTo().getId()),
                            conv.getAssignedTo().getName(),
                            conv.getLastInteraction() != null ? conv.getLastInteraction().format(DATE_FORMATTER) : "",
                            conv.getCreatedAt().format(DATE_FORMATTER)
                    );
                }
                break;

            case "messages":
                table = new PdfPTable(7);
                addHeaderCell(table, "ID", "Conversación", "Dirección", "Contenido", "Estado", "Provider ID", "Enviado");
                for (Object item : data) {
                    Message m = (Message) item;
                    addCell(table,
                            String.valueOf(m.getId()),
                            String.valueOf(m.getConversation().getId()),
                            m.getDirection() != null ? m.getDirection().name() : "",
                            m.getBody() != null ? m.getBody() : "",
                            m.getDeliveryStatus() != null ? m.getDeliveryStatus().name() : "",
                            m.getProviderId() != null ? m.getProviderId() : "",
                            m.getSentAt().format(DATE_FORMATTER)
                    );
                }
                break;

            case "tasks":
                table = new PdfPTable(10);
                addHeaderCell(table, "ID", "Título", "Descripción", "Tipo", "Estado",
                        "Fecha Vencimiento", "Contacto ID", "Contacto", "Asignado ID", "Asignado");
                for (Object item : data) {
                    Task t = (Task) item;
                    addCell(table,
                            String.valueOf(t.getId()),
                            t.getTitle(),
                            t.getDescription() != null ? t.getDescription() : "",
                            t.getType() != null ? t.getType().name() : "",
                            t.getStatus() != null ? t.getStatus().name() : "",
                            t.getDueDate().format(DATE_FORMATTER),
                            String.valueOf(t.getContact().getId()),
                            t.getContact().getName(),
                            String.valueOf(t.getAssignedTo().getId()),
                            t.getAssignedTo().getName()
                    );
                }
                break;

            default:
                table = new PdfPTable(1);
                addHeaderCell(table, "Datos");
        }

        return table;
    }

    private void addHeaderCell(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addCell(PdfPTable table, String... values) {
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, NORMAL_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private List<?> getDataByEntityType(ExportDTOs.ExportRequest request, User currentUser) {
        return switch (request.entityType().toLowerCase()) {
            case "contacts" -> getContactsForExport(request, currentUser);
            case "conversations" -> getConversationsForExport(currentUser);
            case "messages" -> getMessagesForExport(currentUser);
            case "tasks" -> getTasksForExport(currentUser);
            default -> throw new BusinessRuleViolationException("Tipo de entidad no soportada: " + request.entityType());
        };
    }

    private List<Contact> getContactsForExport(ExportDTOs.ExportRequest request, User currentUser) {
        if (request.savedViewId() != null) {
            SavedView savedView = savedViewRepository.findById(request.savedViewId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vista guardada", request.savedViewId()));

            if (!savedView.isGlobal() && !savedView.getUser().getId().equals(currentUser.getId())) {
                throw new UnauthorizedAccessException("No tienes acceso a esta vista");
            }
            log.info("Aplicando vista guardada: {}", savedView.getName());
        }

        if (currentUser.getRole() == Role.ADMIN) {
            return contactRepository.findAll();
        } else {
            return contactRepository.findByOwner(currentUser);
        }
    }

    private List<Conversation> getConversationsForExport(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return conversationRepository.findAll();
        } else {
            return conversationRepository.findByAssignedTo(currentUser);
        }
    }

    private List<Message> getMessagesForExport(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return messageRepository.findAll();
        } else {
            return messageRepository.findBySender(currentUser);
        }
    }

    private List<Task> getTasksForExport(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return taskRepository.findAll();
        } else {
            return taskRepository.findByAssignedTo(currentUser);
        }
    }

    // ==================== CSV HELPERS ====================

    private String getCsvHeaders(String entityType) {
        return switch (entityType) {
            case "contacts" -> "ID,Nombre,Apellido,Email,Teléfono,Empresa,Estado,Source,Creado,Actualizado,Owner ID,Owner";
            case "conversations" -> "ID,ContactoID,ContactoNombre,Canal,Estado,AsignadoID,AsignadoNombre,ÚltimaInteracción,Creado";
            case "messages" -> "ID,ConversaciónID,Dirección,Contenido,Estado,ProviderID,Enviado";
            case "tasks" -> "ID,Título,Descripción,Tipo,Estado,FechaVencimiento,ContactoID,ContactoNombre,AsignadoID,AsignadoNombre";
            default -> "ID,Datos";
        };
    }

    private String getCsvRow(Object item, String entityType) {
        return switch (entityType) {
            case "contacts" -> formatContactRowCsv((Contact) item);
            case "conversations" -> formatConversationRowCsv((Conversation) item);
            case "messages" -> formatMessageRowCsv((Message) item);
            case "tasks" -> formatTaskRowCsv((Task) item);
            default -> "";
        };
    }

    private String formatContactRowCsv(Contact c) {
        return String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,%s,%d,\"%s\"",
                c.getId(),
                escapeCsv(c.getName()),
                escapeCsv(c.getLastName()),
                escapeCsv(c.getEmail()),
                escapeCsv(c.getPhone()),
                escapeCsv(c.getCompany()),
                c.getFunnelStatus() != null ? c.getFunnelStatus().name() : "",
                c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FORMATTER) : "",
                c.getUpdatedAt() != null ? c.getUpdatedAt().format(DATE_FORMATTER) : "",
                c.getOwner() != null ? c.getOwner().getId() : 0,
                escapeCsv(c.getOwner() != null ? c.getOwner().getName() : "")
        );
    }

    private String formatConversationRowCsv(Conversation conv) {
        return String.format("%d,%d,\"%s\",%s,%s,%d,\"%s\",%s,%s",
                conv.getId(),
                conv.getContact().getId(),
                escapeCsv(conv.getContact().getName()),
                conv.getChannel() != null ? conv.getChannel().name() : "",
                conv.getStatus() != null ? conv.getStatus().name() : "",
                conv.getAssignedTo().getId(),
                escapeCsv(conv.getAssignedTo().getName()),
                conv.getLastInteraction() != null ? conv.getLastInteraction().format(DATE_FORMATTER) : "",
                conv.getCreatedAt().format(DATE_FORMATTER)
        );
    }

    private String formatMessageRowCsv(Message m) {
        return String.format("%d,%d,%s,\"%s\",%s,%s,%s",
                m.getId(),
                m.getConversation().getId(),
                m.getDirection() != null ? m.getDirection().name() : "",
                escapeCsv(m.getBody()),
                m.getDeliveryStatus() != null ? m.getDeliveryStatus().name() : "",
                m.getProviderId() != null ? m.getProviderId() : "",
                m.getSentAt().format(DATE_FORMATTER)
        );
    }

    private String formatTaskRowCsv(Task t) {
        return String.format("%d,\"%s\",\"%s\",%s,%s,%s,%d,\"%s\",%d,\"%s\"",
                t.getId(),
                escapeCsv(t.getTitle()),
                escapeCsv(t.getDescription()),
                t.getType() != null ? t.getType().name() : "",
                t.getStatus() != null ? t.getStatus().name() : "",
                t.getDueDate().format(DATE_FORMATTER),
                t.getContact().getId(),
                escapeCsv(t.getContact().getName()),
                t.getAssignedTo().getId(),
                escapeCsv(t.getAssignedTo().getName())
        );
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String generateFilename(String entityType, String format) {
        String date = LocalDate.now().toString();
        return String.format("%s_%s.%s", entityType, date, format);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}