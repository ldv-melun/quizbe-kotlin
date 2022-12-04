package org.quizbe.controller

import javax.servlet.http.HttpServletRequest

data class ParamPagination(val pageNo: String, val pageSize: String, val sortBy: String, val sortDir: String) {
}

/**
 * Obtenir des valeurs de pagination transmises comme paramètre de l'URL (de la route)
 *
 * @param request
 * @return 4 valeurs (dans un objet de type ParamPagination)
 */
fun getParamPaginationFromRequest(request: HttpServletRequest): ParamPagination {
    return ParamPagination(
        request.getParameter("pageNo") ?: "1",
        request.getParameter("pageSize") ?: "10",
        request.getParameter("sortBy") ?: "id",
        request.getParameter("sortDir") ?: "asc"
    )
}