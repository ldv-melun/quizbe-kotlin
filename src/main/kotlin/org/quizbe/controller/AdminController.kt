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

//import javax.validation.Valid

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
        return this.getPaginatedUsers(1, 12, "id", "asc", model) // "admin/list-users"
    }

    @GetMapping("/users2")
    fun getPaginatedUsers(
        @RequestParam(defaultValue = "0") pageNo : Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "id") sortBy : String,
        @RequestParam(defaultValue = "asc") sortDir : String,
        model: Model): String {

        // bad hack, hum...
        val pageSize = 12

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
            val userAttributs = user.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (userAttributs.size < 3) continue
            cptUsers++
            val userDto = UserDto(userAttributs[0].trim { it <= ' ' },
                userAttributs[1].trim { it <= ' ' },
                userAttributs[2].trim { it <= ' ' })
            userDto.roles = roles
            try {
                userService.saveUserFromUserDto(userDto)
                cptNewUser++
            } catch (e: Exception) {
                logger.warn("Exception in addUsers : $userDto")
            }
        }
        // TODO show users not added
        redirAttrs.addFlashAttribute(QuizbeGlobals.Constants.SIMPLE_MESSAGE, "$cptNewUser on $cptUsers added")

        val (pageNo, pageSize, sortBy, sortDir) = getParamPaginationFromRequest(request)

        return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
    }

    @GetMapping("/delete/{id}")
    fun deleteUser(@PathVariable("id") id: Long, model: Model?, request: HttpServletRequest): String {
        val user: User? = userService.findById(id)
            .orElseThrow { IllegalArgumentException("Invalid user Id:$id") }
        user?.let { userService.delete(it) }

        val pageNo =  request.getParameter("pageNo") ?: "1"
        val pageSize =  request.getParameter("pageSize") ?: "10"
        val sortBy =  request.getParameter("sortBy") ?: "id"
        val sortDir =  request.getParameter("sortDir") ?: "asc"

        return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
    }

    @PostMapping("/role")
    fun updateRoleUser(request: HttpServletRequest): String {
        val id = request.getParameter("id").toLong()
        val roleName = request.getParameter("rolename")
        val nameCurrentUser = request.userPrincipal.name
        val currentUser : User = userService.findByUsername(nameCurrentUser) ?: throw UserNotFoundException()
        val userToUpdate : User = userService.findById(id).get()  // throw NoSuchElementException

        val (pageNo, pageSize, sortBy, sortDir) = getParamPaginationFromRequest(request)

        logger.info("userToUpdate : $userToUpdate")
        logger.info("currentUser : $currentUser")
        logger.info("roleName : $roleName")

        // "super admin" stay admin
        if (roleName === "ADMIN") {
            if ((currentUser.id == 1L) && (userToUpdate.id == 1L)) {
                // Le super utilisateur reste admin !
                return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
            }
            if (currentUser.id!! > 1L) {
                // non autorisé à gérer les rôles ADMIN
                return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
            }
        }
        val role = roleService.findByName(roleName) ?: throw IllegalArgumentException("Invalid role name:$roleName")

        // boolean userToUpdateIsAdmin = userToUpdate.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
        userService.flipflopUserRole(userToUpdate, role)
        return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
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
        redirAttrs: RedirectAttributes,
        request: HttpServletRequest
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

        // remplacer ces 4 lignes en une seule ligne
//        val pageNo =  request.getParameter("pageNo") ?: "1"
//        val pageSize =  request.getParameter("pageSize") ?: "10"
//        val sortBy =  request.getParameter("sortBy") ?: "id"
//        val sortDir =  request.getParameter("sortDir") ?: "asc"

        val (pageNo, pageSize, sortBy, sortDir) = getParamPaginationFromRequest(request)

        return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
    }

    @GetMapping(value = ["/resetpw"])
    fun resetpw(redirAttrs: RedirectAttributes, id: Long, request: HttpServletRequest): String {
        val user: User? = userService.findById(id).orElseThrow { UserNotFoundException() }
        userService.invalidePasswordBySetWithDefaultPlainTextPassord(user!!)
        // async call
        quizbeEmailService.sendMailAfterSetDefaultPwPlainText(user, Utils.getBaseUrl(request))
        redirAttrs.addFlashAttribute(QuizbeGlobals.Constants.SIMPLE_MESSAGE, "Message being sent to ${user.email}...")

        val (pageNo, pageSize, sortBy, sortDir) = getParamPaginationFromRequest(request)

        return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
    }
}