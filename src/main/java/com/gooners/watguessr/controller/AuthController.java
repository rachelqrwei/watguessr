package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.dto.UserLoginDto;
import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.UserMapper;
import com.gooners.watguessr.service.AuthenticationService;
import com.gooners.watguessr.service.EmailVerificationService;
import com.gooners.watguessr.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/auth")
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    public AuthController(UserService userService, EmailVerificationService emailVerificationService, AuthenticationService authenticationService, UserMapper userMapper) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.authenticationService = authenticationService;
        this.userMapper = userMapper;
    }

    @PutMapping("/login")
    public ResponseEntity<UserDto> login(
            @RequestBody @Valid UserLoginDto loginDto,
            HttpServletResponse response) {

        // Authenticate and set HttpOnly cookie
        UserDto userDto = authenticationService.authenticate(loginDto, response);

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = jwt.getSubject(); // same as jwt.getClaim("sub")
        String role = jwt.getClaim("role");
        String email = jwt.getClaim("email"); // only if you put it in when issuing token

        User user = userService.findByUsername(username);
        UserDto userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
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
