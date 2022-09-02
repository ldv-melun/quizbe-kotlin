package org.quizbe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class EmailServiceImpl {

  @Autowired
  private JavaMailSender emailSender;

  public void sendSimpleMessage(
          String to, String subject, String messageBody) {


//
//    MimeMessage mimeMessage = mailSender.createMimeMessage();
//    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
//    String htmlMsg = text;

    // mimeMessage.setContent(htmlMsg, "text/html"); /** Use this or below line **/
    // https://stackoverflow.com/questions/5289849/how-do-i-send-html-email-in-spring-mvc
    try {

      MimeMessage message = emailSender.createMimeMessage();

      message.setSubject(subject);
      MimeMessageHelper helper;
      helper = new MimeMessageHelper(message, true);
      helper.setText(messageBody, true); // Use this or above line.
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom("noreply@quizbe.org");
      emailSender.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
    }

//
//    message.setFrom("noreply@quizbe.org");
//    message.setTo(to);
//    message.setSubject(subject);
//    message.setText(text);
//    emailSender.send(message);

  }
}
