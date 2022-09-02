package org.quizbe.config;

import org.quizbe.controller.AdminController;
import org.quizbe.model.User;
import org.quizbe.service.UserService;
import org.quizbe.service.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QuizbeInterceptor implements HandlerInterceptor {

  Logger logger = LoggerFactory.getLogger(QuizbeInterceptor.class);

  @Autowired
  UserServiceImpl userService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // logger.info("In interceptor...");
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    // logger.info("In interceptor afterCompletion...");
    HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
  }
}
