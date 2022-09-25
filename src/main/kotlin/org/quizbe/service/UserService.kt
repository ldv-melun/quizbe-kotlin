package org.quizbe.service

import org.quizbe.dto.UserDto
import org.quizbe.model.Role
import org.quizbe.model.User
import org.springframework.data.domain.Page
import org.springframework.validation.BindingResult
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

interface UserService {
    @Throws(SQLIntegrityConstraintViolationException::class)
    fun saveUserFromUserDto(userDto: UserDto): User
    fun findByEmail(email: String?): User?
    fun findByUsername(username: String?): User?
    fun findAll(): MutableIterable<User?>
    fun findUserDtoById(id: Long): UserDto
    fun delete(user: User)
    fun findById(id: Long): Optional<User?>
    fun save(user: User)
    fun flipflopUserRole(user: User, role: Role?)
    fun userUpdatePassword(user: User, password: String): Boolean
    fun invalidePasswordBySetWithDefaultPlainTextPassord(user: User)
    fun checkAddUpdateUser(userDto: UserDto, bindingResult: BindingResult)
    fun updateDefaultPlainTextPassword(user: User)
    fun getPaginatedUsers(pageNo: Int = 0, pageSize: Int = 10, sortBy: String = "id"): List<User>
    fun findPaginated(pageNo: Int, pageSize: Int, sortField: String, sortDirection: String): Page<User>

}