package com.crm.app.util;

import com.crm.app.exception.InvalidPhoneNumberException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PhoneNumberNormalizer {

    private static final String ARGENTINE_PREFIX = "54";
    private static final int CORRECT_ARGENTINE_LENGTH = 12;
    private static final int LENGTH_WITH_9 = 13;
    private static final int LENGTH_WITH_EXTRA_9 = 14;

    /**
     * Normaliza un número de teléfono al formato que espera WhatsApp.
     *
     * @param phone Número de teléfono a normalizar
     * @return Número normalizado
     * @throws InvalidPhoneNumberException si el número es inválido
     */
    public String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new InvalidPhoneNumberException(phone, "El número de teléfono no puede estar vacío");
        }

        String digitsOnly = phone.replaceAll("[^0-9]", "");

        log.debug("📞 Normalizando teléfono: original={}, soloDigitos={}", phone, digitsOnly);

        if (digitsOnly.isEmpty()) {
            throw new InvalidPhoneNumberException(phone, "El número no contiene dígitos válidos");
        }

        // Normalización para Argentina
        if (digitsOnly.startsWith(ARGENTINE_PREFIX)) {
            String normalized = normalizeArgentineNumber(digitsOnly);
            log.info("✅ Número normalizado: {} -> {}", phone, normalized);
            return normalized;
        }

        // Para otros países, retornar solo dígitos
        log.info("✅ Número normalizado (sin cambios): {}", digitsOnly);
        return digitsOnly;
    }

    private String normalizeArgentineNumber(String digitsOnly) {
        // Caso: 549XXXXXXXXX (13 dígitos) -> 54XXXXXXXXXX
        if (digitsOnly.length() == LENGTH_WITH_9 && digitsOnly.startsWith("549")) {
            return ARGENTINE_PREFIX + digitsOnly.substring(3);
        }
        // Caso: 5499XXXXXXXXX (14 dígitos con 9 extra)
        if (digitsOnly.length() == LENGTH_WITH_EXTRA_9 && digitsOnly.startsWith("5499")) {
            return ARGENTINE_PREFIX + digitsOnly.substring(4);
        }
        // Caso: 54XXXXXXXXXX (12 dígitos, ya correcto)
        if (digitsOnly.length() == CORRECT_ARGENTINE_LENGTH) {
            return digitsOnly;
        }

        throw new InvalidPhoneNumberException(digitsOnly,
                String.format("Número argentino inválido. Longitud: %d, debe ser 12, 13 o 14 dígitos", digitsOnly.length()));
    }
}