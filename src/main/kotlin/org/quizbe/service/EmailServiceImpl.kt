package org.quizbe.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import javax.mail.MessagingException

@Component
class EmailServiceImpl {
    @Autowired
    private val emailSender: JavaMailSender? = null
    fun sendSimpleMessage(
            to: String?, subject: String?, messageBody: String?) {


//
//    MimeMessage mimeMessage = mailSender.createMimeMessage();
//    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
//    String htmlMsg = text;

        // mimeMessage.setContent(htmlMsg, "text/html"); /** Use this or below line **/
        // https://stackoverflow.com/questions/5289849/how-do-i-send-html-email-in-spring-mvc
        try {
            val message = emailSender!!.createMimeMessage()
            message.subject = subject
            val helper: MimeMessageHelper
            helper = MimeMessageHelper(message, true)
            helper.setText(messageBody!!, true) // Use this or above line.
            helper.setTo(to!!)
            helper.setSubject(subject!!)
            helper.setFrom("noreply@quizbe.org")
            emailSender.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
        }

//
//    message.setFrom("noreply@quizbe.org");
//    message.setTo(to);
//    message.setSubject(subject);
//    message.setText(text);
//    emailSender.send(message);
    }
}