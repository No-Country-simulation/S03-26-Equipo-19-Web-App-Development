package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.ConversationStatus;
import com.crm.app.model.enums.FunnelStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class ContactDTOs {

    // ==================== DTOs EXISTENTES ====================

    @Schema(description = "Datos básicos de contacto")
    public record ContactBase(
            @Schema(example = "Juan") String name,
            @Schema(example = "Pérez") String lastName,
            @Email @Schema(example = "juan@gmail.com") String email,
            @Schema(example = "5491123456789") String phone,
            @Schema(example = "Tech Solutions") String company
    ) {
        public boolean hasAtLeastOneIdentifier() {
            return (name != null && !name.isBlank()) ||
                    (lastName != null && !lastName.isBlank()) ||
                    (email != null && !email.isBlank()) ||
                    (phone != null && !phone.isBlank());
        }
    }

    @Schema(description = "Creación de contacto")
    public record CreateContactRequest(
            @NotNull ContactBase contact,
            Channel preferredChannel,
            @Schema(description = "Opcional. Solo Admin puede setearlo") Long ownerId
    ) {}

    @Schema(description = "Respuesta completa de contacto (con tags)")
    public record ContactDetailResponse(
            Long id,
            String name,
            String lastName,
            String email,
            String phone,
            String company,
            FunnelStatus funnelStatus,
            Channel preferredChannel,
            OwnerInfo owner,
            List<TagInfo> tags,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    @Schema(description = "Respuesta resumida de contacto (sin tags)")
    public record ContactSummaryResponse(
            Long id,
            String name,
            String lastName,
            String email,
            String phone,
            String company,
            FunnelStatus funnelStatus,
            Channel preferredChannel,
            OwnerInfo owner,
            LocalDateTime createdAt
    ) {}

    @Schema(description = "Información resumida del vendedor")
    public record OwnerInfo(
            Long id,
            String name,
            String email
    ) {}

    @Schema(description = "Información resumida de la etiqueta")
    public record TagInfo(
            Long id,
            String name,
            String color
    ) {}

    // ==================== NUEVOS DTOs PARA DASHBOARD DE CONTACTOS ====================

    @Schema(description = "Información de conversación para el dashboard de contactos")
    public record ConversationBriefInfo(
            Long id,
            Channel channel,
            ConversationStatus status,
            LocalDateTime lastInteraction,
            @Schema(description = "Cantidad de mensajes NO LEÍDOS en esta conversación")
            Long unreadCount
    ) {}

    @Schema(description = "Dashboard de contacto con métricas y conversaciones")
    public record ContactDashboardResponse(
            Long id,
            String name,
            String lastName,
            String email,
            String phone,
            String company,
            FunnelStatus funnelStatus,
            Channel preferredChannel,
            OwnerInfo owner,
            List<TagInfo> tags,
            LocalDateTime createdAt,
            List<ConversationBriefInfo> conversations,
            @Schema(description = "Total de mensajes NO LEÍDOS de este contacto (todas las conversaciones)")
            Long totalUnreadCount
    ) {}

    // ==================== MÉTRICAS GLOBALES ====================

    @Schema(description = "Métricas globales del dashboard")
    public record DashboardMetrics(
            @Schema(description = "Total de contactos del usuario")
            Long totalContacts,

            @Schema(description = "Total de mensajes NO LEÍDOS en todas las conversaciones del usuario")
            Long totalUnreadMessages,

            @Schema(description = "Desglose de no leídos por canal")
            UnreadByChannel unreadByChannel
    ) {}

    @Schema(description = "Desglose de mensajes no leídos por canal")
    public record UnreadByChannel(
            @Schema(description = "Mensajes no leídos en WhatsApp")
            Long whatsapp,

            @Schema(description = "Mensajes no leídos en Email")
            Long email
    ) {}

    // ==================== RESPUESTA FINAL DEL DASHBOARD ====================

    @Schema(description = "Respuesta completa del dashboard de contactos")
    public record ContactDashboardListResponse(
            DashboardMetrics metrics,
            List<ContactDashboardResponse> contacts
    ) {}
}