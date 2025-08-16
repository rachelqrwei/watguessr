package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.dto.UserLoginDto;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gooners.watguessr.dto.LeaderboardUser;
import com.gooners.watguessr.dto.QueryResults;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.UserService;

import com.gooners.watguessr.service.GameService;
import com.gooners.watguessr.dto.MatchHistoryItem;

@RestController
@RequestMapping("api/user")
public class UserController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    private final EmailVerificationService emailVerificationService;
    private final GameService gameService;
    public UserController(UserService userService, UserMapper userMapper, EmailVerificationService emailVerificationService, GameService gameService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.emailVerificationService = emailVerificationService;
        this.gameService = gameService;
    }

    @PostMapping(value = "/register")
    public void register(@RequestBody @Valid User user) {
        this.userService.create(user);
    }
  
    public UserController(UserService userService) {
        this.userService = userService;
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
}
