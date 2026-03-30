package com.crm.app.exception;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {

    private final String service;
    private final String providerId;

    public ExternalServiceException(String service, String message) {
        super(message);
        this.service = service;
        this.providerId = null;
    }

    public ExternalServiceException(String service, String message, Throwable cause) {
        super(message, cause);
        this.service = service;
        this.providerId = null;
    }

    public ExternalServiceException(String service, String providerId, String message) {
        super(message);
        this.service = service;
        this.providerId = providerId;
    }

}