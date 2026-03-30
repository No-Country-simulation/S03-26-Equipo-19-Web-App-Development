package com.crm.app.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s no encontrado con id: %d", resource, id));
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s no encontrado con identificador: %s", resource, identifier));
    }
}
