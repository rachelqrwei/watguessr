package com.gooners.watguessr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/scene")
public class SceneController {

    @GetMapping(value = "/random")
    public void random() {

    }
}
