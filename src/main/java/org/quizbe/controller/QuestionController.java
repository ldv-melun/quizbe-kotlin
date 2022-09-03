package org.quizbe.controller;

import org.quizbe.dao.RatingRepository;
import org.quizbe.dto.QuestionDto;
import org.quizbe.dto.RatingDto;
import org.quizbe.exception.ScopeNotFoundException;
import org.quizbe.exception.TopicNotFoundException;
import org.quizbe.model.*;
import org.quizbe.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequestMapping("/question")
@Controller
public class QuestionController {

  Logger logger = LoggerFactory.getLogger(QuestionController.class);

  private TopicService topicService;
  private UserService userService;
  private ScopeService scopeService;
  private QuestionService questionService;
  private RatingService ratingService;

  @Autowired
  public QuestionController(TopicService topicService, UserService userService, ScopeService scopeService, QuestionService questionService, RatingService ratingService) {
    this.topicService = topicService;
    this.userService = userService;
    this.scopeService = scopeService;
    this.questionService = questionService;
    this.ratingService = ratingService;
  }

  @GetMapping(value = {"/index", "/", ""})
  public String questions(Model model, HttpServletRequest request) {
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());
    if (currentUser == null)
      return "/";

    String idSelectedTopic = request.getParameter("id-selected-topic");
    String idSelectedScope = request.getParameter("id-selected-scope");

    Topic selectedTopic = null;
    Scope selectedScope = null;
    List<Question> questions = new ArrayList<>();

    // get idSelectedTopic AND (optional) idSelectedScope

    if (idSelectedTopic != null) {
      long idTopic = Long.parseLong(idSelectedTopic);
      selectedTopic = topicService.findById(idTopic)
              .orElseThrow(() -> new AccessDeniedException("Invalid topic id : " + idTopic));
      if ( ! request.isUserInRole("TEACHER") && ! currentUser.getSubscribedTopicsVisibles().contains(selectedTopic)) {
        throw new AccessDeniedException("Topic non disponible !");
      }
    }

    List<Topic> topics =
            request.isUserInRole("TEACHER")
                    ? currentUser.getSubscribedTopics().stream().collect(Collectors.toList())
                    : currentUser.getSubscribedTopicsVisibles();

    // user can only see these topics (hack)
    // only TEACHER can view not visible topics
    if (selectedTopic != null) {
      if (!topics.contains(selectedTopic)) {
        selectedTopic = null;
        selectedScope = null;
        // throw new TopicNotFoundException("Invalid classroom selected Id:" + idSelectedTopic);
      } else {
        if (idSelectedScope != null) {
          long idScope = Long.parseLong(idSelectedScope);
          selectedScope = scopeService.findById(idScope)
                  .orElseThrow(() -> new ScopeNotFoundException("Invalid id : " + idSelectedScope));
        }
        questions = selectedTopic.getQuestions(selectedScope);
      }
    }

    model.addAttribute("currentUser", currentUser);
    model.addAttribute("topics", topics);
    model.addAttribute("selectedTopic", selectedTopic);
    model.addAttribute("selectedScope", selectedScope);
    model.addAttribute("questions", questions);
    // TODO placer dans la session de l'utilisateur (ou autre ?) la liste des ids des questions sélectionnées
    // afin de permettre la navigation dans la vue "play"

    return "/question/index";
  }

  @GetMapping(value = {"/new/{idtopic}/{idscope}", "/new/{idtopic}"})
  public String newQuestion(@PathVariable("idtopic") long idTopic,
                            @PathVariable("idscope") Optional<Long> idScope,
                            HttpServletRequest request, Model model) {
    Topic topic = topicService.findTopicById(idTopic)
            .orElseThrow(() -> new TopicNotFoundException("Topic error id : " + idTopic));

    Scope scope = topic.getScopes().get(0);
    if (idScope.isPresent()) {
      scope = scopeService.findById(idScope.get()).orElse(topic.getScopes().get(0));
    }
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());

    QuestionDto questionDto =
            new QuestionDto(null, topic, scope == null ? null : scope.getId(), currentUser.getUsername());

    model.addAttribute("questionDto", questionDto);

    return "/question/add-update-question";
  }

  @PostMapping(value = {"/addupdate"})
  public String addOrUpdateClassroom(@Valid QuestionDto questionDto, BindingResult result, Model model, HttpServletRequest request) {
    if (result.hasErrors()) {
      logger.info("result : " + result);
      return "/question/add-update-question";
    }
    questionService.saveQuestionFromQuestionDto(questionDto);
    return "redirect:/question/index?id-selected-topic=" + questionDto.getTopic().getId()
            + "&id-selected-scope=" + questionDto.getIdScope();
  }

  @GetMapping("/delete/{id}")
  public String delete(@PathVariable("id") long id, Model model, HttpServletRequest request) {
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());

    Question question = questionService.findById(id);
    if (!question.getDesigner().equals(currentUser.getUsername())) {
      throw new AccessDeniedException("");
    }

    questionService.delete(question);

    return "redirect:/question/index";
  }


  @GetMapping("/edit/{id}")
  public String showUpdateForm(@PathVariable("id") long id, Model model, HttpServletRequest request) {
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());

    Question question = questionService.findById(id);
    if (!question.getDesigner().equals(currentUser.getUsername())) {
      throw new AccessDeniedException("");
    }
    QuestionDto questionDto = questionService.findQuestionDtoById(id);
    model.addAttribute("questionDto", questionDto);
    return "/question/add-update-question";
  }

  @GetMapping("/play/{idquest}")
  public String showPlay(@PathVariable("idquest") long idQuestion, RatingDto ratingDto, Model model, HttpServletRequest request) {
    Question question = questionService.findById(idQuestion);
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());
    Rating userRating = ratingService.getRating(currentUser, question).orElse(null);

