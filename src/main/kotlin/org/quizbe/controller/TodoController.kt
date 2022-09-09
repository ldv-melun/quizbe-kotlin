package org.quizbe.controller

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TodoController {
    var logger = LoggerFactory.getLogger(TodoController::class.java)
    @GetMapping(value = ["/todo"])
    fun todo(): String {
        return "todo"
    }
}