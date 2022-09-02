package org.quizbe.dto;

import javax.validation.constraints.*;
import java.util.Objects;

public class RatingDto {

  private Long id;

  @Size(min = 3, max = 200, message = "{play.user.rating.comment.min.max}")
  private String comment;

  @NotNull(message = "{play.user.rating.value}")
  @Min(value = 1, message = "{play.user.rating.value}")
  @Max(value = 5, message = "{play.user.rating.value}")
  private Integer value;
  private boolean outDated;

  public RatingDto() {
  }

  /**
   * Constructor
   * @param id
   * @param comment
   * @param value
   * @param outDated
   */
  public RatingDto(Long id, String comment, Integer value, boolean outDated) {
    this.id = id;
    this.comment = comment;
    this.value = value;
    this.outDated = outDated;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  public boolean isOutDated() {
    return outDated;
  }

  public void setOutDated(boolean outDated) {
    this.outDated = outDated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RatingDto ratingDto = (RatingDto) o;
    return getId().equals(ratingDto.getId()) && Objects.equals(getComment(), ratingDto.getComment()) && getValue().equals(ratingDto.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getComment(), getValue());
  }

  @Override
  public String toString() {
    return "RatingDto{" +
            "id=" + id +
            ", comment='" + comment + '\'' +
            ", value=" + value +
            '}';
  }
}
