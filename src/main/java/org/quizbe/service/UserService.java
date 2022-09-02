package org.quizbe.service;

import org.quizbe.dto.UserDto;
import org.quizbe.model.Role;
import org.quizbe.model.User;
import org.springframework.validation.BindingResult;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

public interface UserService  {
    User saveUserFromUserDto(UserDto userDto) throws SQLIntegrityConstraintViolationException;
    User findByEmail(String email);
    User findByUsername(String username);
    List<User> findAll();
    UserDto findUserDtoById(long id);

    void delete(User user);
    Optional<User> findById(long id);

    void save(User user);

    void flipflopUserRole(User user, Role role);

    boolean userUpdatePassword(User user, String password);

    void invalidePasswordBySetWithDefaultPlainTextPassord(User user);

    void checkAddUpdateUser(UserDto userDto, BindingResult bindingResult);

    void updateDefaultPlainTextPassword(User user);
}
