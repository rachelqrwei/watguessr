package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import com.gooners.watguessr.dto.LeaderboardUser;
import com.gooners.watguessr.dto.MatchHistoryItem;
import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.dto.UserDto;
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
    public void register(@RequestBody @Valid User user) {
        this.userService.create(user);
    }

    @GetMapping(value = "/{id}")
    public UserDto getUser(@PathVariable UUID id) {
        return this.userMapper.toDto(this.userService.findById(id));
    }

    @GetMapping(value = "/all")
    public List<UserDto> getSorted(String keyword, String sortBy, int page, int pageSize) {
        return this.userService.findSorted(keyword, sortBy, page, pageSize).stream().map(userMapper::toDto).toList();
    }

    @GetMapping(value = "/leaderboard")
    public ResponseEntity<QueryResults<LeaderboardUser>>getLeaderboard(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {

        return ResponseEntity.ok(userService.getLeaderboard(searchTerm, sortBy, limit, offset));
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

    @GetMapping(value = "/{id}/match-history")
    public QueryResults<MatchHistoryItem> getUserMatchHistory(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        var results = gameService.getUserMatchHistory(id, limit, offset);
        return new QueryResults<>(results);
    }

    @GetMapping(value = "/{id}/leaderboard") //for the profile stats section
    public LeaderboardUser getLeaderboardUserById(@PathVariable UUID id) {
        return userService.getLeaderboardUserById(id);
    }

    @PostMapping("/report-bug")
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
    public void changePassword(@RequestParam String emailAddress, @RequestParam String newPassword) {
        userService.changePassword(emailAddress, newPassword);
    }

    @PutMapping("/change-username")
    public void changeUsername(@RequestParam String emailAddress, @RequestParam String newUsername) {
        userService.changeUsername(emailAddress, newUsername);
    }

    @DeleteMapping("/delete-user")
    public void deleteUser(@RequestParam String emailAddress) {
        userService.deleteUser(emailAddress);
    }
    
}
