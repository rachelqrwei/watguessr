package com.gooners.watguessr.service;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.gooners.watguessr.entity.EmailVerification;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.repository.EmailVerificationRepository;
import com.gooners.watguessr.repository.UserRepository;

import jakarta.transaction.Transactional;

@Component
@Transactional
public class EmailVerificationService {

    private EmailVerificationRepository emailVerificationRepository;
    private JavaMailSender mailSender;
    private UserRepository userRepository;
    
    @Value("${frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

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
        // check if otp exists for a user. -> update existing otp instance
        Optional<EmailVerification> ev = emailVerificationRepository.findFirstUnverifiedByEmail(to);
        var otp = generateOTP();
        
        // Get user to include username in the redirect URL (do this first)
        User user = userRepository.findByEmailAddress(to);
        String username = user != null ? user.getUsername() : "user";
        
        if (ev.isPresent()) {
            EmailVerification existingEV = ev.get(); // unwrap
            existingEV.setCode(otp);
            existingEV.setExpiry(LocalDateTime.now().plusMinutes(5));
            emailVerificationRepository.save(existingEV);
        } else {
            EmailVerification emailVerification = new EmailVerification();
            // Only set user if it exists, otherwise leave it null for now
            if (user != null) {
                emailVerification.setUser(user);
            }
            emailVerification.setCode(otp);
            emailVerification.setExpiry(LocalDateTime.now().plusMinutes(5));
            emailVerificationRepository.save(emailVerification);
        }
        
        // Create the OTP redirect URL
        String redirectUrl = frontendBaseUrl + "/?email=" + to + "&username=" + username + "&action=send-otp";
        
        String subject = "Verify Your WatGuessr Account";
        String body = "Hi " + username + ",\n\n" +
                     "Thanks for signing up for WatGuessr! To complete your account verification, please use the following verification code:\n\n" +
                     "VERIFICATION CODE: " + otp + "\n\n" +
                     "This code will expire in 5 minutes.\n\n" +
                     "\n\n" +
                     "If you accidentally close the verification modal, you can always resend it with the following link:\n" +
                     redirectUrl;

        sendEmail(to, subject, body);
    }
    
    /**
     * Send OTP email with a specific username (useful during signup when user doesn't exist yet)
     */
    public void prepareToSendEmail(String to, String username) {
        // check if otp exists for a user. -> update existing otp instance
        Optional<EmailVerification> ev = emailVerificationRepository.findFirstUnverifiedByEmail(to);
        var otp = generateOTP();
        
        if (ev.isPresent()) {
            EmailVerification existingEV = ev.get(); // unwrap
            existingEV.setCode(otp);
            existingEV.setExpiry(LocalDateTime.now().plusMinutes(5));
            emailVerificationRepository.save(existingEV);
        } else {
            EmailVerification emailVerification = new EmailVerification();
            // Try to get user if it exists
            User user = userRepository.findByEmailAddress(to);
            if (user != null) {
                emailVerification.setUser(user);
            }
            emailVerification.setCode(otp);
            emailVerification.setExpiry(LocalDateTime.now().plusMinutes(5));
            emailVerificationRepository.save(emailVerification);
        }
        
        // Create the OTP redirect URL using the provided username
        String redirectUrl = frontendBaseUrl + "/?email=" + to + "&username=" + username + "&action=send-otp";
        
        String subject = "Verify Your WatGuessr Account - OTP Code";
        String body = "Hello " + username + ",\n\n" +
                "Thank you for signing up for WatGuessr! To complete your account verification, please use the following verification code:\n\n" +
                "🔐 Verification Code: " + otp + "\n\n" +
                "⏰ This code will expire in 5 minutes.\n\n" +
                "📱 Quick Verification:\n" +
                "Click the link below to verify your account:\n" +
                redirectUrl + "\n\n" +
                "🔗 Manual Verification:\n" +
                "If the link doesn't work, copy and paste it into your browser:\n" +
                redirectUrl + "\n\n" +
                "🪿 gauk-guak! Your journey through campus magic is just beginning!\n" +
                "Step right into the fun world of WatGuessr," + "\n\n" +
                "The WatGuessr Team";

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

        // Mark EmailVerification as verified
        rec.setVerified(true);
        emailVerificationRepository.save(rec);
        
        // Also mark the User as verified so they can login
        if (rec.getUser() != null) {
            User user = rec.getUser();
            user.setVerified(true);
            userRepository.save(user);
        }
        
        return true;
    }

}
