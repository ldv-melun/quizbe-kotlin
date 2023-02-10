package org.quizbe.controller

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class TodoController {
    var logger = LoggerFactory.getLogger(TodoController::class.java)
    @GetMapping(value = ["/todo"])
    fun todo(@RequestParam message: String?, model: Model): String {
        model.addAttribute("message", message ?: "ok")
        return "todo"
    }
}