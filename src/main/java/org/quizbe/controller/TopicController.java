package org.quizbe.controller;

import org.quizbe.dto.TopicDto;
import org.quizbe.dto.ScopeDto;
import org.quizbe.exception.TopicNotFoundException;
import org.quizbe.model.Topic;
import org.quizbe.model.User;
import org.quizbe.service.TopicService;
import org.quizbe.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;


@RequestMapping("/topic")
@Controller
public class TopicController {

  Logger logger = LoggerFactory.getLogger(TopicController.class);

  private TopicService topicService;
  private UserService userService;

  @Autowired
  public TopicController(TopicService topicService, UserService userService) {
    this.topicService = topicService;
    this.userService = userService;
  }

  @GetMapping(value = {"/index","/", ""})
  public String index(Model model, HttpServletRequest request) {
    String nameCurrentUser = request.getUserPrincipal().getName();
    User currentUser = userService.findByUsername(nameCurrentUser);

    model.addAttribute("topics", topicService.getAllTopicsOf(currentUser));
    return "/topic/index";
  }

  // @EnableGlobalMethodSecurity(prePostEnabled = true) in Configuration class
  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  @GetMapping(value = {"/add",})
  public String register(@ModelAttribute TopicDto topicDto) {
    if(topicDto.getScopesDtos().isEmpty()) {
      topicDto.getScopesDtos().add(new ScopeDto("Scope1"));
      topicDto.getScopesDtos().add(new ScopeDto("Scope2"));
      topicDto.getScopesDtos().add(new ScopeDto("Scope3"));
    } else {
      // already set
    }
    return "topic/add-update";
  }

  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  @GetMapping("/edit/{id}")
  public String showUpdateForm(@PathVariable("id") long id, Model model) {
    try {
      TopicDto topicDto = topicService.findTopicDtoById(id);
      model.addAttribute("topicDto", topicDto);
    } catch (TopicNotFoundException ex) {
      throw new TopicNotFoundException(HttpStatus.NOT_FOUND.toString(), ex);
    }
    return "topic/add-update";
  }

  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  @GetMapping("/delete/{id}")
  public String deleteTopic(@PathVariable("id") long id, HttpServletRequest request) {
    Topic topic = topicService.findTopicById(id).orElseThrow(TopicNotFoundException::new);
    String nameCurrentUser = request.getUserPrincipal().getName();
    User currentUser = userService.findByUsername(nameCurrentUser);

    if( topic.getCreator().getId() != currentUser.getId() ) {
      // TODO accept ADMIN ?
      throw new AccessDeniedException("");
    }
    topicService.deleteTopic(topic);
    return "redirect:/topic/index";
  }

  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  @PostMapping(value= {"/addupdate"})
  public String addOrUpdateClassroom(@Valid TopicDto topicDto, BindingResult result, Model model, HttpServletRequest request) {

    logger.info("scopes : " + topicDto.getScopesDtos());
    logger.info("result : " + result);

    if (result.hasErrors()) {
      return "topic/add-update";
    }

    if (topicDto.getId() == null) {
      String nameCurrentUser = request.getUserPrincipal().getName();
      topicDto.setCreatorUsername(nameCurrentUser);
    }

    topicService.saveTopicFromTopicDto(topicDto, result);

    if (result.hasErrors()) {
      return "topic/add-update";
    }

    return "redirect:/topic/index";
  }

  @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
  @PostMapping(value= {"/visible"})
  public String setVisible(HttpServletRequest request)
  {
    Long id = Long.parseLong(request.getParameter("id"));

    Topic topic = topicService.findTopicById(id)
            .orElseThrow(() -> new TopicNotFoundException("Invalid topic Id:" + id));

    String visible = request.getParameter("visible");

    // checkbox not ckecked => visible == null
    topic.setVisible(visible != null);
    topicService.saveTopic(topic);

    return "redirect:/topic/index";
  }

  @PreAuthorize("hasAnyRole('USER', 'TEACHER', 'ADMIN')")
  @GetMapping("/subscribed")
  public String manageSubscribedTopics(Model model, HttpServletRequest request) {
    List<Topic> allTopics = topicService.getAllTopics();
    String nameCurrentUser = request.getUserPrincipal().getName();
    User currentUser = userService.findByUsername(nameCurrentUser);
    model.addAttribute("topics", allTopics);
    model.addAttribute("currentUser", currentUser);
    return "topic/subscribed";
  }

  @PreAuthorize("hasAnyRole('USER', 'TEACHER', 'ADMIN')")
  @PostMapping("/subscribed")
  public String doSubscribedTopics(Model model, HttpServletRequest request) {
    Long id = Long.parseLong(request.getParameter("id"));

    Topic topic = topicService.findTopicById(id)
            .orElseThrow(() -> new TopicNotFoundException("Invalid topic Id:" + id));

    String nameCurrentUser = request.getUserPrincipal().getName();
    User currentUser = userService.findByUsername(nameCurrentUser);

    // bascule
    if (currentUser.getSubscribedTopics().contains(topic)) {
      currentUser.getSubscribedTopics().remove(topic);
    } else {
      currentUser.getSubscribedTopics().add(topic);
    }

    userService.save(currentUser);

    return "redirect:/topic/subscribed";
  }



}
