package com.gooners.watguessr.controller;

import com.gooners.watguessr.config.RateLimit;
import com.gooners.watguessr.dto.GoogleSignupRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

@Controller
@RequestMapping("api/auth")
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    public AuthController(
            UserService userService,
            EmailVerificationService emailVerificationService,
            AuthenticationService authenticationService,
            UserMapper userMapper) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
        this.authenticationService = authenticationService;
        this.userMapper = userMapper;
    }

    @PutMapping("/login")
    @RateLimit(requests = 5, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many login attempts. Please try again later.")
    public ResponseEntity<UserDto> login(
            @RequestBody @Valid UserLoginDto loginDto,
            HttpServletResponse response) {

        // Authenticate and set HttpOnly cookie
        UserDto userDto = authenticationService.authenticate(loginDto, response);

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/me")
    @RateLimit(requests = 100, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many profile requests.")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = jwt.getSubject(); // same as jwt.getClaim("sub")

        User user = userService.findByUsername(username);
        UserDto userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/signup")
    @RateLimit(requests = 3, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many signup attempts. Please try again later.")
    public ResponseEntity<?> signup(@RequestBody @Valid UserSignupDto dto) {
        userService.signup(dto);
        return ResponseEntity.ok("Account created");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Overwrite the HttpOnly cookie with empty value and immediate expiration
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true) // set true if using HTTPS
                .path("/")
                .maxAge(0) // expire immediately
                .sameSite("Strict") // optional: adjust according to your setup
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/send-otp")
    @RateLimit(requests = 3, timeWindow = 5, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many OTP requests. Please wait before requesting again.")
    public String sendOtp(@RequestParam String to) {
        emailVerificationService.prepareToSendEmail(to);
        return "OTP sent to " + to;
    }

    @PostMapping("/verify-otp")
    @RateLimit(requests = 10, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many OTP verification attempts.")
    public ResponseEntity<Void> verifyOtp(@RequestParam String email, @RequestParam String submittedOtp) {
        var success = emailVerificationService.verify(email, submittedOtp); // checks + side effects. String email,
                                                                            // String submittedCode
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }

    @PostMapping("/google-signup")
    @RateLimit(requests = 5, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many Google signup attempts.")
    public ResponseEntity<UserDto> googleSignup(@RequestBody GoogleSignupRequest request,
            HttpServletResponse response) {
        try {
            // Create user from Google credentials
            User user = userService.createUserFromGoogle(request.getEmail(), request.getName(), request.getPicture());

            // Authenticate the user and set JWT cookie
            UserDto userDto = authenticationService.authenticateGoogleUser(user, response);

            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
