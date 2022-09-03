package org.quizbe.config;

import org.quizbe.model.User;
import org.quizbe.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class QuizbeAccessDeniedHandler implements AccessDeniedHandler {

  Logger logger = LoggerFactory.getLogger(QuizbeAccessDeniedHandler.class);

  @Autowired
  private UserService userService;

  @Override
  public void handle(
          HttpServletRequest request,
          HttpServletResponse response,
          AccessDeniedException exc) throws IOException, ServletException {

    Authentication auth
            = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      logger.warn("User: " + auth.getName()
              + " attempted to access the protected URL: "
              + request.getRequestURI());
      User currentUser = userService.findByUsername(auth.getName());
      if (currentUser != null && currentUser.mustChangePassword()) {
        response.sendRedirect(request.getContextPath() + "/douser/updatepw");
        return;
      }
    } else {
      logger.info("Access denied to " +  request.getRequestURI());
    }

    response.sendRedirect(request.getContextPath() + "/access-denied");
  }

}


