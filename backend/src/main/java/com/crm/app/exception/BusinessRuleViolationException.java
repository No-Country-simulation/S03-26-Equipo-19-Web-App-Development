package com.crm.app.exception;

public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public BusinessRuleViolationException(String rule, String details) {
        super(String.format("Regla de negocio violada: %s - %s", rule, details));
    }
}
