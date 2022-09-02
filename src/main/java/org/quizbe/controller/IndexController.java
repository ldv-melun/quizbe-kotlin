package org.quizbe.controller;

import org.quizbe.dto.PasswordDto;
import org.quizbe.exception.TopicNotFoundException;
import org.quizbe.exception.UserNotFoundException;
import org.quizbe.model.Topic;
import org.quizbe.model.User;
import org.quizbe.service.TopicService;
import org.quizbe.service.UserService;
import org.quizbe.validator.PasswordDtoValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
public class IndexController {

  Logger logger = LoggerFactory.getLogger(IndexController.class);

  private TopicService topicService;
  private UserService userService;

  @Autowired
  private PasswordDtoValidator passwordDtoValidator;

  @Autowired
  public IndexController(TopicService topicService, UserService userService) {
    this.topicService = topicService;
    this.userService = userService;
  }

  @GetMapping(value = {"/",})
  public String index() {

    return "/main/index";
  }

  @GetMapping(value = {"/login",})
  public String login() {
    return "/main/login";
  }

  /**
   * @See WebSecurityConfiguration
   */
  @GetMapping(value = {"/access-denied",})
  public String acceDenied() {
    return "/error/access-denied";
  }


  /*
   * come from QuizbeAccessDeniedHandler and CustomUserServiceDetails
   */
  @PreAuthorize("hasRole('CHANGE_PW')")
  @GetMapping("/douser/updatepw")
  public String showUpdatePassword(@ModelAttribute PasswordDto passwordDto, HttpServletRequest request, Model model) throws ServletException {
    User user = userService.findByUsername(request.getUserPrincipal().getName());
    // defensive code
    if (user == null) {
      throw new UserNotFoundException("Invalid User");
    }

    return "main/update-user-pw";
  }

  @PreAuthorize("hasRole('CHANGE_PW')")
  @PostMapping("/douser/updatepw")
  public String userUpdatePassword(@Valid @ModelAttribute PasswordDto passwordDto,
                                   BindingResult result, Principal principal, HttpServletRequest request) throws ServletException {
    User user = userService.findByUsername(principal.getName());
    if (user == null) {
      throw new UserNotFoundException("Invalid User");
    }
    if (result.hasErrors()) {
      return "main/update-user-pw";
    }

    passwordDtoValidator.validate(passwordDto, result);
    if (result.hasErrors()) {
      return "main/update-user-pw";
    }

    if (userService.userUpdatePassword(user, passwordDto.getPassword())) {
      // password has changed
      request.logout();
      return "redirect:/login";
    }

    return "main/update-user-pw";
  }

}
