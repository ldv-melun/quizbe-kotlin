package org.quizbe.config

import org.springframework.stereotype.Component

@Component
class QuizbeGlobals {
    companion object {
        const val pwLifeTimeHours: Int = 48+1
    }

}