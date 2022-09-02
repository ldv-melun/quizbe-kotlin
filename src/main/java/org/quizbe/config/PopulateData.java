package org.quizbe.config;

import com.google.common.collect.Lists;
import org.quizbe.dto.*;
import org.quizbe.model.*;
import org.quizbe.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Order(value = 1)
@Component
public class PopulateData implements ApplicationRunner {

  Logger logger = LoggerFactory.getLogger(PopulateData.class);

  private User admin;
  private User teacher;
  private User eleve;

  @Autowired
  UserService userService;

  @Autowired
  RoleService roleService;

  @Autowired
  TopicService topicService;

  @Autowired
  QuestionService questionService;

  @Autowired
  ScopeService scopeService;

  @Autowired
  RatingService ratingService;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    Role role = roleService.findByName("USER");

    if (role != null) return;

    roleService.saveRole(new Role("USER"));
    roleService.saveRole(new Role("TEACHER"));
    roleService.saveRole(new Role("ADMIN"));

    addUsers();
    addTopics();
    addQuestions();
    addRatings();
  }

  private void addRatings() {
    Question question1 = questionService.findById(1);
    Rating rating = new Rating(null, "La vie est belle", 5, LocalDateTime.now(), question1, teacher);
    ratingService.save(rating);
    rating = new Rating(null, "La vie, cette inconnue", 3, LocalDateTime.now(), question1, eleve);
    ratingService.save(rating);
  }

  private void addTopics() {
    TopicDto topicDto = new TopicDto();
    topicDto.setName("Topic1");
    topicDto.setCreatorUsername(teacher.getUsername());

    List<ScopeDto> scopeDtos = new ArrayList<>();
    scopeDtos.add(new ScopeDto("scope1"));
    scopeDtos.add(new ScopeDto("scope2"));
    topicDto.setScopesDtos(scopeDtos);
    topicService.saveTopicFromTopicDto(topicDto, null);

    topicDto = new TopicDto();
    topicDto.setName("Topic2");
    topicDto.setCreatorUsername(teacher.getUsername());

    scopeDtos.add(new ScopeDto("scope3"));
    topicDto.setScopesDtos(scopeDtos);
    topicService.saveTopicFromTopicDto(topicDto, null);

    // users subscribe to topic1
    Topic topic = topicService.findTopicById(1L).get();

    HashSet<Topic> topics = new HashSet<>();
    topics.add(topic);

    admin.setSubscribedTopics(topics);
    teacher.setSubscribedTopics(topics);
    eleve.setSubscribedTopics(topics);

    userService.save(eleve);
    userService.save(teacher);
    userService.save(admin);

  }

  private void addQuestions() {
    Topic topic = topicService.getTopicById(1L).orElse(null);

    List<ResponseDto> responseDtos = new ArrayList<>();
    responseDtos.add((new ResponseDto(null, "_42_", "feedback proposition 1", 1)));
    responseDtos.add((new ResponseDto(null, "proposition 2", "feedback proposition 2", -1)));
    responseDtos.add((new ResponseDto(null, "proposition 3", "feedback proposition 3", -2)));

    QuestionDto questionDto =
            new QuestionDto(null, topic, 1L, admin.getUsername());
    questionDto.setName("Question1");
    questionDto.setSentence("Answer to the Ultimate Question of Life, the Universe, and Everything");
    questionDto.setResponseDtos(responseDtos);

    questionService.saveQuestionFromQuestionDto(questionDto);
  }

  private void addUsers() throws SQLIntegrityConstraintViolationException {
    User userAdmin = userService.findById(1).orElse(null);
    if (userAdmin == null) {
      Set<String> roles = new HashSet<>();
      roles.add("USER");
      roles.add("TEACHER");
      roles.add("ADMIN");
      UserDto adminDto = new UserDto("admin", "admin@admin.org", "adminadmin", roles);
      logger.info("userAdmin : " + adminDto);

      admin = userService.saveUserFromUserDto(adminDto);

      roles.remove("ADMIN");
      UserDto teacherDto = new UserDto("prof", "prof@prof.org", "profprof", roles);
      logger.info("userTeacher : " + teacherDto);

      teacher = userService.saveUserFromUserDto(teacherDto);

      roles.remove("TEACHER");
      UserDto eleveDto = new UserDto("eleve", "eleve@eleve.org", "eleveeleve", roles);
      logger.info("userEleve : " + eleveDto);

      eleve = userService.saveUserFromUserDto(eleveDto);

    }
  }
}
