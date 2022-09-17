package org.quizbe.service

import org.quizbe.controller.AdminController
import org.quizbe.model.Question
import org.quizbe.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.mail.MessagingException
import javax.servlet.http.HttpServletRequest

@Component
class QuizbeEmailService @Autowired constructor(private val emailSender: JavaMailSender) {


    val logger: Logger = LoggerFactory.getLogger(QuizbeEmailService::class.java)

    fun sendSimpleMessage(to: String, subject: String, messageBody: String) {
        // https://stackoverflow.com/questions/5289849/how-do-i-send-html-email-in-spring-mvc
        try {
            val message = emailSender.createMimeMessage()
            message.subject = subject
            val helper = MimeMessageHelper(message, true)
            helper.setText(messageBody, true) // Use this or above line.
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setFrom("noreply@quizbe.org")
            emailSender.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }

    // declare asynchrone methode : https://www.baeldung.com/spring-async
    @Async
    fun sendMailAfterSetDefaultPwPlainText(user: User, baseUrl : String) {
        try {
            val messageEmailBody = "Please go to <a href=\"$baseUrl\">$baseUrl</a> <br>" +
                    "for change your password<br>" +
                    "by pre-connect with this default password : <pre>" + user.defaultPlainTextPassword + "</pre>"
            logger.info("Send email to " + user.email)
            this.sendSimpleMessage(user.email, "Update PW", messageEmailBody)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @Async
    fun sendMailToDesignerAfterCreteOrUpdateRating(designerUser: User, question: Question, baseUrl: String) { //}: Boolean {

        val messageEmailBody = "A new comment is coming for your quiz \" ${question.name} \" on " +
                "<a href=\"" + baseUrl + "\"> url app</a>"
        this.sendSimpleMessage(designerUser.email, "New or Update comment", messageEmailBody)

        try {
            this.sendSimpleMessage(designerUser.email, "New or Update comment", messageEmailBody)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}