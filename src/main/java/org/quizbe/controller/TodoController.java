package org.quizbe.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TodoController {

  Logger logger = LoggerFactory.getLogger(TodoController.class);

  @GetMapping(value = {"/todo"})
  public String todo() {
    return "todo";
  }

}
