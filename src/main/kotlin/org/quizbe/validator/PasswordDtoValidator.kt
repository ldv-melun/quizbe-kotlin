package org.quizbe.validator

import org.quizbe.dto.PasswordDto
import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator

@Component
class PasswordDtoValidator : Validator {
    override fun supports(aClass: Class<*>): Boolean {
        return PasswordDto::class.java == aClass
    }

    override fun validate(o: Any, errors: Errors) {
        val passwordDto = o as PasswordDto
        if (passwordDto.password != passwordDto.confirmPassword) {
            errors.rejectValue("password", "passwords.not.equals", "passwords.not.equals")
        }
    }
}