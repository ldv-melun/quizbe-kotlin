package org.quizbe.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

public class ResponseDto {

  private Long id;

  @NotBlank(message = "{response.proposition.blank}")
  @Size(min = 3, max = 150, message = "{response.proposition.min.max}")
  private String proposition;

  @NotBlank(message = "{response.feedback.blank}")
  @Size(min = 10, max = 255, message = "{response.feedback.min.max}")
  private String feedback;


  @Min(value = -2, message = "{response.value.min}")
  @Max(value =  2, message = "{response.value.max}")
  private Integer value;

  public ResponseDto() {
  }

  /**
   *  Constructor
   * @param id
   * @param proposition
   * @param feedback
   * @param value
   */
  public ResponseDto(Long id, String proposition, String feedback, Integer value) {
    this.id = id;
    this.proposition = proposition;
    this.feedback = feedback;
    this.value = value;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getProposition() {
    return proposition;
  }

  public void setProposition(String proposition) {
    this.proposition = proposition;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "ResponseDto{" +
            "id=" + id +
            ", proposition='" + proposition + '\'' +
            ", feedback='" + feedback + '\'' +
            ", value=" + value +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResponseDto that = (ResponseDto) o;
    return Objects.equals(getId(), that.getId()) && Objects.equals(getProposition(), that.getProposition());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getProposition());
  }
}
