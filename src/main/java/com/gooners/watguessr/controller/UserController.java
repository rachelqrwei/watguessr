package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.dto.UserLoginDto;

import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.dto.UserLoginDto;

import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.UserMapper;
import com.gooners.watguessr.service.UserService;
import com.gooners.watguessr.utils.Utility;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.dto.LeaderboardUser;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.dto.LeaderboardUser;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/user")
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    private final Utility utility;
    public UserController(UserService userService, UserMapper userMapper, Utility utility) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.utility = utility;
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


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserSignupDto dto) {
        userService.signup(dto);  // Handles uniqueness and saving
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

    @GetMapping(value = "/leaderboard")
    public QueryResults<LeaderboardUser> getLeaderboard(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        return this.userService.getLeaderboard(searchTerm, sortBy, limit, offset);
    }

    @PostMapping(value = "/send-email")
    public void sendEmail() {
        utility.sendEmail("wukenny0126@gmail.com", "Test Subject", "Hello from WatGuessr!");
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String to) {
        utility.sendOtpEmail(to);
        return "OTP sent to " + to;
    }
//    @PostMapping("/request-verification-code")
//    public ResponseEntity<?> sendCode(@RequestParam String email) {
//        // 1. Generate OTP
//        // 2. Save OTP and expiry to user/email_verification table
//        // 3. Send email
//    }
//
//    @PostMapping("/verify-code")
//    public ResponseEntity<?> verifyCode(@RequestBody CodeDto dto) {
//        // 1. Lookup code from DB
//        // 2. Check expiry
//        // 3. If valid: mark as verified
//    }

    @GetMapping(value = "/leaderboard")
    public QueryResults<LeaderboardUser> getLeaderboard(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        return this.userService.getLeaderboard(searchTerm, sortBy, limit, offset);
    }

}
