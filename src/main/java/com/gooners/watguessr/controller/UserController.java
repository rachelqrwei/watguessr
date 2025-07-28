package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.UserDto;
import com.gooners.watguessr.dto.UserSignupDto;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

}
