package com.gooners.watguessr.service;
import com.gooners.watguessr.entity.EmailVerification;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.EmailVerificationRepository;
import com.gooners.watguessr.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Transactional
public class EmailVerificationService {

    private EmailVerificationRepository emailVerificationRepository;
    private JavaMailSender mailSender;
    private UserRepository userRepository;

    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository, JavaMailSender mailSender, UserRepository userRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

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

    public void prepareToSendEmail(String to) {
        // check if otp exists ofr a user. -> update existing otp instance
        Optional<EmailVerification> ev = emailVerificationRepository.findFirstUnverifiedByEmail(to);
        var otp = generateOTP();
        if (ev.isPresent()) {
            EmailVerification existingEV = ev.get(); // unwrap
            existingEV.setCode(otp);
            existingEV.setExpiry(LocalDateTime.now().plusMinutes(5));
            emailVerificationRepository.save(existingEV);
        } else {
            EmailVerification emailVerification = new EmailVerification();
            User user = userRepository.findByEmailAddress(to);
            emailVerification.setUser(user);
            emailVerification.setCode(otp);
            emailVerification.setExpiry(LocalDateTime.now().plusMinutes(5));
            emailVerificationRepository.save(emailVerification);
        }
        String subject = "Your WatGuessr Verification Code";
        String body = "Your one time password is: " + otp + "\nIt will expire in 5 minutes.";

        sendEmail(to, subject, body);
    }

    public Boolean verify(String email, String submittedOtp) {

        var recOpt = emailVerificationRepository.findFirstUnverifiedByEmail(email);
        if (recOpt.isEmpty()) return false;

        var rec = recOpt.get();

        if (LocalDateTime.now().isAfter(rec.getExpiry())) return false;

        if (!rec.getCode().equals(submittedOtp)) {
            return false;
        }

        rec.setVerified(true);
        emailVerificationRepository.save(rec);
        return true;
    }

}
