package org.quizbe

import org.quizbe.config.QuizbeProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(QuizbeProperties::class)
class QuizbeApplication

fun main(args: Array<String>) {
    runApplication<QuizbeApplication>(*args)
}

//
//open class QuizbeApplication {
//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            SpringApplication.run(QuizbeApplication::class.java, *args)
//        }
//    }
//}
