package org.quizbe.service

import org.quizbe.dao.UserRepository
import org.quizbe.model.Role
import org.quizbe.model.User
import org.quizbe.model.User.Companion.generateRandomPassword
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

@Service
class CustomUserServiceDetails : UserDetailsService {
    var logger : Logger = LoggerFactory.getLogger(CustomUserServiceDetails::class.java)

//    private val VALID_HOURS_DEFAULT_PW =  QuizbeProperties().pwLifeTimeHours

    @Autowired
    lateinit var userService : UserRepository

    @Transactional
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {

        // voir aussi  (Implement UserDetails)
        //  https://www.codejava.net/frameworks/spring-boot/spring-boot-security-authentication-with-jpa-hibernate-and-mysql
        var user = userService.findByEmail(username)
        if (user == null) {
            user = userService.findByUsername(username)
            if (user == null) {
                throw UsernameNotFoundException("Could not find user")
            }
        }

        // force user to change his password
        val authorities: List<GrantedAuthority>
        authorities = if (user.mustChangePassword()) {
            logger.info("user do change password")
            if (user.hasDefaultPlainTextPasswordInvalidate()) {
                logger.info("user has password expired, so we set a new default password to him")
                updateDefaultPlainTextPassword(user)
            }
            ArrayList<GrantedAuthority>(Arrays.asList(SimpleGrantedAuthority("CHANGE_PW")))
        } else {
            // logger.info("user do no change password");
            getUserAuthority(user.roles)
        }
        return buildUserForAuthentication(user, authorities)
    }

    private fun getUserAuthority(userRoles: Set<Role>): List<GrantedAuthority> {
        val roles: MutableSet<GrantedAuthority> = HashSet()
        for (role in userRoles) {
            roles.add(SimpleGrantedAuthority(role.name))
        }
        return ArrayList(roles)
    }

    private fun buildUserForAuthentication(user: User, authorities: List<GrantedAuthority>): UserDetails {
        return org.springframework.security.core.userdetails.User(user.username, user.password,
                user.isEnabled, true, true, true, authorities)
    }
//
//

    private fun updateDefaultPlainTextPassword(user: User) {
        user.defaultPlainTextPassword = generateRandomPassword(8)
        val encoder = BCryptPasswordEncoder()
        // pas moyen de passer par un bean pour l'encoder à cause d'une réf circulaire
        user.password = encoder.encode(user.defaultPlainTextPassword)
        user.dateDefaultPassword = LocalDateTime.now()
        user.dateUpdatePassword = null
        userService.save(user)
    }
}