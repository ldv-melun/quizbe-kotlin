
package org.quizbe.dto;


import org.quizbe.model.Topic;

import javax.persistence.Basic;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;


public class QuestionDto {

  private Long id;
  private Topic topic;
  private Long idScope;
  private boolean visible;
//  // auto set = current user (username)
//  private String creatorUsername;

  @NotBlank(message = "{question.name.blank}")
  @Size(min = 3, max = 30, message = "{question.name.min.max}")
  private String name;

  @NotBlank(message = "{question.sentence.blank}")
  @Size(min = 3, max = 255, message = "{question.sentence.min.max}")
  private String sentence;

  @NotBlank(message = "{question.designer.blank}")
  @Size(min = 3, max = 30, message = "{question.designer.min.max}")
  private String designer;

  @Basic
  private String codesigners = "";

  @NotNull
  @Size(min = 2, max = 10)
  @Valid private List<ResponseDto> responseDtos;

  public QuestionDto() {
  }

  /**
   * QuestionDto
   * @param id id
   * @param topic topic
   * @param idScope
   * @param creatorUsername
   */
  public QuestionDto(Long id, Topic topic, Long idScope, String creatorUsername) {
    this.id = id;
    this.topic = topic;
    this.idScope = idScope;
    this.designer = creatorUsername;
  }

  public Long getIdScope() {
    return idScope;
  }

  public void setIdScope(Long idScope) {
    this.idScope = idScope;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ResponseDto> getResponseDtos() {
    return responseDtos;
  }

  public void setResponseDtos(List<ResponseDto> responseDtos) {
    this.responseDtos = responseDtos;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return this.id;
  }

  public String getSentence() {
    return sentence;
  }

  public void setSentence(String sentence) {
    this.sentence = sentence;
  }

  public String getDesigner() {
    return designer;
  }

  public void setDesigner(String designer) {
    this.designer = designer;
  }

  public String getCodesigners() {
    return codesigners;
  }

  public void setCodesigners(String codesigners) {
    this.codesigners = codesigners;
  }

  public Topic getTopic() {
    return topic;
  }

  public void setTopic(Topic topic) {
    this.topic = topic;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}
