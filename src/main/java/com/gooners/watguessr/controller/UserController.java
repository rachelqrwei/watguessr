package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.dto.UserLoginDto;

import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.UserMapper;
import com.gooners.watguessr.service.UserService;
import com.gooners.watguessr.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.dto.LeaderboardUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/user")
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    private final EmailVerificationService emailVerificationService;
    public UserController(UserService userService, UserMapper userMapper, EmailVerificationService emailVerificationService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping(value = "/register")
    public void register(@RequestBody @Valid User user) {
        this.userService.create(user);
    }

    @PutMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDto loginDto) {
        User user = userService.login(loginDto.getUsername(), loginDto.getPassword());
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserSignupDto dto) {
        userService.signup(dto);
        return ResponseEntity.ok("Account created");
    }

    @GetMapping(value = "/{id}")
    public User getUser(@PathVariable UUID id) {
        return this.userService.findById(id);
    }

    @GetMapping(value = "/all")
    public List<User> getSorted(String keyword, String sortBy, int page, int pageSize) {
        return this.userService.findSorted(keyword, sortBy, page, pageSize);
    }

    @PostMapping(value = "/send-email")
    public void sendEmail() {
        emailVerificationService.sendEmail("wukenny0126@gmail.com", "Test Subject", "Hello from WatGuessr!");
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String to) {
        emailVerificationService.prepareToSendEmail(to);
        return "OTP sent to " + to;
    }

    @GetMapping(value = "/leaderboard")
    public QueryResults<LeaderboardUser> getLeaderboard(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        return this.userService.getLeaderboard(searchTerm, sortBy, limit, offset);
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
