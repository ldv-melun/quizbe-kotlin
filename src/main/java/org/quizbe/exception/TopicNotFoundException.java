package org.quizbe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TopicNotFoundException extends RuntimeException {

  public TopicNotFoundException() {
  }

  public TopicNotFoundException(String message) {
    super(message);
  }

  public TopicNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public TopicNotFoundException(Throwable cause) {
    super(cause);
  }

  public TopicNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
