package org.quizbe.controller

import org.quizbe.dto.UserDto
import org.quizbe.exception.UserNotFoundException
import org.quizbe.model.User
import org.quizbe.service.EmailServiceImpl
import org.quizbe.service.RoleService
import org.quizbe.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RequestMapping("/admin")
@Controller
class AdminController @Autowired constructor(
    private val userService: UserService,
    private val roleService: RoleService,
    private val emailService: EmailServiceImpl
) {
    var logger: Logger = LoggerFactory.getLogger(AdminController::class.java)

    @GetMapping("/users")
    fun showUserList(model: Model): String {
        model.addAttribute("users", userService.findAll())
        model.addAttribute("allRoles", roleService.findAllByOrderByName())
        return "admin/list-users"
    }

    @PostMapping("/addusers")
    fun addUsers(request: HttpServletRequest, redirAttrs: RedirectAttributes): String {
        val users = request.getParameter("users").split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val roles: MutableSet<String> = HashSet()
        var cptNewUser = 0
        var cptUsers = 0
        roles.add("USER")
        for (user in users) {
            val userAttrib = user.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (userAttrib.size < 3) continue
            cptUsers++
            val userDto = UserDto(userAttrib[0].trim { it <= ' ' },
                userAttrib[1].trim { it <= ' ' },
                userAttrib[2].trim { it <= ' ' })
            userDto.roles = roles
            try {
                userService.saveUserFromUserDto(userDto)
                cptNewUser++
            } catch (e: Exception) {
                logger.warn("""
    Exception in addUsers : $userDto
    ${e.message}
    """.trimIndent()
                )
                //
            }
        }
        // TODO show users not added
        redirAttrs.addFlashAttribute("simpleMessage", "$cptNewUser on $cptUsers added")
        return "redirect:/admin/users"
    }

    @GetMapping("/delete/{id}")
    fun deleteUser(@PathVariable("id") id: Long, model: Model?): String {
        val user: User? = userService.findById(id)
            .orElseThrow { IllegalArgumentException("Invalid user Id:$id") }
        user?.let { userService.delete(it) }
        return "redirect:/admin/users"
    }

    @PostMapping("/role")
    fun updateRoleUser(request: HttpServletRequest): String {
        val id = request.getParameter("id").toLong()
        val roleName = request.getParameter("rolename")
        val nameCurrentUser = request.userPrincipal.name
        val currentUser = userService.findByUsername(nameCurrentUser)
        val userToUpdate = userService.findById(id)
            .orElseThrow { IllegalArgumentException("Invalid user Id:$id") }
        logger.info("userToUpdate : $userToUpdate")
        logger.info("currentUser : $currentUser")
        logger.info("roleName : $roleName")

        // "super admin" stay admin
        if (roleName === "ADMIN") {
            if ((currentUser!!.id == 1L) && (userToUpdate!!.id == 1L)) {
                // Le super utilisateur reste admin !
                return "redirect:/admin/users"
            }
            if (currentUser.id!! > 1L) {
                // non autorisé à gérer les rôles ADMIN
                return "redirect:/admin/users"
            }
        }
        val role = roleService.findByName(roleName) ?: throw IllegalArgumentException("Invalid role name:$roleName")

        // boolean userToUpdateIsAdmin = userToUpdate.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
        userService.flipflopUserRole(userToUpdate!!, role)
        return "redirect:/admin/users"
    }

    @GetMapping(value = ["/register"])
    fun register(@ModelAttribute userDto: UserDto): String {
        return "/admin/registration"
    }

    @PostMapping(value = ["/register"])
    fun registerPost(
        @ModelAttribute userDto: @Valid UserDto?,
        bindingResult: BindingResult,
        model: Model,
        redirAttrs: RedirectAttributes
    ): String {
        userService.checkAddUpdateUser(userDto!!, bindingResult)
        if (bindingResult.hasErrors()) {
            return "/admin/registration"
        }
        try {
            userService.saveUserFromUserDto(userDto)
        } catch (e: Exception) {
            model.addAttribute("errorMessage", "error.message")
            redirAttrs.addFlashAttribute("errorMessage", "email.error.force.update.pw.message")
            return "/admin/registration"
        }

        return "redirect:/admin/users"
    }

    @GetMapping(value = ["/resetpw"])
    fun resetpw(redirAttrs: RedirectAttributes, id: Long): String {
        val user: User? = userService.findById(id).orElseThrow { UserNotFoundException() }
        userService.invalidePasswordBySetWithDefaultPlainTextPassord(user!!)
        try {
            val messageEmailBody = "Please go to <a href=\"https://quizbe.org\">https://quizbe.org</a> <br>" +
                    "for change your password<br>" +
                    "by pre-connect with this default password : <pre>" + user.defaultPlainTextPassword + "</pre>"
            logger.info("Send email to " + user.email)
            emailService.sendSimpleMessage(user.email, "Update PW", messageEmailBody)
            // don't work with parameter...
            //  redirAttrs.addFlashAttribute("successMessage", "#{${email.force.update.pw.message}("+user.getEmail()+")}");
            redirAttrs.addFlashAttribute("successMessage", "email.force.update.pw.message")
        } catch (e: Exception) {
            e.printStackTrace()
            redirAttrs.addFlashAttribute("errorMessage", "email.error.force.update.pw.message")
        }
        return "redirect:/admin/users"
    }
}