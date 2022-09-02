package org.quizbe.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "USER", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {

  @Transient
  Logger logger = LoggerFactory.getLogger(User.class);

  @Transient
  public static final long VALIDE_TIME_HOURS_DEFAULT_PW = 48+1;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "USERNAME", unique = true, nullable = false)
  private String username;

  @Column(name = "EMAIL", unique = true, nullable = false)
  private String email;

  @Column(name = "PASSWORD", nullable = false)
  private String password;

  @Column(name = "ACTIVE")
  private Boolean enabled;

  @Basic
  private LocalDateTime dateUpdatePassword;

  @Basic
  private LocalDateTime dateDefaultPassword;

  @Basic
  private String defaultPlainTextPassword;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinTable(
          name = "USER_ROLES",
          joinColumns = @JoinColumn(
                  name = "USER_ID", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(
                  name = "ROLE_ID", referencedColumnName = "id"))
  private Set< Role > roles;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
          name = "USER_TOPICS",
          joinColumns = @JoinColumn(
                  name = "USER_ID", referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(
                  name = "TOPIC_ID", referencedColumnName = "id"))
  private Set<Topic> subscribedTopics;

  @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
  private List<Topic> topics;

  public User() {
    this.enabled = true;
    // this.defaultPlainTextPassword = User.generateRandomPassword(8);
  }

  public User(String userName, String email, String password, Set < Role > roles) {
    super();
    this.username = userName;
    this.email = email;
    this.password = password;
    this.roles = roles;
    this.enabled = true;
  }
  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String userName) {
    this.username = userName;
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

  public Boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean active) {
    this.enabled = active;
  }

  public Set < Role > getRoles() {
    return roles;
  }
  public void setRoles(Set < Role > roles) {
    this.roles = roles;
  }

  public Set<Topic> getSubscribedTopics() {
    return subscribedTopics;
  }

  public List<Topic> getSubscribedTopicsVisibles() {
    return subscribedTopics.stream().filter(topic -> topic.isVisible()).collect(Collectors.toList());
  }

  public void setSubscribedTopics(Set<Topic> subscribedTopics) {
    this.subscribedTopics = subscribedTopics;
  }

  public boolean isSubscribed(Topic topic) {
    return this.subscribedTopics.contains(topic);
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public List<Topic> getTopics() {
    return topics;
  }

  public List<Topic> getVisibleTopics() {
    return topics.stream().filter(topic -> topic.isVisible()).collect(Collectors.toList());
  }

  public void setTopics(List<Topic> topics) {
    this.topics = topics;
  }

  public LocalDateTime getDateUpdatePassword() {
    return dateUpdatePassword;
  }

  public void setDateUpdatePassword(LocalDateTime dateUpdatePassword) {
    this.dateUpdatePassword = dateUpdatePassword;
  }

  public String getDefaultPlainTextPassword() {
    return defaultPlainTextPassword;
  }

  public void setDefaultPlainTextPassword(String defaultPlainTextPassword) {
    this.defaultPlainTextPassword = defaultPlainTextPassword;
  }

  @Override
  public String toString() {
    return "User{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", email='" + email + '\'' +
            ", password='" + password + '\'' +
            ", enabled=" + enabled +
            '}';
  }

  public static String generateRandomPassword(int len) {
    String chars = "0123456789ABCDEFGHIJKLMNOPQRTUVWXYZabcdefghijkmnopqrstuvwxyz";
    Random rnd = new Random();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(chars.charAt(rnd.nextInt(chars.length())));
    }
    return sb.toString();
  }

  public void removeSubscribedTopics(Topic topic) {
    this.getSubscribedTopics().remove(topic);
  }

  public LocalDateTime getSetDateDefaultPassword() {
    return this.dateDefaultPassword;
  }

  public void setDateDefaultPassword(LocalDateTime dateDefaultPassword) {
    this.dateDefaultPassword = dateDefaultPassword;
  }

  public boolean mustChangePassword() {
    return null == this.getDateUpdatePassword();
  }

  public long hoursWaitingToChangePassword() {
    if (this.getDateUpdatePassword() != null) return 0;
    long rest = ChronoUnit.HOURS.between( LocalDateTime.now() , this.getSetDateDefaultPassword().plusHours(this.VALIDE_TIME_HOURS_DEFAULT_PW) ) ;

    logger.info("this.getSetDateDefaultPassword()" + this.getSetDateDefaultPassword());

    return rest;
  }

  public boolean canWrite(Question question) {
    return question.getDesigner().equalsIgnoreCase(this.username);
  }

  public boolean canWrite(Topic topic) {
    return topic.getCreator().getId() == this.id;
  }
}
