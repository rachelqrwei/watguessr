package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.UserLoginDto;
import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.service.AuthenticationService;
import com.gooners.watguessr.service.EmailVerificationService;
import com.gooners.watguessr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/auth")
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final AuthenticationService authenticationService;

    public AuthController(UserService userService, EmailVerificationService emailVerificationService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.authenticationService = authenticationService;
    }

    @PutMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDto loginDto) {
        return ResponseEntity.ok(authenticationService.authenticate(loginDto));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserSignupDto dto) {
        userService.signup(dto);
        return ResponseEntity.ok("Account created");
    }


    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String to) {
        emailVerificationService.prepareToSendEmail(to);
        return "OTP sent to " + to;
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOtp(@RequestParam String email, @RequestParam String submittedOtp) {
        var success = emailVerificationService.verify(email, submittedOtp); // checks + side effects. String email, String submittedCode
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }


}
