package org.quizbe.controller

import org.quizbe.config.QuizbeGlobals
import org.quizbe.dto.UserDto
import org.quizbe.exception.UserNotFoundException
import org.quizbe.model.User
import org.quizbe.service.QuizbeEmailService
import org.quizbe.service.RoleService
import org.quizbe.service.UserService
import org.quizbe.utils.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
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
    private val quizbeEmailService: QuizbeEmailService
) {
    var logger: Logger = LoggerFactory.getLogger(AdminController::class.java)

    @GetMapping("/users")
    fun showUserList(model: Model): String {
        return this.getPaginatedUsers(1, 2, "id", "asc", model) // "admin/list-users"
    }

    @GetMapping("/users2")
    fun getPaginatedUsers(
        @RequestParam(defaultValue = "0") pageNo : Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "id") sortBy : String,
        @RequestParam(defaultValue = "asc") sortDir : String,
        model: Model): String {

//        val pageSize = 5

        val page: Page<User> = userService.findPaginated(pageNo, pageSize, sortBy, sortDir)
        val listUsers: List<User> = page.content

        model.addAttribute("currentPage", pageNo)
        model.addAttribute("totalPages", page.totalPages)
        model.addAttribute("totalItems", page.totalElements)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("sortBy", sortBy)
        model.addAttribute("sortDir", sortDir)
        model.addAttribute("reverseSortDir", if (sortDir == "asc") "desc" else "asc")

        model.addAttribute("users", listUsers) //userService.getPaginatedUsers(pageNo, pageSize,sortBy))
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
    fun resetpw(redirAttrs: RedirectAttributes, id: Long, request: HttpServletRequest): String {
        val user: User? = userService.findById(id).orElseThrow { UserNotFoundException() }
        userService.invalidePasswordBySetWithDefaultPlainTextPassord(user!!)
        // async call
        quizbeEmailService.sendMailAfterSetDefaultPwPlainText(user, Utils.getBaseUrl(request))
        redirAttrs.addFlashAttribute(QuizbeGlobals.Constants.SIMPLE_MESSAGE, "Message being sent to ${user.email}...")

//        if (quizbeEmailService.sendMailAfterSetDefaultPwPlainText(user, Utils.getBaseUrl(request))) {
//            redirAttrs.addFlashAttribute("successMessage", "email.force.update.pw.message")
//        } else {
//            redirAttrs.addFlashAttribute("errorMessage", "email.error.force.update.pw.message")
//        }
        return "redirect:/admin/users"
    }
}