//    logger.info("in showPlay - 1 question " + question);
//    logger.info("in showPlay - 1 ratingDto " + ratingDto);

    if (userRating == null) {
      userRating = new Rating();
    } else {
      // existing user rating
      ratingDto.setId(userRating.getId());
      // is user being updated his rating ?
      if (ratingDto.getComment() == null) {
        ratingDto.setComment(userRating.getComment());
      }
      if (ratingDto.getValue() == null || ratingDto.getValue() == 0) {
        ratingDto.setValue(userRating.getValue());
      }
      ratingDto.setOutDated(userRating.isOutDated());
    }

//    logger.info("in showPlay -2 userRating " + userRating);
//    logger.info("in showPlay - 2 ratingDto " + ratingDto);

    model.addAttribute("question", question);
    model.addAttribute("userRating", userRating);

    // TODO duplicate code in view ...
    return "/question/play";
  }

  @GetMapping("/deleterating/{idquest}")
  public String deletePlayRating(@PathVariable("idquest") long idQuestion, HttpServletRequest request, RedirectAttributes redirAttrs) {
    Question question = questionService.findById(idQuestion);
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());
    Rating userRating = ratingService.getRating(currentUser, question).orElse(null);

    if (userRating != null) {
      ratingService.delete(userRating);
      redirAttrs.addFlashAttribute("successMessage", "delete.ok");
    } else {
      redirAttrs.addFlashAttribute("errorMessage", "delete.no.ok");
    }
    return "redirect:/question/play/" + idQuestion;
  }


  @PostMapping("/play/{idquest}")
  public String doUserRating(@PathVariable("idquest") long idQuestion, @Valid @ModelAttribute RatingDto ratingDto,
                             BindingResult result, Model model, HttpServletRequest request, RedirectAttributes redirAttrs) {

    if (result.hasErrors()) {
      return showPlay(idQuestion, ratingDto, model, request); //"/question/play";
    }

    Question question = questionService.findById(idQuestion);
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());
    Rating userRating = ratingService.getRating(currentUser, question).orElse(new Rating());

    userRating.setQuestion(question);

    if (userRating.getUser() == null) {
      userRating.setUser(currentUser);
    }

    userRating.setComment(ratingDto.getComment());
    userRating.setValue(ratingDto.getValue());
    userRating.setDateUpdate(LocalDateTime.now());
    ratingService.save(userRating);
//    redirAttrs.addFlashAttribute("successMessage", "operation.successful");
    return "redirect:/question/play/" + idQuestion;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/userrate/{idquestion}")
  public String adminUserRate(@PathVariable("idquestion") long idQuestion, Model model,
                              HttpServletRequest request, RedirectAttributes redirAttrs) {
    Question question = questionService.findById(idQuestion);
    User currentUser = userService.findByUsername(request.getUserPrincipal().getName());
    try {
      long idRating =
              request.getParameter("id-rating") == null ? 0 : Integer.parseInt(request.getParameter("id-rating"));
      if (idRating <= 0) {
        throw new IllegalArgumentException("Invalid action");
      }
      String comment = request.getParameter("comment");
      int value = Integer.parseInt(request.getParameter("value"));
      Rating userRating = ratingService.findById(idRating).orElseThrow(IllegalArgumentException::new);
      userRating.setQuestion(question);
      // no change user
      userRating.setComment(comment);
      userRating.setValue(value);
      // no change update date ? (because it's admin who performs this operation)
      // userRating.setDateUpdate(LocalDateTime.now());
      ratingService.save(userRating);
      redirAttrs.addFlashAttribute("successMessage", "operation.successful");
    } catch (Exception e) {
      redirAttrs.addFlashAttribute("errorMessage", "operation.fail");
    }
    return "redirect:/question/play/" + idQuestion;
  }

}
