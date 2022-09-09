package org.quizbe.controller

import org.quizbe.dto.ScopeDto
import org.quizbe.dto.TopicDto
import org.quizbe.exception.TopicNotFoundException
import org.quizbe.service.TopicService
import org.quizbe.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RequestMapping("/topic")
@Controller
class TopicController @Autowired constructor(private val topicService: TopicService, private val userService: UserService) {
    var logger: Logger = LoggerFactory.getLogger(TopicController::class.java)

    @GetMapping(value = ["/index", "/", ""])
    fun index(model: Model, request: HttpServletRequest): String {
        val nameCurrentUser = request.userPrincipal.name
        val currentUser = userService.findByUsername(nameCurrentUser)
        model.addAttribute("topics", topicService.getAllTopicsOf(currentUser))
        return "/topic/index"
    }

    // @EnableGlobalMethodSecurity(prePostEnabled = true) in Configuration class
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @GetMapping(value = ["/add"])
    fun register(@ModelAttribute topicDto: TopicDto): String {
        if (topicDto.getScopesDtos().isEmpty()) {
            topicDto.getScopesDtos().add(ScopeDto("Scope1"))
            topicDto.getScopesDtos().add(ScopeDto("Scope2"))
            topicDto.getScopesDtos().add(ScopeDto("Scope3"))
        } else {
            // already set
        }
        return "topic/add-update"
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @GetMapping("/edit/{id}")
    fun showUpdateForm(@PathVariable("id") id: Long, model: Model): String {
        try {
            val topicDto = topicService.findTopicDtoById(id)
            model.addAttribute("topicDto", topicDto)
        } catch (ex: TopicNotFoundException) {
            throw TopicNotFoundException(HttpStatus.NOT_FOUND.toString(), ex)
        }
        return "topic/add-update"
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @GetMapping("/delete/{id}")
    fun deleteTopic(@PathVariable("id") id: Long, request: HttpServletRequest): String {
        val topic = topicService.findTopicById(id).orElseThrow { TopicNotFoundException() }
        val nameCurrentUser = request.userPrincipal.name
        val currentUser = userService.findByUsername(nameCurrentUser)
        if (topic!!.creator!!.id != currentUser!!.id) {
            // TODO accept ADMIN ?
            throw AccessDeniedException("")
        }
        topicService.deleteTopic(topic)
        return "redirect:/topic/index"
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping(value = ["/addupdate"])
    fun addOrUpdateClassroom(topicDto: @Valid TopicDto, result: BindingResult, request: HttpServletRequest): String {
        logger.info("scopes : " + topicDto.getScopesDtos())
        logger.info("result : $result")
        if (result.hasErrors()) {
            return "topic/add-update"
        }
        if (topicDto.id == null) {
            val nameCurrentUser = request.userPrincipal.name
            topicDto.creatorUsername = nameCurrentUser
        }
        topicService.saveTopicFromTopicDto(topicDto, result)
        return if (result.hasErrors()) {
            "topic/add-update"
        } else "redirect:/topic/index"
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping(value = ["/visible"])
    fun setVisible(request: HttpServletRequest): String {
        val id = request.getParameter("id").toLong()
        val topic = topicService.findTopicById(id)
                .orElseThrow { TopicNotFoundException("Invalid topic Id:$id") }
        val visible = request.getParameter("visible")

        // checkbox not ckecked => visible == null
        topic!!.isVisible = visible != null
        topicService.save(topic)
        return "redirect:/topic/index"
    }

    @PreAuthorize("hasAnyRole('USER', 'TEACHER', 'ADMIN')")
    @GetMapping("/subscribed")
    fun manageSubscribedTopics(model: Model, request: HttpServletRequest): String {
        val allTopics = topicService.allTopics
        val nameCurrentUser = request.userPrincipal.name
        val currentUser = userService.findByUsername(nameCurrentUser)
        model.addAttribute("topics", allTopics)
        model.addAttribute("currentUser", currentUser)
        return "topic/subscribed"
    }

    @PreAuthorize("hasAnyRole('USER', 'TEACHER', 'ADMIN')")
    @PostMapping("/subscribed")
    fun doSubscribedTopics(model: Model?, request: HttpServletRequest): String {
        val id = request.getParameter("id").toLong()
        val topic = topicService.findTopicById(id)
                .orElseThrow { TopicNotFoundException("Invalid topic Id:$id") }
        val nameCurrentUser = request.userPrincipal.name
        val currentUser = userService.findByUsername(nameCurrentUser)

        // bascule
        if (topic!!.subscribers.contains(currentUser)) {
            topic.subscribers.remove(currentUser)
        } else {
            topic.subscribers.add(currentUser!!)
        }
        topicService.save(topic)
        return "redirect:/topic/subscribed"
    }
}