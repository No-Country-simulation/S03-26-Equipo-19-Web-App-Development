package com.crm.app.exception;

public class WebhookVerificationException extends RuntimeException {

    private final String expectedToken;
    private final String receivedToken;

    public WebhookVerificationException(String expectedToken, String receivedToken) {
        super(String.format("Verificación de webhook fallida. Token esperado: %s, recibido: %s",
                expectedToken, receivedToken));
        this.expectedToken = expectedToken;
        this.receivedToken = receivedToken;
    }

    public String getExpectedToken() { return expectedToken; }
    public String getReceivedToken() { return receivedToken; }
}