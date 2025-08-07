package com.gooners.watguessr.utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class Utility {

    @Autowired
    private JavaMailSender mailSender;
    // for 2FA
    public String generateOTP() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6-digit
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }


}
