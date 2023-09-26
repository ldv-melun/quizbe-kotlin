package org.quizbe.utils

import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.servlet.http.HttpServletRequest
import org.quizbe.model.Question
import java.time.LocalDateTime

class Utils {
    companion object {
        fun getBaseUrl(request: HttpServletRequest): String {
            return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString()
        }

        final fun compareByDesignerAsc( q1: Question, q2: Question): Int {
            return q1.designer.compareTo(q2.designer)
        }

        final fun compareByDesignerDesc( q1: Question, q2: Question): Int {
            return q2.designer.compareTo(q1.designer)
        }

        final fun compareByNameAsc( q1: Question, q2: Question): Int {
            return q1.name.compareTo(q2.name)
        }

        final fun compareByNameDesc( q1: Question, q2: Question): Int {
            return q2.name.compareTo(q1.name)
        }


        final fun compareByUpdateDateAsc( q1: Question, q2: Question): Int {
            return q1.dateUpdate!!.compareTo(q2!!.dateUpdate)
        }

        final fun compareByUpdateDateDesc( q1: Question, q2: Question): Int {
            return q2.dateUpdate!!.compareTo(q1!!.dateUpdate)
        }

        final fun compareByScopeAsc( q1: Question, q2: Question): Int {
            return q1.scope.name.compareTo(q2.scope.name)
        }

        final fun compareByScopeDesc( q1: Question, q2: Question): Int {
            return q2.scope.name.compareTo(q1.scope.name)
        }

    }
}
