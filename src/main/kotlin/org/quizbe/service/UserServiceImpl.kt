package org.quizbe.service

import org.quizbe.dao.RoleRepository
import org.quizbe.dao.UserRepository
import org.quizbe.dto.UserDto
import org.quizbe.exception.UserNotFoundException
import org.quizbe.model.Role
import org.quizbe.model.User
import org.quizbe.model.User.Companion.generateRandomPassword
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import org.springframework.validation.BindingResult
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors


@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val topicService: TopicService,
    private val ratingService: RatingService,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    ) : UserService {

    var logger : Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    override fun saveUserFromUserDto(userDto: UserDto): User {
        val roleUserDB = roleRepository.findByName("USER")
        Assert.notNull(roleUserDB, "Role USER impossible access")
        val user: Optional<User?>
        if (userDto.id != null) {
            user = userRepository.findById(userDto.id!!)
            return if (user.isPresent) {
                val updateUser = user.get()
                updateUser.email = userDto.email
                updateUser.username = userDto.username
                updateUser.password = bCryptPasswordEncoder.encode(userDto.password)
                try {
                    userRepository.save(updateUser)
                } catch (e: Exception) {
                    logger.warn("Exception userRepository.save : " + e.message + " " + e.javaClass.name)
                    throw e
                }
            } else {
                throw UserNotFoundException("Invalid user Id:" + userDto.id)
            }
        }
        val roles: MutableSet<Role> = mutableSetOf()
        if (userDto.roles.isEmpty()) {
            roles.add(roleUserDB!!)
        } else {
            for (r in userDto.roles) {
                val roleDB = roleRepository.findByName(r)
                if (roleDB != null) {
                    roles.add(roleDB)
                }
            }
        }
        val newUser = User(userDto.username,
                userDto.email,
                bCryptPasswordEncoder.encode(userDto.password),
                roles)
        save(newUser)
        return newUser
    }

    override fun findByEmail(email: String?): User? {
        return userRepository.findByEmail(email)
    }

    override fun findByUsername(username: String?): User? {
        return userRepository.findByUsername(username)
    }

    override fun findAll(): MutableIterable<User?> {
        return userRepository.findAll()
    }

    override fun findUserDtoById(id: Long): UserDto {
        val user = userRepository.findById(id)
        return if (user.isPresent) {
            fromUserToUserDto(user.get())
        } else {
            throw UserNotFoundException("Invalid user Id:$id")
        }
    }

    override fun delete(user: User) {
        user.roles.clear()

        for (topic in user.subscribedTopics) {
            topic.subscribers.remove(user)
            topicService.save(topic)
        }

        for (rating in ratingService.findAllByUser(user)) {
            rating?.also { ratingService.delete(rating) }
        }
        userRepository.delete(user)
    }

    override fun findById(id: Long): Optional<User?> {
        return userRepository.findById(id)
    }

    override fun save(user: User) {
        if (user.defaultPlainTextPassword == null) {
            user.defaultPlainTextPassword = generateRandomPassword(8)
            user.dateUpdatePassword = LocalDateTime.now()
        }
        userRepository.save(user)
    }

    /**
     * Add or remove role of user (flipflop)
     *
     * @param role role too add or remove
     */
    override fun flipflopUserRole(user: User, role: Role?) {
        if (user.roles.contains(role)) {
            user.roles.remove(role)
        } else {
            user.roles.add(role!!)
        }
        userRepository.save(user)
    }

    private fun fromUserToUserDto(user: User): UserDto {
        val userDto = UserDto(user.username, user.email, user.password)
        userDto.id = user.id
        userDto.roles = user.roles.stream().map { r: Role -> r.name }.collect(Collectors.toSet())
        return userDto
    }

    /**
     * Set password only if password <> default clear password of user
     *
     * @param user
     * @param password new password
     * @return true if set or false else
     */
    override fun userUpdatePassword(user: User, password: String): Boolean {
        if (user.defaultPlainTextPassword!!.trim { it <= ' ' }.equals(password.trim { it <= ' ' }, ignoreCase = true)) {
            return false
        }
        user.password = bCryptPasswordEncoder.encode(password)
        user.dateUpdatePassword = LocalDateTime.now()
        userRepository.save(user)
        return true
    }

    /**
     * Set password => default password
     *
     * @param user
     */
    override fun invalidePasswordBySetWithDefaultPlainTextPassord(user: User) {
        user.password = bCryptPasswordEncoder.encode(user.defaultPlainTextPassword)
        user.dateDefaultPassword = LocalDateTime.now()
        user.dateUpdatePassword = null
        userRepository.save(user)
    }

    override fun updateDefaultPlainTextPassword(user: User) {
        user.defaultPlainTextPassword = generateRandomPassword(8)
        invalidePasswordBySetWithDefaultPlainTextPassord(user)
    }

    /**
     * Check if userDto can be save in DB
     *
     * @param userDto
     * @param bindingResult out, result of check
     */
    override fun checkAddUpdateUser(userDto: UserDto, bindingResult: BindingResult) {
        val bundle = ResourceBundle.getBundle("i18n/validationMessages", LocaleContextHolder.getLocale())
        var userExists = findByUsername(userDto.username)
        if (userExists != null && (userDto.id == null || userDto.id !== userExists.id)) {
            val errorMessageDefault = bundle.getString("user.username.already.exist")
            val key = "user.username.already.exist"
            bindingResult
                    .rejectValue("username", key, errorMessageDefault)
        }
        userExists = findByEmail(userDto.email)
        if (userExists != null && (userDto.id == null || userDto.id !== userExists.id)) {
            val errorMessageDefault = bundle.getString("user.email.already.exist")
            val key = "user.email.already.exist"
            bindingResult
                    .rejectValue("email", key, errorMessageDefault)
        }
    }

    override fun getPaginatedUsers(pageNo: Int, pageSize: Int, sortBy: String): List<User> {
        val paging: Pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy))
        val pagedResult: Page<User> = userRepository.findAll(paging)
        return if (pagedResult.hasContent()) {
            pagedResult.content
        } else {
            ArrayList<User>()
        }
    }

    override fun findPaginated(pageNo: Int, pageSize: Int, sortField: String, sortDirection: String): Page<User> {
        val sort =
            if (sortDirection.equals(Sort.Direction.ASC.name, ignoreCase = true))
                Sort.by(sortField).ascending()
            else
                Sort.by(sortField).descending()

        val pageable: Pageable = PageRequest.of(pageNo - 1, pageSize, sort)
        return this.userRepository.findAll(pageable)
    }


}