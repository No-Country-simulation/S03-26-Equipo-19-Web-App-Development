package com.crm.app.exception;

public class TokenExpiredException extends RuntimeException {

    private final String service;

    public TokenExpiredException(String service) {
        super(String.format("Token expirado para el servicio: %s. Genera un nuevo token en Meta Developers.", service));
        this.service = service;
    }

    public String getService() { return service; }
}