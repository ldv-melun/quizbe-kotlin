package org.quizbe.service

import org.quizbe.dto.UserDto
import org.quizbe.model.Role
import org.quizbe.model.User
import org.springframework.validation.BindingResult
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

interface UserService {
    @Throws(SQLIntegrityConstraintViolationException::class)
    fun saveUserFromUserDto(userDto: UserDto): User
    fun findByEmail(email: String?): User?
    fun findByUsername(username: String?): User?
    fun findAll(): List<User?>
    fun findUserDtoById(id: Long): UserDto
    fun delete(user: User)
    fun findById(id: Long): Optional<User?>
    fun save(user: User)
    fun flipflopUserRole(user: User, role: Role?)
    fun userUpdatePassword(user: User, password: String): Boolean
    fun invalidePasswordBySetWithDefaultPlainTextPassord(user: User)
    fun checkAddUpdateUser(userDto: UserDto, bindingResult: BindingResult)
    fun updateDefaultPlainTextPassword(user: User)
    fun hasDefaultPlainTextPasswordInvalidate(user: User): Boolean
}