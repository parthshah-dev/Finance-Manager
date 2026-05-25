package com.example.moneymanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;
    @Value("${app.email.enabled}")
    private boolean isEmailEnabled;

    public void sendEmail(String to, String subject, String body){
        if (!isEmailEnabled) {
            System.out.println("Email sending is disabled. Skipping email to: " + to);
            return; // Exit the method without sending
        }
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            System.out.println("Email sent successfully to " + to);
        }catch (Exception e){
           throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
