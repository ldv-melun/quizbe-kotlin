
package org.quizbe.dto;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


public class PasswordDto {

  @NotBlank(message = "{password.name.blank}")
  @Size(min = 8, max = 30, message = "{password.name.min.max}")
  private String password;

  @NotBlank(message = "{password.name.blank}")
  @Size(min = 8, max = 30, message = "{password.name.min.max}")
  private String confirmPassword;

  public PasswordDto() {

  }

  public PasswordDto(String password, String confirmPassword) {
    super();
    this.password = password;
    this.confirmPassword = confirmPassword;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }
}
