
package org.quizbe.dto;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


public class ScopeDto {

  @NotBlank(message = "{scope.name.blank}")
  @Size(min = 3, max = 30, message = "{scope.name.min.max}")
  private String name;

  private Long id;

  public ScopeDto() {
  }

  public ScopeDto(String name) {
    super();
    this.name = name;
  }

  public ScopeDto(Long id, String name) {
    this(name);
    this.id = id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return this.id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "ScopeDto{" +
            "name='" + name + '\'' +
            ", id=" + id +
            '}';
  }
}
