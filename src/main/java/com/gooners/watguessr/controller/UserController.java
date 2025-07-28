package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.UserService;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register")
    public void register(User user) {
        this.userService.create(user);
    }

    @PostMapping(value = "/login")
    public void login(User user) {
        user.setLastLoginAt(OffsetDateTime.now());
        this.userService.update(user);
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

}
