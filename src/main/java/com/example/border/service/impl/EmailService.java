package com.example.border.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String email, String code) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setTo(email);
            message.setSubject("Email Verification Code");
            message.setText("Your verification code is " + code, false);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }
    }

    public void sendResetPasswordEmail(String to, String token) {
        String resetLink = "http://localhost:8080/api/v1/auth/reset-password?token=" + token;
        String htmlContent = """
                <html>
                <body>
                    <h3>Password Reset Request</h3>
                    <p>Click the link below to reset your password:</p>
                    <a href="%s">Reset Password</a>
                </body>
                </html>
                """.formatted(resetLink);

        try {
            var message = javaMailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Password Reset Request");
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }
    }
}
