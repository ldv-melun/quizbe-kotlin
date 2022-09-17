package org.quizbe.utils

import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.servlet.http.HttpServletRequest

class Utils {
    companion object {
        fun getBaseUrl(request: HttpServletRequest): String {
            return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString()
        }
    }
}