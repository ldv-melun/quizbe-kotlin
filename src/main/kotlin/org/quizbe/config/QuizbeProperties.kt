package org.quizbe.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

// not working (see QuizbeGlobals as temporary solution)
@ConstructorBinding
@ConfigurationProperties("quizbe")
data class QuizbeProperties(var pwLifeTimeHours: Long) {

}
