package org.quizbe.config

import org.springframework.stereotype.Component

@Component
class QuizbeGlobals(private val properties: QuizbeProperties) {

    val pwLifeTimeHours: Long
        get() = properties.pwLifeTimeHours

}