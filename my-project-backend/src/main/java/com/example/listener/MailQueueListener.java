package com.example.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Message queue listener for handling email sending
 */
@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    /**
     * Handles email sending
     * @param data email information
     */
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = data.get("email").toString();
        Integer code = (Integer) data.get("code");
        SimpleMailMessage message = switch (data.get("type").toString()) {
            case "register" ->
                    createMessage("Welcome to our website",
                            "Your registration verification code is: " + code + ". It is valid for 3 minutes. For your account's security, please do not disclose the verification code to others.",
                            email);
            case "reset" ->
                    createMessage("Password Reset Email",
                            "Hello, you are performing a password reset operation. The verification code is: " + code + ". It is valid for 3 minutes. If this is not your operation, please ignore it.",
                            email);
            default -> null;
        };
        if (message == null) return;
        sender.send(message);
    }

    /**
     * Quickly encapsulates a simple email message entity
     * @param title the title
     * @param content the content
     * @param email the recipient
     * @return the email entity
     */
    private SimpleMailMessage createMessage(String title, String content, String email){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}
