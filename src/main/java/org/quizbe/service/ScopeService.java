package org.quizbe.service;

import com.google.common.collect.Lists;
import org.quizbe.dao.ScopeRepository;
import org.quizbe.dao.TopicRepository;
import org.quizbe.dao.UserRepository;
import org.quizbe.dto.ScopeDto;
import org.quizbe.dto.TopicDto;
import org.quizbe.exception.TopicNotFoundException;
import org.quizbe.exception.UserNotFoundException;
import org.quizbe.model.Scope;
import org.quizbe.model.Topic;
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
public class ScopeService {
  TopicRepository topicRepository;
  ScopeRepository scopeRepository;
  Logger logger = LoggerFactory.getLogger(ScopeService.class);

  @Autowired
  public ScopeService(TopicRepository topicRepository, UserRepository userRepository, ScopeRepository scopeRepository) {
    this.topicRepository = topicRepository;
    this.scopeRepository = scopeRepository;
  }

  public void save(Scope scope) {
    scopeRepository.save(scope);
  }

  public Optional<Scope> findById(long idScope) {
    return scopeRepository.findById(idScope);
  }
}

