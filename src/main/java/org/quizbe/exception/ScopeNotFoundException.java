package org.quizbe.exception;

public class ScopeNotFoundException extends RuntimeException {

  public ScopeNotFoundException() {
    super();
  }

  public ScopeNotFoundException(String message) {
    super(message);
  }

  public ScopeNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ScopeNotFoundException(Throwable cause) {
    super(cause);
  }

  public ScopeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
