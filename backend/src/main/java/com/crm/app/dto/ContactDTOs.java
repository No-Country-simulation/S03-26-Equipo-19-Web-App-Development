package com.crm.app.dto;

import com.crm.app.model.enums.Channel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ContactDTOs {

    @Schema(description = "Datos básicos de contacto")
    public record ContactBase(

            @Schema(example = "Juan", description = "Nombre del contacto (opcional en creación automática)")
            String name,

            @Schema(example = "Pérez", description = "Apellido del contacto (opcional)")
            String lastName,

            @Email
            @Schema(example = "juan@gmail.com")
            String email,

            @Schema(example = "5491123456789", description = "Formato E.164 sin '+' (ej: 5491123456789)")
            String phone,

            @Schema(example = "Tech Solutions")
            String company
    ) {
        public boolean hasAtLeastOneIdentifier() {
            return (name != null && !name.isBlank()) ||
                    (lastName != null && !lastName.isBlank()) ||
                    (email != null && !email.isBlank()) ||
                    (phone != null && !phone.isBlank());
        }

        // Normalizar teléfono al formato E.164 (solo dígitos, sin '+', sin '9' extra)
        public String getNormalizedPhone() {
            if (phone == null) return null;
            return normalizePhoneNumber(phone);
        }

        private String normalizePhoneNumber(String phone) {
            if (phone == null) return null;

            // Eliminar todo excepto dígitos
            String digitsOnly = phone.replaceAll("[^0-9]", "");

            // Argentina: eliminar el '9' después del código de país '54'
            // Formato correcto: 54 + código área + número (11 dígitos totales)
            // Si tiene 549... (13 dígitos), eliminar el 9: 549XXXXXXXXX -> 54XXXXXXXXX
            if (digitsOnly.startsWith("549") && digitsOnly.length() == 13) {
                String normalized = "54" + digitsOnly.substring(3);
                log.info("📱 Número argentino normalizado: {} -> {}", digitsOnly, normalized);
                return normalized;
            }

            // Si tiene 5499... (14 dígitos con 9 extra), eliminar el 9
            if (digitsOnly.startsWith("5499") && digitsOnly.length() == 14) {
                String normalized = "54" + digitsOnly.substring(4);
                log.info("📱 Número argentino normalizado (con 9 extra): {} -> {}", digitsOnly, normalized);
                return normalized;
            }

            // Si ya tiene el formato correcto (54 + 10 dígitos = 12 total)
            if (digitsOnly.startsWith("54") && digitsOnly.length() == 12) {
                log.info("📱 Número argentino ya en formato correcto: {}", digitsOnly);
                return digitsOnly;
            }

            // Para otros países o formatos, solo retornar dígitos
            return digitsOnly;
        }
    }

    @Schema(description = "Creación de contacto")
    public record CreateContactRequest(

            @NotNull
            ContactBase contact,

            @Schema(example = "Instagram Ads")
            String source,

            Channel preferredChannel,

            @Schema(description = "Solo Admin puede setearlo")
            Long ownerId
    ) {}
}