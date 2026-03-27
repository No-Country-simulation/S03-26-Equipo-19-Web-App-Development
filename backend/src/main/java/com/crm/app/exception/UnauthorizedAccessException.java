package com.crm.app.exception;

public class UnauthorizedAccessException extends RuntimeException {

  public UnauthorizedAccessException(String message) {
    super(message);
  }

  public UnauthorizedAccessException(String resource, Long id) {
    super(String.format("No tienes acceso a %s con id: %d", resource, id));
  }
}
