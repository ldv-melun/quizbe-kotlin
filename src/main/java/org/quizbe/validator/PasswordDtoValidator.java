package org.quizbe.validator;

import org.quizbe.dto.PasswordDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PasswordDtoValidator implements Validator {

  @Override
  public boolean supports(Class<?> aClass) {
    return PasswordDto.class.equals(aClass);
  }

  @Override
  public void validate(Object o, Errors errors) {
    PasswordDto passwordDto = (PasswordDto) o;
     if (! passwordDto.getPassword().equals(passwordDto.getConfirmPassword())) {
       errors.rejectValue("password", "passwords.not.equals", "passwords.not.equals");
     }
  }
}


