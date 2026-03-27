package com.crm.app.exception;

public class ConversationClosedException extends RuntimeException {
    public ConversationClosedException(String message) {
        super(message);}

    public ConversationClosedException(Long conversationId) {
        super(String.format("La conversación con id %d está cerrada y no acepta nuevos mensajes", conversationId));
    }
}
