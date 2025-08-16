package com.gooners.watguessr.controller;

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

@RestController
@RequestMapping("api/user")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
