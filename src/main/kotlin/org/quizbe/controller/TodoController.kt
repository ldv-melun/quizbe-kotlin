package org.quizbe.controller

import org.quizbe.dao.QuestionRepository
import org.quizbe.dao.ResponseRepository
import org.quizbe.dao.TopicRepository
import org.quizbe.exception.TopicNotFoundException
import org.quizbe.model.Topic
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
import javax.servlet.http.HttpServletRequest

@Controller
class TodoController @Autowired constructor(
    private val questionRepository: QuestionRepository,
    private val responseRepository: ResponseRepository,
    private val topicRepository: TopicRepository
) {
    var logger = LoggerFactory.getLogger(TodoController::class.java)

    @GetMapping(value = ["/todo"])
    fun todo(@RequestParam message: String?, model: Model): String {
        model.addAttribute("message", message ?: "ok")
        return "todo"
    }

    @GetMapping("/exportRaw")
    fun exportRaw(
        @RequestParam("idTopic") idTopic: Long,
        @RequestParam("idScope") idScope: Long,
        request: HttpServletRequest,
        model: Model
    ): ResponseEntity<String> {
        val build = StringBuilder()
        val questions = questionRepository.findByScopeIdAndTopicId(idScope, idTopic)
        for (question in questions) {
            if (question != null) {
                build.append(question.sentence).append(": ").append("\n")
                val responses = responseRepository.findByQuestion(question)
                if (responses != null) {
                    for (response in responses) {
                        if (response != null) {
                            build.append("[ ]").append(response.proposition).append("\n")
                        }
                    }
                }
                build.append("\n")
            }
        }
        val topic: Topic? = topicRepository.findById(idTopic).orElseThrow { TopicNotFoundException("topic not found") }
        val fileName = "export-quizbe-${topic?.name}-${LocalDate.now()}.txt"
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$fileName")
            .body(build.toString())

    }
}