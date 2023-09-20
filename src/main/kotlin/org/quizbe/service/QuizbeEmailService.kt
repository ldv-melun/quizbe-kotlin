package org.quizbe.service

import org.quizbe.model.Question
import org.quizbe.model.Rating
import org.quizbe.model.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import javax.mail.MessagingException

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
    fun sendMailToDesignerAfterCreateOrUpdateRating(designerUser: User, question: Question, comment: String, baseUrl: String) { //}: Boolean {
        val messageEmailBody = "A new comment is coming for your question quiz in ref to topic : ${question.topic.name}[scope: ${question.scope.name}] : \" ${question.name} \" on " +
                "<a href=\"" + baseUrl + "\">" + "Quizbe" + "</a>" + "\n<br><p>" + comment + "</p>"
        try {
            this.sendSimpleMessage(designerUser.email, "New or Update comment", messageEmailBody)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Async
    fun sendMailToUserRatingAfterObsolete(rating: Rating, baseUrl: String) {
        val messageEmailBody = "Your rating is now Obsolete, in ref to topic : ${rating.question?.topic?.name}[scope: ${rating.question?.scope?.name}](${rating.question?.name}) on " +
                "<a href=\"" + baseUrl + "\">" + "Quizbe" + "</a>" + "\n" +
                "<br><p> Your comment : " + rating.comment + "</p>"
        try {
            this.sendSimpleMessage(rating.user?.email+"", "Your rating is obsolete", messageEmailBody)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
