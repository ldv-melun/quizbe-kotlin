package org.quizbe.controller

import org.quizbe.dto.UserDto
import org.quizbe.exception.UserNotFoundException
import org.quizbe.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.*
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RequestMapping("/user")
@Controller
class UserController @Autowired constructor(private val userService: UserService) {
    var logger : Logger = LoggerFactory.getLogger(UserController::class.java)

    @GetMapping(value = ["/edit/{id}", "/edit/"])
    fun showUpdateForm(@PathVariable("id") id: Optional<Long>, model: Model, request: HttpServletRequest): String {
        val currentUser = userService.findByUsername(request.userPrincipal.name)
                ?: throw UserNotFoundException()
        if (id.isPresent) {
            try {
                if (!request.isUserInRole("ADMIN") && currentUser.id != id.get()) {
                    throw AccessDeniedException("edit user")
                }
                val userDto = userService.findUserDtoById(id.get())
                // logger.info("userDto pw :${userDto.password}") pw is hashed
                userDto.password=""
                model.addAttribute("userDto", userDto)
            } catch (ex: UserNotFoundException) {
                throw UserNotFoundException(ex)
            }
        } else {
            // user self update
            try {
                val userDto = userService.findUserDtoById(currentUser.id!!)
                model.addAttribute("userDto", userDto)
            } catch (ex: UserNotFoundException) {
                throw UserNotFoundException(ex)
            }
        }
        return "/user/update-user"
    }

    @PostMapping("/update/{id}")
    fun updateUser(@ModelAttribute userDto: @Valid UserDto,
                   bindingResult: BindingResult,
                   request: HttpServletRequest,
                   redirAttrs: RedirectAttributes,
                   model: Model): String {

        val currentUser = userService.findByUsername(request.userPrincipal.name) ?: throw UserNotFoundException()

        if (!request.isUserInRole("ADMIN") && currentUser.id != userDto.id) {
            throw AccessDeniedException("update user")
        }

        if (bindingResult.hasErrors()) {
            return "/user/update-user"
        }
        userService.checkAddUpdateUser(userDto, bindingResult)
        if (bindingResult.hasErrors()) {
            return "/user/update-user"
        }
        try {
            userService.saveUserFromUserDto(userDto)
        } catch (e: Exception) {
            logger.warn("SQL Integrity Exception in updateUser : " + e.message)
            model.addAttribute("errorMessage", "error.message")
            return "/user/update-user"
        }

        // ok, staff done

        return if (request.isUserInRole("ADMIN")) {
            // flash message
            redirAttrs.addFlashAttribute("message", "success.message")
            "redirect:/admin/users"
        } else {
            try {
                request.logout()
            } catch (e: ServletException) {
                // ??
                logger.info("Error logout : " + e.message)
            }
            "redirect:/"
        }
    }
}