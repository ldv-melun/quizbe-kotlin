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
import javax.servlet.http.HttpSession
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
    fun showUserList(model: Model, session: HttpSession): String {
        return this.getPaginatedUsers(1, 12, "id", "asc", model, session) // "admin/list-users"
    }

    @GetMapping("/users2")
    fun getPaginatedUsers(
        @RequestParam(defaultValue = "1") pageNo: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String,
        model: Model,
        session: HttpSession
    ): String {

        // bad hack, hum...
        val pageSize = 12

        val pageNumero : Int = if (pageNo > 0) pageNo else  1

        val page: Page<User> = userService.findPaginated(pageNumero - 1, pageSize, sortBy, sortDir)
        val listUsers: List<User> = page.content
        logger.info("User first : " + listUsers.get(0))
        val notAddedUsers: List<UserDto>? = session.getAttribute("notAddedUsers") as List<UserDto>?
        if (notAddedUsers != null) {
            model.addAttribute("notAddedUsers", notAddedUsers)
            session.removeAttribute("notAddedUsers") // add by kpu (n'ayant pas vu ou cela est réalisé... TODO vérifier tout cela
        }

        model.addAttribute("currentPage", pageNo)
        model.addAttribute("totalPages", page.totalPages)
        model.addAttribute("totalItems", page.totalElements)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("sortBy", sortBy)
        model.addAttribute("sortDir", sortDir)
        model.addAttribute("reverseSortDir", if (sortDir == "asc") "desc" else "asc")
        model.addAttribute("users", listUsers)//userService.getPaginatedUsers(pageNo, pageSize,sortBy))
        model.addAttribute("allRoles", roleService.findAllByOrderByName())
        return "admin/list-users"
    }


    @GetMapping("/users-search")
    fun getPaginatedUsersSearch(
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "1") pageNo: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String,
        model: Model,
        session: HttpSession
    ): String {

        // bad hack, hum... TODO set pageSize in a range
        val pageSize = 12

        // Select all roles minus "USER" (if USER in roles, get a bug with mysql when cal findByRole).
        // So as all users are "USER"... empty search is same action
        val roles: List<String>? =
            roleService.findAllByOrderByName()?.filterNotNull()!!.filter { it.name != "USER" }.map { it.name.toString() }
//        logger.info("Roles : " + roles.toString())

        // si la valeur de search est le nom d'un rôle, alors on recherchera tous les users ayant ce rôle
        val maybeRoleName = search.trim().uppercase()
        val findRole: Boolean = roles?.find { maybeRoleName == it  } != null

        val pageNumero : Int = if (pageNo > 0) pageNo else  1

        val page: Page<User> =
            if (findRole)
              userService.findByRole(maybeRoleName, pageNumero - 1, pageSize, sortBy, sortDir)
            else
              userService.findByUsernameLike(search, pageNumero - 1, pageSize, sortBy, sortDir)

        val listUsers: List<User> =  page.content

        model.addAttribute("currentPage", pageNo)
        model.addAttribute("totalPages", page.totalPages)
        model.addAttribute("totalItems", page.totalElements)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("sortBy", sortBy)
        model.addAttribute("sortDir", sortDir)
        model.addAttribute("reverseSortDir", if (sortDir == "asc") "desc" else "asc")
        model.addAttribute("users", listUsers)//userService.getPaginatedUsers(pageNo, pageSize,sortBy))
        model.addAttribute("allRoles", roleService.findAllByOrderByName())
        return "admin/list-users"
    }


    /**
     * Ajout des utilisateurs en lot
     * users : string de la forme: nom, email\nnom, email\n etc.
     * L'admin devra activer le mot de pass par defaut afin que les utilisateurs reçoivent le mail de chgt de pw
     */
    @PostMapping("/addusers")
    fun addUsers(
        request: HttpServletRequest,
        redirAttrs: RedirectAttributes,
        model: Model
    ): String {
        val users = request.getParameter("users").split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        logger.info("users : " + users)
        val roles: MutableSet<String> = HashSet()
        var cptNewUser = 0
        var cptUsers = 0
        val notAddedUsers = ArrayList<UserDto>()
        roles.add("USER")
        for (user in users) {
            val userAttributs = user.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (userAttributs.size < 2) continue
            cptUsers++
            val userDto = UserDto(
                userAttributs[0].trim { it <= ' ' },
                userAttributs[1].trim { it <= ' ' },
                ""
            )
            userDto.roles = roles
            try {
                userService.saveUserFromUserDto(userDto)
                cptNewUser++
            } catch (e: Exception) {
                notAddedUsers.add(userDto)
                logger.warn("Exception in addUsers : $userDto")
            }
        }
        // show users not added
        redirAttrs.addFlashAttribute("notAddedUsers", notAddedUsers)
        redirAttrs.addFlashAttribute(QuizbeGlobals.Constants.SIMPLE_MESSAGE, "$cptNewUser on $cptUsers added")

        val (pageNo, pageSize, sortBy, sortDir) = getParamPaginationFromRequest(request)

        return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
    }

    @GetMapping("/delete/{id}")
    fun deleteUser(@PathVariable("id") id: Long, model: Model?, request: HttpServletRequest): String {
        val user: User? = userService.findById(id)
            .orElseThrow { IllegalArgumentException("Invalid user Id:$id") }
        user?.let { userService.delete(it) }

        val pageNo = request.getParameter("pageNo") ?: "1"
        val pageSize = request.getParameter("pageSize") ?: "10"
        val sortBy = request.getParameter("sortBy") ?: "id"
        val sortDir = request.getParameter("sortDir") ?: "asc"

        return "redirect:/admin/users2?pageNo=$pageNo&pageSize=$pageSize&sortBy=$sortBy&sortDir=$sortDir"
    }

    @PostMapping("/role")
    fun updateRoleUser(request: HttpServletRequest): String {
        val id = request.getParameter("id").toLong()
        val roleName = request.getParameter("rolename")
        val nameCurrentUser = request.userPrincipal.name
        val currentUser: User = userService.findByUsername(nameCurrentUser) ?: throw UserNotFoundException()
        val userToUpdate: User = userService.findById(id).get()  // throw NoSuchElementException

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
