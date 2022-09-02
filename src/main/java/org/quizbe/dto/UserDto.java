package org.quizbe.dto;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

public class UserDto {

  @NotBlank(message = "{user.username.blank}")
  @Size(min = 3, max = 30, message = "{user.username.min.max}")
  private String username;

  @NotBlank(message = "{user.email.blank}")
  @Email(message = "{user.email.invalid}")
  private String email;

  @NotBlank(message = "{user.password.blank}")
  @Size(min = 8, message = "{user.password.min}")
  private String password;

  private Long id;

  private Set<String> roles;

  public UserDto() {

  }

  /**
   * UserDto
   * @param userName
   * @param email
   * @param password
   */
  public UserDto(String userName, String email, String password) {
    super();
    this.username = userName;
    this.email = email;
    this.password = password;
  }

  public UserDto(String userName, String email, String password, Set<String> roles) {
    this(userName, email, password);
    this.roles = roles;
  }

  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }

  public void setRole(Set<String> roles) {
    this.roles = roles;
  }
  public Set<String> getRoles() {
    return this.roles;
  }

  public void setId(Long id) {
    this.id = id;
  }
  public Long getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return "UserDto {" +
            "id='" + id + '\'' +
            ", userName='" + username + '\'' +
            ", email='" + email + '\'' +
            ", Roles=" + roles.toString() +
            '}';
  }

}
