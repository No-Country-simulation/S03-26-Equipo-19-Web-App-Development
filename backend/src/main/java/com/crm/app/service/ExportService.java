package com.crm.app.service;

import com.crm.app.dto.ExportDTOs;
import com.crm.app.exception.BusinessRuleViolationException;
import com.crm.app.exception.ResourceNotFoundException;
import com.crm.app.exception.UnauthorizedAccessException;
import com.crm.app.model.*;
import com.crm.app.model.enums.Role;
import com.crm.app.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== EXPORTACIÓN PRINCIPAL ====================

    @PreAuthorize("isAuthenticated()")
    public ExportDTOs.ExportResponse exportData(ExportDTOs.ExportRequest request, User currentUser) {
        log.info("📁 Exportando {} en formato {} para usuario: {}",
                request.entityType(), request.format(), currentUser.getEmail());

        // Validar formato
        if (!request.format().equalsIgnoreCase("csv") && !request.format().equalsIgnoreCase("pdf")) {
            throw new BusinessRuleViolationException("Formato no soportado. Use 'csv' o 'pdf'");
        }

        // Obtener datos según el tipo de entidad
        List<?> data = getDataByEntityType(request, currentUser);

        if (data.isEmpty()) {
            throw new BusinessRuleViolationException("No hay datos para exportar con los filtros seleccionados");
        }

        // Generar CSV o PDF
        String filename = generateFilename(request.entityType(), request.format());
        String contentType = request.format().equalsIgnoreCase("csv") ? "text/csv" : "application/pdf";
        String dataEncoded = generateExportData(data, request.entityType(), request.format());

        return new ExportDTOs.ExportResponse(dataEncoded, filename, contentType);
    }

    // ==================== OBTENCIÓN DE DATOS SEGÚN ENTIDAD ====================

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
        // Si hay una vista guardada, aplicarla
        if (request.savedViewId() != null) {
            SavedView savedView = savedViewRepository.findById(request.savedViewId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vista guardada", request.savedViewId()));

            // Verificar acceso a la vista
            if (!savedView.isGlobal() && !savedView.getUser().getId().equals(currentUser.getId())) {
                throw new UnauthorizedAccessException("No tienes acceso a esta vista");
            }

            // Aquí aplicarías los filtros de la vista (simplificado)
            log.info("Aplicando vista guardada: {}", savedView.getName());
        }

        // Aplicar filtros de rol
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

    // ==================== GENERACIÓN DE CSV ====================

    private String generateExportData(List<?> data, String entityType, String format) {
        if (format.equalsIgnoreCase("csv")) {
            return generateCsv(data, entityType);
        }
        // Para PDF, retornaríamos Base64 del PDF (simplificado)
        return generateCsv(data, entityType); // Placeholder
    }

    private String generateCsv(List<?> data, String entityType) {
        StringBuilder csv = new StringBuilder();

        // Cabeceras según tipo de entidad
        csv.append(getCsvHeaders(entityType)).append("\n");

        // Datos
        for (Object item : data) {
            csv.append(getCsvRow(item, entityType)).append("\n");
        }

        return csv.toString();
    }

    private String getCsvHeaders(String entityType) {
        return switch (entityType) {
            case "contacts" -> "ID,Nombre,Apellido,Email,Teléfono,Empresa,Estado,Funnel,Source,Creado,Actualizado";
            case "conversations" -> "ID,Contacto,Canal,Estado,Asignado,Última Interacción,Creado";
            case "messages" -> "ID,Conversación,Dirección,Contenido,Estado,Provider ID,Enviado";
            case "tasks" -> "ID,Título,Descripción,Tipo,Estado,Fecha Vencimiento,Contacto,Asignado";
            default -> "ID,Datos";
        };
    }

    private String getCsvRow(Object item, String entityType) {
        return switch (entityType) {
            case "contacts" -> formatContactRow((Contact) item);
            case "conversations" -> formatConversationRow((Conversation) item);
            case "messages" -> formatMessageRow((Message) item);
            case "tasks" -> formatTaskRow((Task) item);
            default -> "";
        };
    }

    private String formatContactRow(Contact c) {
        return String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%s,%s,%s",
                c.getId(),
                escapeCsv(c.getName()),
                escapeCsv(c.getLastName()),
                escapeCsv(c.getEmail()),
                escapeCsv(c.getPhone()),
                escapeCsv(c.getCompany()),
                c.getFunnelStatus(),
                c.getSource() != null ? c.getSource() : "",
                c.getCreatedAt() != null ? c.getCreatedAt().format(DATE_FORMATTER) : "",
                c.getUpdatedAt() != null ? c.getUpdatedAt().format(DATE_FORMATTER) : ""
        );
    }

    private String formatConversationRow(Conversation conv) {
        return String.format("%d,%d,%s,%s,%d,%s,%s",
                conv.getId(),
                conv.getContact().getId(),
                conv.getChannel(),
                conv.getStatus(),
                conv.getAssignedTo().getId(),
                conv.getLastInteraction() != null ? conv.getLastInteraction().format(DATE_FORMATTER) : "",
                conv.getCreatedAt().format(DATE_FORMATTER)
        );
    }

    private String formatMessageRow(Message m) {
        return String.format("%d,%d,%s,\"%s\",%s,%s,%s",
                m.getId(),
                m.getConversation().getId(),
                m.getDirection(),
                escapeCsv(m.getBody()),
                m.getDeliveryStatus(),
                m.getProviderId() != null ? m.getProviderId() : "",
                m.getSentAt().format(DATE_FORMATTER)
        );
    }

    private String formatTaskRow(Task t) {
        return String.format("%d,\"%s\",\"%s\",%s,%s,%s,%d,%d",
                t.getId(),
                escapeCsv(t.getTitle()),
                escapeCsv(t.getDescription()),
                t.getType(),
                t.getStatus(),
                t.getDueDate().format(DATE_FORMATTER),
                t.getContact().getId(),
                t.getAssignedTo().getId()
        );
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    // ==================== GENERACIÓN DE NOMBRE DE ARCHIVO ====================

    private String generateFilename(String entityType, String format) {
        String date = LocalDate.now().toString();
        return String.format("%s_%s.%s", entityType, date, format);
    }
}