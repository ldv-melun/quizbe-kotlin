package org.quizbe.controller

import org.quizbe.dto.PasswordDto
import org.quizbe.exception.UserNotFoundException
import org.quizbe.service.UserService
import org.quizbe.validator.PasswordDtoValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import java.security.Principal
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@Controller
class IndexController @Autowired constructor(
    private val userService: UserService,
    private val passwordDtoValidator: PasswordDtoValidator
) {
    var logger : Logger = LoggerFactory.getLogger(IndexController::class.java)

    @GetMapping(value = ["/"])
    fun index(): String {
        return "/main/index"
    }

    @GetMapping(value = ["/login"])
    fun login(): String {
        return "/main/login"
    }

    /**
     * @See org.quizbe.config.WebSecurityConfiguration
     */
    @GetMapping(value = ["/access-denied"])
    fun accessDenied(): String {
        return "/error/access-denied"
    }

    /*
   * come from QuizbeAccessDeniedHandler and CustomUserServiceDetails
   */
    @PreAuthorize("hasRole('CHANGE_PW')")
    @GetMapping("/douser/updatepw")
//    @Throws(ServletException::class)
    fun showUpdatePassword(@ModelAttribute passwordDto: PasswordDto?, request: HttpServletRequest, model: Model?): String {
        userService.findByUsername(request.userPrincipal.name) ?: throw UserNotFoundException("Invalid User")
        // defensive code
        return "main/update-user-pw"
    }

    @PreAuthorize("hasRole('CHANGE_PW')")
    @PostMapping("/douser/updatepw")
    fun userUpdatePassword(@ModelAttribute @Valid passwordDto: PasswordDto,
                           result: BindingResult, principal: Principal, request: HttpServletRequest): String {
        val user = userService.findByUsername(principal.name) ?: throw UserNotFoundException("Invalid User")
        if (result.hasErrors()) {
            return "main/update-user-pw"
        }
        passwordDtoValidator.validate(passwordDto, result)
        if (result.hasErrors()) {
            return "main/update-user-pw"
        }
        if (userService.userUpdatePassword(user, passwordDto.password!!)) {
            // password has changed
            request.logout()
            return "redirect:/login"
        }
        return "main/update-user-pw"
    }
}