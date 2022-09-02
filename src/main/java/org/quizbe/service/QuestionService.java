package org.quizbe.service;

import org.quizbe.dao.QuestionRepository;
import org.quizbe.dao.ScopeRepository;
import org.quizbe.dto.QuestionDto;
import org.quizbe.dto.ResponseDto;
import org.quizbe.exception.QuestionNotFoundException;
import org.quizbe.exception.ScopeNotFoundException;
import org.quizbe.model.Question;
import org.quizbe.model.Response;
import org.quizbe.model.Scope;
import org.quizbe.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
  private QuestionRepository questionRepository;
  private ScopeRepository scopeRepository;

  Logger logger = LoggerFactory.getLogger(QuestionService.class);

  @Autowired
  public QuestionService(QuestionRepository questionRepository, ScopeRepository scopeRepository) {
    this.questionRepository = questionRepository;
    this.scopeRepository = scopeRepository;
  }

  public boolean saveQuestionFromQuestionDto(QuestionDto questionDto) {
    Question question = convertToQuestion(questionDto);
    questionRepository.save(question);
    return true;
  }

  private Question convertToQuestion(QuestionDto questionDto) {
    Question question;

    if (questionDto.getId() != null) {
      question = questionRepository.findById(questionDto.getId()).orElseThrow(
              QuestionNotFoundException::new);
      question.removeResponses();

    } else {
      question = new Question();

    }
    Topic topic = questionDto.getTopic();
    Scope scope;
    if (questionDto.getIdScope() != null) {
      scope = scopeRepository.findById(questionDto.getIdScope()).orElseThrow(ScopeNotFoundException::new);
    } else {
      throw new ScopeNotFoundException();
    }
    question.setTopic(topic);
    question.setScope(scope);
    question.setCodesigner(questionDto.getCodesigners());
    question.setDesigner(questionDto.getDesigner());
    question.setName(questionDto.getName());
    question.setSentence(questionDto.getSentence());
    question.setVisible(questionDto.isVisible());
    question.setDateUpdate(LocalDateTime.now());
    // add/update all responses
    for (ResponseDto responseDto : questionDto.getResponseDtos()) {
      Response response = convertDtoToResponse(responseDto);
      question.addResponse(response);
    }
     return question;
  }

  private Response convertDtoToResponse(ResponseDto responseDto) {
    Response response = new Response();
    if (responseDto.getId() != null) {
      response.setId(responseDto.getId());
    }
    response.setProposition(responseDto.getProposition());
    response.setFeedback(responseDto.getFeedback());
    response.setValue(responseDto.getValue());
    return response;
  }

  private ResponseDto convertResponseToDto(Response response) {
    ResponseDto responseDto = new ResponseDto();
    responseDto.setId(response.getId());
    responseDto.setProposition(response.getProposition());
    responseDto.setFeedback(response.getFeedback());
    responseDto.setValue(response.getValue());
    return responseDto;
  }

  public QuestionDto findQuestionDtoById(long id) {
    Optional<Question> question = questionRepository.findById(id);
    if (question.isPresent()) {
      return fromQuestionToQuestionDto(question.get());
    } else {
      throw new QuestionNotFoundException("Invalid question Id:" + id);
    }
  }

  private QuestionDto fromQuestionToQuestionDto(Question question) {
    QuestionDto questionDto = new QuestionDto();
    questionDto.setDesigner(question.getDesigner());
    questionDto.setId(question.getId());
    questionDto.setName(question.getName());
    questionDto.setSentence(question.getSentence());
    questionDto.setVisible(question.isVisible());
    questionDto.setTopic(question.getTopic());
    questionDto.setIdScope(question.getScope().getId());
    List<ResponseDto> responseDtos = new ArrayList<>();
    for (Response response : question.getResponses() ) {
      responseDtos.add(convertResponseToDto(response));
    }
    questionDto.setResponseDtos(responseDtos);
    return questionDto;
  }

  public Question findById(long id) {
    return questionRepository.findById(id).orElseThrow(QuestionNotFoundException::new);
  }

  public Question findNextById(long id) {
    Integer idNext = questionRepository.findNextById(id);
    logger.info("id next :" + idNext);
    if (idNext != null)  return findById(idNext);
    return null;
  }

  public void delete(Question question) {
    questionRepository.delete(question);
  }
}

