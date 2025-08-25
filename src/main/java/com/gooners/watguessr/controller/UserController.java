package com.gooners.watguessr.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gooners.watguessr.config.RateLimit;
import com.gooners.watguessr.dto.BugReportRequest;
import com.gooners.watguessr.dto.LeaderboardUser;
import com.gooners.watguessr.dto.MatchHistoryItem;
import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.dto.UserSettingsDto;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.UserMapper;
import com.gooners.watguessr.service.EmailVerificationService;
import com.gooners.watguessr.service.GameService;
import com.gooners.watguessr.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final EmailVerificationService emailVerificationService;
    private final GameService gameService;
    private final JavaMailSender mailSender;

    public UserController(UserService userService, UserMapper userMapper, EmailVerificationService emailVerificationService, GameService gameService, JavaMailSender mailSender) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.emailVerificationService = emailVerificationService;
        this.gameService = gameService;
        this.mailSender = mailSender;
    }

    @PostMapping(value = "/register")
    @RateLimit(requests = 3, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many registration attempts.")
    public void register(@RequestBody @Valid User user) {
        this.userService.create(user);
    }

    @GetMapping(value = "/{id}")
    @RateLimit(requests = 100, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many user fetch requests.")
    public UserDto getUser(@PathVariable UUID id) {
        return this.userMapper.toDto(this.userService.findById(id));
    }

    @GetMapping(value = "/all")
    @RateLimit(requests = 30, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many user list requests.")
    public List<UserDto> getSorted(String keyword, String sortBy, int page, int pageSize) {
        return this.userService.findSorted(keyword, sortBy, page, pageSize).stream().map(userMapper::toDto).toList();
    }

    @GetMapping(value = "/leaderboard")
    public ResponseEntity<QueryResults<LeaderboardUser>> getLeaderboard(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {

        return ResponseEntity.ok(userService.getLeaderboard(searchTerm, sortBy, limit, offset));
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

    @GetMapping(value = "/{id}/match-history")
    @RateLimit(requests = 50, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many match history requests.")
    public QueryResults<MatchHistoryItem> getUserMatchHistory(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        var results = gameService.getUserMatchHistory(id, limit, offset);
        return new QueryResults<>(results);
    }

    @GetMapping(value = "/{id}/leaderboard") // for the profile stats section
    @RateLimit(requests = 30, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many leaderboard user requests.")
    public LeaderboardUser getLeaderboardUserById(@PathVariable UUID id) {
        return userService.getLeaderboardUserById(id);
    }

    @GetMapping(value = "/{id}/ranked-stats") // for ranked-only statistics
    @RateLimit(requests = 30, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many ranked stats requests.")
    public LeaderboardUser getRankedStatsForUser(@PathVariable UUID id) {
        return userService.getRankedStatsForUser(id);
    }



    @PostMapping("/report-bug")
    @RateLimit(requests = 5, timeWindow = 5, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many bug reports. Please wait before submitting another.")
    public ResponseEntity<String> reportBug(@RequestBody BugReportRequest request) {
        try {
            // Create email message
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("watguessr@gmail.com");
            message.setSubject(request.getSubject());
            message.setText(request.getContent());

            mailSender.send(message);

            return ResponseEntity.ok("Bug report submitted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send bug report: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}/settings")
    @RateLimit(requests = 50, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many settings requests.")
    public ResponseEntity<UserSettingsDto> getUserSettings(@PathVariable UUID id, Authentication authentication) {
        var user = userService.findById(id);
        var requesterUsername = authentication != null ? authentication.getName() : null;

        if (requesterUsername == null || !user.getUsername().equals(requesterUsername)) {
            return ResponseEntity.status(403).build();
        }

        var dto = userMapper.toUserSettingsDto(user);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/change-password")
    @RateLimit(requests = 5, timeWindow = 5, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many password change attempts.")
    public void changePassword(@RequestParam String emailAddress, @RequestParam String newPassword) {
        userService.changePassword(emailAddress, newPassword);
    }

    @PutMapping("/change-username")
    @RateLimit(requests = 3, timeWindow = 10, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many username change attempts.")
    public ResponseEntity<String> changeUsername(@RequestParam String emailAddress, @RequestParam String newUsername) {
        userService.changeUsername(emailAddress, newUsername);
        return ResponseEntity.ok("Username changed successfully");
    }

    @DeleteMapping("/delete-user")
    @RateLimit(requests = 2, timeWindow = 10, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many account deletion attempts.")
    public ResponseEntity<String> deleteUser(@RequestParam String emailAddress) {
        userService.deleteUser(emailAddress);
        return ResponseEntity.ok("User deleted successfully");
    }
    
}
