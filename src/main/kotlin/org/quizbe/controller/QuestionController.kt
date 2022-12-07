package org.quizbe.controller

import org.quizbe.config.QuizbeGlobals.Constants.ERROR_MESSAGE
import org.quizbe.config.QuizbeGlobals.Constants.SUCCESS_MESSAGE
import org.quizbe.dto.QuestionDto
import org.quizbe.dto.RatingDto
import org.quizbe.exception.ScopeNotFoundException
import org.quizbe.exception.TopicNotFoundException
import org.quizbe.exception.UserNotFoundException
import org.quizbe.model.Question
import org.quizbe.model.Rating
import org.quizbe.model.Scope
import org.quizbe.model.Topic
import org.quizbe.service.*
import org.quizbe.utils.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RequestMapping("/question")
@Controller
class QuestionController @Autowired constructor(private val topicService: TopicService,
                                                private val userService: UserService,
                                                private val scopeService: ScopeService,
                                                private val questionService: QuestionService,
                                                private val ratingService: RatingService,
                                                private val quizbeEmailService: QuizbeEmailService) {
    val logger : Logger = LoggerFactory.getLogger(QuestionController::class.java)


    @GetMapping(value = ["/index", "/", ""])
    fun questions(model: Model, request: HttpServletRequest): String {
        val currentUser = userService.findByUsername(request.userPrincipal.name) ?: throw UserNotFoundException()
        val idSelectedTopic = request.getParameter("id-selected-topic")
        val idSelectedScope = request.getParameter("id-selected-scope")
        var selectedTopic: Topic? = null
        var selectedScope: Scope? = null
        var questions: List<Question?>? = ArrayList()

        // get idSelectedTopic AND (optional) idSelectedScope
        if (idSelectedTopic != null) {
            val idTopic = idSelectedTopic.toLong()
            selectedTopic = topicService.findById(idTopic)
                    .orElseThrow { AccessDeniedException("Invalid topic id : $idTopic") }
            if (!request.isUserInRole("TEACHER") && !currentUser.subscribedTopicsVisibles.contains(selectedTopic)) {
                throw AccessDeniedException("Topic non disponible !")
            }
        }
        val topics = if (request.isUserInRole("TEACHER"))
            currentUser.subscribedTopics.stream().collect(Collectors.toList()) else currentUser.subscribedTopicsVisibles

        // user can only see these topics (hack)
        // only TEACHER can view not visible topics
        if (selectedTopic != null) {
            if (!topics.contains(selectedTopic)) {
                selectedTopic = null
                selectedScope = null
                // throw new TopicNotFoundException("Invalid classroom selected Id:" + idSelectedTopic);
            } else {
                if (idSelectedScope != null) {
                    val idScope = idSelectedScope.toLong()
                    selectedScope = scopeService.findById(idScope)
                            .orElseThrow { ScopeNotFoundException("Invalid id : $idSelectedScope") }
                }
                questions = selectedTopic.getQuestions(selectedScope)
            }
        }
        model.addAttribute("currentUser", currentUser)
        model.addAttribute("topics", topics)
        model.addAttribute("selectedTopic", selectedTopic)
        model.addAttribute("selectedScope", selectedScope)
        model.addAttribute("questions", questions)
        // TODO placer dans la session de l'utilisateur (ou autre ?) la liste des ids des questions sélectionnées
        // afin de permettre la navigation dans la vue "play"
        return "/question/index"
    }

    @GetMapping(value = ["/new/{idtopic}/{idscope}", "/new/{idtopic}"])
    fun newQuestion(@PathVariable("idtopic") idTopic: Long,
                    @PathVariable("idscope") idScope: Optional<Long?>,
                    request: HttpServletRequest, model: Model): String {
        val topic = topicService.findTopicById(idTopic)
                .orElseThrow { TopicNotFoundException("Topic error id : $idTopic") }
        var scope: Scope? = topic!!.getScopes()[0]
        if (idScope.isPresent) {
            scope = scopeService.findById(idScope.get()).orElse(topic.getScopes()[0])
        }
        val currentUser = userService.findByUsername(request.userPrincipal.name)
        val questionDto = QuestionDto(null, topic, scope!!.id!!, currentUser!!.username)
        model.addAttribute("questionDto", questionDto)
        return "/question/add-update-question"
    }

    @PostMapping(value = ["/addupdate"])
    fun addOrUpdateQuestion(@Valid questionDto:  QuestionDto,
                            result: BindingResult, model: Model, request: HttpServletRequest): String {
        if (result.hasErrors()) {
            return "/question/add-update-question"
        }

        questionService.saveQuestionFromQuestionDto(questionDto)
        return ("redirect:/question/index?id-selected-topic=" + questionDto.topic!!.id
                + "&id-selected-scope=" + questionDto.idScope)
    }

    @GetMapping("/delete/{id}")
    fun delete(@PathVariable("id") id: Long, request: HttpServletRequest): String {
        val currentUser = userService.findByUsername(request.userPrincipal.name)
        val question = questionService.findById(id)
        if (question.designer != currentUser!!.username) {
            throw AccessDeniedException("")
        }
        questionService.delete(question)

        val idTopic = question.topic.id
        val arg = if (idTopic > 0) ("?id-selected-topic=$idTopic") else ""

        return "redirect:/question/index/$arg"
    }

    @GetMapping("/edit/{id}")
    fun showUpdateForm(@PathVariable("id") id: Long, model: Model, request: HttpServletRequest): String {
        val currentUser = userService.findByUsername(request.userPrincipal.name)
        val question = questionService.findById(id)
        if (question.designer != currentUser!!.username) {
            throw AccessDeniedException("")
        }
        val questionDto = questionService.findQuestionDtoById(id)
        model.addAttribute("questionDto", questionDto)
        return "/question/add-update-question"
    }

    @GetMapping("/play/{idquest}")
    fun showPlay(@PathVariable("idquest") idQuestion: Long, ratingDto: RatingDto?,
                 model: Model, request: HttpServletRequest): String {
        val question = questionService.findById(idQuestion)
        val currentUser = userService.findByUsername(request.userPrincipal.name)
        var userRating = ratingService.getRating(currentUser, question)?.orElse(null)

//    logger.info("in showPlay - 1 question " + question);
//    logger.info("in showPlay - 1 ratingDto " + ratingDto);
        if (userRating == null) {
            userRating = Rating()
        } else {
            // existing user rating
            ratingDto!!.id = userRating.id
            // is user being updated his rating ?
            if (ratingDto.comment == null) {
                ratingDto.comment = userRating.comment
            }
            if (ratingDto.value == null || ratingDto.value == 0) {
                ratingDto.value = userRating.value
            }
            ratingDto.outDated = userRating.isOutDated
        }

//    logger.info("in showPlay -2 userRating " + userRating);
//    logger.info("in showPlay - 2 ratingDto " + ratingDto);
        model.addAttribute("question", question)
        model.addAttribute("userRating", userRating)

        // TODO duplicate code in view ...
        return "/question/play"
    }

    @GetMapping("/deleterating/{idquest}")
    fun deletePlayRating(@PathVariable("idquest") idQuestion: Long,
                         request: HttpServletRequest, redirAttrs: RedirectAttributes): String {
        val question = questionService.findById(idQuestion)
        val currentUser = userService.findByUsername(request.userPrincipal.name)
        val userRating : Rating? = ratingService.getRating(currentUser, question)?.orElse(null)
        if (userRating != null) {
            ratingService.delete(userRating)
            redirAttrs.addFlashAttribute(SUCCESS_MESSAGE, "delete.ok")
        } else {
            redirAttrs.addFlashAttribute(ERROR_MESSAGE, "delete.no.ok")
        }
        return "redirect:/question/play/$idQuestion"
    }

    @PostMapping("/play/{idquest}")
    fun doUserRating(@PathVariable("idquest") idQuestion: Long, @Valid @ModelAttribute ratingDto: RatingDto?,
                     result: BindingResult, model: Model,
                     request: HttpServletRequest, redirAttrs: RedirectAttributes): String {
        if (result.hasErrors()) {
            return showPlay(idQuestion, ratingDto, model, request) //"/question/play";
        }
        val question = questionService.findById(idQuestion)
        val currentUser = userService.findByUsername(request.userPrincipal.name)
        val userRating : Rating? = ratingService.getRating(currentUser, question)?.orElse(Rating())
        userRating!!.question = question
        if (userRating.user == null) {
            userRating.user = currentUser
        }
        userRating.comment = ratingDto!!.comment
        userRating.value = ratingDto.value
        userRating.dateUpdate = LocalDateTime.now()

        ratingService.save(userRating)
        redirAttrs.addFlashAttribute(SUCCESS_MESSAGE, "operation.successful")

        // notification by mail to designer
        val designerUser = userService.findByUsername(question.designer)
        if (designerUser != null) {
            // an async call
            quizbeEmailService.sendMailToDesignerAfterCreteOrUpdateRating(designerUser, question, Utils.getBaseUrl(request))

//            if (quizbeEmailService.sendMailToDesignerAfterCreteOrUpdateRating(designerUser, question, Utils.getBaseUrl(request))) {
//                redirAttrs.addFlashAttribute(SUCCESS_MESSAGE, "operation.successful");
//            } else {
//                redirAttrs.addFlashAttribute(ERROR_MESSAGE, "email.error.force.update.pw.message")
//            }
        }

        return "redirect:/question/play/$idQuestion"
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/userrate/{idquestion}")
    fun adminUserRate(@PathVariable("idquestion") idQuestion: Long, model: Model?,
                      request: HttpServletRequest, redirAttrs: RedirectAttributes): String {
        val question = questionService.findById(idQuestion)
//        val currentUser = userService.findByUsername(request.userPrincipal.name)
        try {
            val idRating = (if (request.getParameter("id-rating") == null)
                0 else request.getParameter("id-rating").toInt()).toLong()
            require(idRating > 0) { "Invalid action" }
            val comment = request.getParameter("comment")
            val value = request.getParameter("value").toInt()
            val userRating = ratingService.findById(idRating).orElseThrow { IllegalArgumentException() }
            userRating!!.question = question
            // no change user
            userRating.comment = comment
            userRating.value = value
            // no change update date ? (because it's admin who performs this operation)
            // userRating.setDateUpdate(LocalDateTime.now());
            ratingService.save(userRating)
            redirAttrs.addFlashAttribute(SUCCESS_MESSAGE, "operation.successful")
        } catch (e: Exception) {
            redirAttrs.addFlashAttribute(ERROR_MESSAGE, "operation.fail")
        }
        return "redirect:/question/play/$idQuestion"
    }
}