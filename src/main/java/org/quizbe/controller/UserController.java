package org.quizbe.controller;

import org.quizbe.dto.UserDto;
import org.quizbe.exception.UserNotFoundException;
import org.quizbe.model.Role;
import org.quizbe.model.User;
import org.quizbe.service.RoleService;
import org.quizbe.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;
import java.util.ResourceBundle;

@RequestMapping("/user")
@Controller
public class UserController {

  Logger logger = LoggerFactory.getLogger(UserController.class);

  private UserService userService;
  private RoleService roleService;

  @Autowired
  public UserController(UserService userService, RoleService roleService) {
    this.userService = userService;
    this.roleService = roleService;
  }

  @GetMapping(value = {"/edit/{id}", "/edit/"})
  public String showUpdateForm(@PathVariable("id") Optional<Long> id, Model model, HttpServletRequest request) {
    if (id.isPresent()) {
      try {
        User currentUser = userService.findByUsername(request.getUserPrincipal().getName());
        if (!request.isUserInRole("ADMIN") && currentUser.getId() != id.get()) {
          throw new AccessDeniedException("edit user");
        }
        UserDto userDto = userService.findUserDtoById(id.get());
        model.addAttribute("userDto", userDto);
      } catch (UserNotFoundException ex) {
        throw new UserNotFoundException(ex);
      }
    } else {
      // user self update
      try {
        User currentUser = userService.findByUsername(request.getUserPrincipal().getName());
        UserDto userDto = userService.findUserDtoById(currentUser.getId());
        model.addAttribute("userDto", userDto);
      } catch (UserNotFoundException ex) {
        throw new UserNotFoundException(ex);
      }
    }
    return "admin/update-user";
  }

  @PostMapping("/update/{id}")
  public String updateUser(@Valid @ModelAttribute UserDto userDto,
                           BindingResult bindingResult,
                           HttpServletRequest request,
                           RedirectAttributes redirAttrs,
                           Model model) {
    // logger.info("bindingResult :" + bindingResult);
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());
    if (!request.isUserInRole("ADMIN") && currentUser.getId() != userDto.getId()) {
      throw new AccessDeniedException("update user");
    }
    if (bindingResult.hasErrors()) {
      return "admin/update-user";
    }
    userService.checkAddUpdateUser(userDto, bindingResult);

    if (bindingResult.hasErrors()) {
      return "admin/update-user";
    }

    try {
      userService.saveUserFromUserDto(userDto);
    } catch (Exception e) {
      logger.warn("SQL Integrity Exception in updateUser : " + e.getMessage());
      model.addAttribute("errorMessage", "error.message");
      return "admin/update-user";
    }

    if (request.isUserInRole("ADMIN")) {
      // flash message
      redirAttrs.addFlashAttribute("message", "success.message");
      return "redirect:/admin/users";
    } else {
      try {
        request.logout();
      } catch (ServletException e) {
        // ??
        logger.info("Error logout : " + e.getMessage());
      }
      return "redirect:/";
    }
  }

}
