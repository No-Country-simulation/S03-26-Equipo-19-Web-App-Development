package com.crm.app.exception;

public class InvalidPhoneNumberException extends RuntimeException {

    private final String originalNumber;
    private final String reason;

    public InvalidPhoneNumberException(String originalNumber, String reason) {
        super(String.format("Número de teléfono inválido: %s - %s", originalNumber, reason));
        this.originalNumber = originalNumber;
        this.reason = reason;
    }

    public InvalidPhoneNumberException(String originalNumber, String reason, Throwable cause) {
        super(String.format("Número de teléfono inválido: %s - %s", originalNumber, reason), cause);
        this.originalNumber = originalNumber;
        this.reason = reason;
    }

    public String getOriginalNumber() { return originalNumber; }
    public String getReason() { return reason; }
}