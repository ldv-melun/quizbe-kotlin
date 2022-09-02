package org.quizbe.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.quizbe.dao.RoleRepository;
import org.quizbe.dao.UserRepository;
import org.quizbe.dto.UserDto;
import org.quizbe.exception.UserNotFoundException;
import org.quizbe.model.Role;
import org.quizbe.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;

@Service
public class UserServiceImpl implements UserService {

  private UserRepository userRepository;
  private RoleRepository roleRepository;

  private BCryptPasswordEncoder bCryptPasswordEncoder;

  Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
    super();
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  public User saveUserFromUserDto(UserDto userDto) {
    Role roleUserDB = roleRepository.findByName("USER");

    Assert.notNull(roleUserDB, "Role USER n'existe pas");

    Optional<User> user;

    if (userDto.getId() != null) {
      user = userRepository.findById(userDto.getId());
      if (user.isPresent()) {
        User updateUser = user.get();
        updateUser.setEmail(userDto.getEmail());
        updateUser.setUsername(userDto.getUsername());
        updateUser.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        try {
          return userRepository.save(updateUser);
        } catch (Exception e) {
          logger.warn("Exception userRepository.save : " + e.getMessage() + " " + e.getClass().getName());
          throw e;
        }
      } else {
        throw new UserNotFoundException("Invalid user Id:" + userDto.getId());
      }
    }

    Set<Role> roles = new HashSet<>();
    if (userDto.getRoles() == null || userDto.getRoles().size() == 0) {
      roles.add(roleUserDB);
    } else {
      for (String r : userDto.getRoles()) {
        Role roleDB = roleRepository.findByName(r);
        if (roleDB != null) {
          roles.add(roleDB);
        }
      }
    }
    User newUser = new User(userDto.getUsername(),
            userDto.getEmail(),
            bCryptPasswordEncoder.encode(userDto.getPassword()),
            roles);

    save(newUser);

    return newUser;

  }

  @Override
  public User findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public User findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  public UserDto findUserDtoById(long id) {
    Optional<User> user = userRepository.findById(id);
    if (user.isPresent()) {
      return fromUserToUserDto(user.get());
    } else {
      throw new UserNotFoundException("Invalid user Id:" + id);
    }
  }

  @Override
  public void delete(User user) {
    user.setRoles(null);
    userRepository.delete(user);
  }

  @Override
  public Optional<User> findById(long id) {
    return userRepository.findById(id);
  }

  @Override
  public void save(User user) {
    if (user.getDefaultPlainTextPassword() == null) {
      user.setDefaultPlainTextPassword(User.generateRandomPassword(8));
      user.setDateUpdatePassword(LocalDateTime.now());
    }
    userRepository.save(user);
  }

  /**
   * Add or remove role of user (flipflop)
   *
   * @param role role too add or remove
   */
  @Override
  public void flipflopUserRole(User user, Role role) {
    if (user.getRoles().contains(role)) {
      user.getRoles().remove(role);
    } else {
      user.getRoles().add(role);
    }
    userRepository.save(user);
  }

  private UserDto fromUserToUserDto(User user) {
    UserDto userDto = new UserDto();
    userDto.setId(user.getId());
    userDto.setUsername(user.getUsername());
    userDto.setEmail(user.getEmail());
    userDto.setPassword(user.getPassword());
    userDto.setRole(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()));
    return userDto;
  }

  /**
   * Set password only if password <> default clear password of user
   *
   * @param user
   * @param password new password
   * @return true if set or false else
   */
  @Override
  public boolean userUpdatePassword(User user, String password) {
    if (user.getDefaultPlainTextPassword().trim().equalsIgnoreCase(password.trim())) {
      return false;
    }
    user.setPassword(bCryptPasswordEncoder.encode(password));
    user.setDateUpdatePassword(LocalDateTime.now());
    userRepository.save(user);
    return true;
  }

  /**
   * Set password => default password
   *
   * @param user
   */
  public void invalidePasswordBySetWithDefaultPlainTextPassord(User user) {
    user.setPassword(bCryptPasswordEncoder.encode(user.getDefaultPlainTextPassword()));
    user.setDateDefaultPassword(LocalDateTime.now());
    user.setDateUpdatePassword(null);
    userRepository.save(user);
  }


  @Override
  public void updateDefaultPlainTextPassword(User user) {
    user.setDefaultPlainTextPassword(User.generateRandomPassword(8));
    this.invalidePasswordBySetWithDefaultPlainTextPassord(user);
  }

  /**
   * Check if userDto can be save in DB
   *
   * @param userDto
   * @param bindingResult out, result of check
   */
  public void checkAddUpdateUser(UserDto userDto, BindingResult bindingResult) {
    ResourceBundle bundle =
            ResourceBundle.getBundle("i18n/validationMessages", LocaleContextHolder.getLocale());
    User userExists = this.findByUsername(userDto.getUsername());

    if (userExists != null && (userDto.getId() == null || userDto.getId() != userExists.getId())) {
      String errorMessageDefault = bundle.getString("user.username.already.exist");
      String key = "user.username.already.exist";
      bindingResult
              .rejectValue("username", key, errorMessageDefault);
    }
    userExists = this.findByEmail(userDto.getEmail());
    if (userExists != null && (userDto.getId() == null || userDto.getId() != userExists.getId())) {
      String errorMessageDefault = bundle.getString("user.email.already.exist");
      String key = "user.email.already.exist";
      bindingResult
              .rejectValue("email", key, errorMessageDefault);
    }
  }
}
