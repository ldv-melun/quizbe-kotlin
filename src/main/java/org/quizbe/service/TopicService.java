package org.quizbe.service;

import com.google.common.collect.Lists;
import org.quizbe.dao.TopicRepository;
import org.quizbe.dao.ScopeRepository;
import org.quizbe.dao.UserRepository;
import org.quizbe.dto.TopicDto;
import org.quizbe.dto.ScopeDto;
import org.quizbe.exception.TopicNotFoundException;
import org.quizbe.exception.UserNotFoundException;
import org.quizbe.model.Topic;
import org.quizbe.model.Scope;
import org.quizbe.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TopicService {
  TopicRepository topicRepository;
  UserRepository userRepository;
  ScopeRepository scopeRepository;
  Logger logger = LoggerFactory.getLogger(TopicService.class);

  @Autowired
  public TopicService(TopicRepository topicRepository, UserRepository userRepository, ScopeRepository scopeRepository) {
    this.topicRepository = topicRepository;
    this.userRepository = userRepository;
    this.scopeRepository = scopeRepository;
  }

  public List<Topic> getAllTopics() {
    return Lists.newArrayList(topicRepository.findAll());
  }

  public List<Topic> getAllTopicsOf(User user) {
    return topicRepository.findByCreator(user);
  }

  public boolean saveTopicFromTopicDto(TopicDto topicDto, BindingResult result) {
    // rechercher si il n'existe pas une classe de même nom associée au courrent user

    User creator = userRepository.findByUsername(topicDto.getCreatorUsername());

    if (creator == null) {
      throw new UserNotFoundException("Creator not found :" + topicDto.getCreatorUsername());
    }

    List<Topic> topics = topicRepository.findByCreator(creator);

    // create topic ?
    if (topics != null && topicDto.getId() == null) {
      if (topics.stream().anyMatch(classroom -> classroom.getName().equalsIgnoreCase(topicDto.getName()))) {
        // a topic exists whith same name
        if (result != null) {
          result.rejectValue("name", "topic.name.already.exists", "already exists");
//        ResourceBundle bundle = ResourceBundle.getBundle("i18n/validationMessages", LocaleContextHolder.getLocale());
//        String errorMessageDefault = bundle.getString("topic.name.already.exists"); // always default... tested also with "Truc"
//        String keyNotExists = "topic.name.already.exists";
//        result.rejectValue("name", keyNotExists, errorMessageDefault);
        }
        return false;
      }
    }
    Topic topic;
    if (topicDto.getId() != null) {
      // update
      topic = topicRepository.findById(topicDto.getId()).orElseThrow(
              TopicNotFoundException::new);
      topic.removeScopes();
      topic.setName(topicDto.getName());
    } else {
      topic = new Topic();
      topic.setName(topicDto.getName());
      topic.setCreator(creator);
    }
    // add/update all scopes
    for (ScopeDto scopeDto : topicDto.getScopesDtos()) {
      Scope scope = new Scope();
      scope.setName(scopeDto.getName());
      if (scopeDto.getId() != null) {
        scope.setId(scopeDto.getId());
      }
      topic.addScope(scope);
       // logger.info("scope add : " + scope);
    }

    topic.setVisible(topicDto.isVisible());
    topicRepository.save(topic);

    return true;
  }

  public TopicDto findTopicDtoById(long id)  {
    Optional<Topic> topic = topicRepository.findById(id);
    if (topic.isPresent()) {
      return fromTopicToTopicDto(topic.get());
    } else {
      throw new TopicNotFoundException("Invalid topic Id:" + id);
    }
  }

  private TopicDto fromTopicToTopicDto(Topic topic) {
    TopicDto topicDto = new TopicDto();
    topicDto.setId(topic.getId());
    topicDto.setName(topic.getName());
    topicDto.setVisible(topic.isVisible());
    topicDto.setCreatorUsername(topic.getCreator().getUsername());
    topicDto.setScopesDtos(topic.getScopes().stream().map(scope -> new ScopeDto(scope.getId(), scope.getName())).collect(Collectors.toList()));
    return topicDto;
  }

  public Optional<Topic> findTopicById(long id) {
    return topicRepository.findById(id);
  }

  public void saveTopic(Topic topic) {
    topicRepository.save(topic);
  }

  public Optional<Topic> findById(long idClassroom) {
    return topicRepository.findById(idClassroom);
  }

  public void deleteTopic(Topic topic) {
    topic.getCreator().removeSubscribedTopics(topic);
    topicRepository.delete(topic);
  }

  public Optional<Topic> getTopicById(long i) {
    return topicRepository.findById(i);
  }
}
