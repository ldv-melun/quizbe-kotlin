package org.quizbe.config

import org.quizbe.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.access.AccessDeniedHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class QuizbeAccessDeniedHandler : AccessDeniedHandler {
    var logger = LoggerFactory.getLogger(QuizbeAccessDeniedHandler::class.java)

    @Autowired
    private val userService: UserService? = null
    @Throws(IOException::class, ServletException::class)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exc: AccessDeniedException) {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null) {
            logger.warn("User: " + auth.name
                    + " attempted to access the protected URL: "
                    + request.requestURI)
            val currentUser = userService!!.findByUsername(auth.name)
            if (currentUser != null && currentUser.mustChangePassword()) {
                response.sendRedirect(request.contextPath + "/douser/updatepw")
                return
            }
        } else {
            logger.info("Access denied to " + request.requestURI)
        }
        response.sendRedirect(request.contextPath + "/access-denied")
    }
}