package org.quizbe.controller;

import org.quizbe.dto.UserDto;
import org.quizbe.exception.UserNotFoundException;
import org.quizbe.model.Role;
import org.quizbe.model.User;
import org.quizbe.service.EmailServiceImpl;
import org.quizbe.service.RoleService;
import org.quizbe.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequestMapping("/admin")
@Controller
public class AdminController {

  Logger logger = LoggerFactory.getLogger(AdminController.class);

  private UserService userService;
  private RoleService roleService;
  private EmailServiceImpl emailService;

  @Autowired
  public AdminController(UserService userService, RoleService roleService, EmailServiceImpl emailService) {
    this.userService = userService;
    this.roleService = roleService;
    this.emailService = emailService;
  }

  @GetMapping("/users")
  public String showUserList(Model model) {
    model.addAttribute("users", userService.findAll());
    model.addAttribute("allRoles", roleService.findAllByOrderByName());
    return "admin/list-users";
  }

  @PostMapping("/addusers")
  public String addUsers(HttpServletRequest request, RedirectAttributes redirAttrs) {
    String[] users = request.getParameter("users").split(";");
    Set<String> roles = new HashSet<String>();
    roles.add("USER");

    for (String user : users ) {
      String[] userAttrib = user.split(",");
      if (userAttrib.length < 3) continue;
      UserDto userDto = new UserDto(userAttrib[0].trim(), userAttrib[1].trim(), userAttrib[2].trim());
      userDto.setRole(roles);
      try {
        userService.saveUserFromUserDto(userDto);
      } catch (/*SQLIntegrityConstraintViolationException*/ Exception e) {
        logger.warn("Exception in addUsers : " + userDto + "\n" + e.getMessage());
        redirAttrs.addFlashAttribute("errorMessage", "admin.user.add.error");
      }
    }
    return "redirect:/admin/users";
  }

  @GetMapping("/delete/{id}")
  public String deleteUser(@PathVariable("id") long id, Model model) {
    User user = userService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    userService.delete(user);
    return "redirect:/admin/users";
  }

  @PostMapping("/role")
  public String updateRoleUser(HttpServletRequest request) {
    Long id = Long.parseLong(request.getParameter("id"));
    String roleName = request.getParameter("rolename");

    String nameCurrentUser = request.getUserPrincipal().getName();
    User currentUser = userService.findByUsername(nameCurrentUser);

    User userToUpdate = userService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

    logger.info("userToUpdate : " + userToUpdate);
    logger.info("currentUser : " + currentUser);
    logger.info("roleName : " + roleName);

    // "super admin" stay admin
    if (roleName.equals("ADMIN")) {
      if (currentUser.getId() == 1 && userToUpdate.getId() == 1) {
        // Le super utilisateur reste admin !
        return "redirect:/admin/users";
      }
      if (currentUser.getId() > 1) {
        // non autorisé à gérer les rôles ADMIN
        return "redirect:/admin/users";
      }
    }

    Role role = roleService.findByName(roleName);
    if (role == null) {
      throw new IllegalArgumentException("Invalid role name:" + roleName);
    }

    // boolean userToUpdateIsAdmin = userToUpdate.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));

    userService.flipflopUserRole(userToUpdate, role);

    return "redirect:/admin/users";
  }


  @GetMapping(value = {"/register",})
  public String register(@ModelAttribute UserDto userDto) {
    return "/admin/registration";
  }

  @PostMapping(value = {"/register",})
  public String registerPost(@Valid @ModelAttribute UserDto userDto, BindingResult bindingResult, Model model,RedirectAttributes redirAttrs) {
    userService.checkAddUpdateUser(userDto, bindingResult);
    if (bindingResult.hasErrors()) {
      return "/admin/registration";
    }
    try {
      userService.saveUserFromUserDto(userDto);
    } catch (Exception e) {
      model.addAttribute("errorMessage", "error.message");
      redirAttrs.addFlashAttribute("errorMessage", "email.error.force.update.pw.message");
      return "/admin/registration";
    }
    return "redirect:/";
  }


  @GetMapping(value = {"/resetpw",})
  public String resetpw(RedirectAttributes redirAttrs, long id) {
    User user = userService.findById(id).orElseThrow(UserNotFoundException::new);
    userService.invalidePasswordBySetWithDefaultPlainTextPassord(user);
    try {
      String messageEmailBody =
        "Please go to <a href=\"https://quizbe.org\">https://quizbe.org</a> <br>" +
                "for change your password<br>" +
                "by pre-connect with this default password : <pre>" + user.getDefaultPlainTextPassword() + "</pre>";
      logger.info("Send email to " + user.getEmail());
      emailService.sendSimpleMessage(user.getEmail(), "Update PW", messageEmailBody);
      // don't work with parameter...
      //  redirAttrs.addFlashAttribute("successMessage", "#{${email.force.update.pw.message}("+user.getEmail()+")}");
      redirAttrs.addFlashAttribute("successMessage", "email.force.update.pw.message");
    } catch (Exception e) {
      e.printStackTrace();
      redirAttrs.addFlashAttribute("errorMessage", "email.error.force.update.pw.message");
    }
    return "redirect:/admin/users";
  }

}
