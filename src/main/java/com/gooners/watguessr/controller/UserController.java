package com.gooners.watguessr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController {
    @PostMapping(value = "/craete")
    public void register() {
        //create user (persist)
    }

    @PostMapping(value = "/login")
    public void login() {

    }

    @GetMapping(value = "/profile")
    public void profile() {
    }


}
