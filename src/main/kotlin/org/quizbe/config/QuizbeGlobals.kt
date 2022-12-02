package org.quizbe.config

import org.springframework.stereotype.Component

@Component
class QuizbeGlobals(private val properties: QuizbeProperties) {

    val pwLifeTimeHours: Long
        get() = properties.pwLifeTimeHours

    object Constants {
        const val SUCCESS_MESSAGE = "successMessage"
        const val ERROR_MESSAGE = "errorMessage"
        const val SIMPLE_MESSAGE = "simpleMessage"
    }

}