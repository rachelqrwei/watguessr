package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.Building;
import com.gooners.watguessr.entity.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/guess")
public class GuessController {

    @PostMapping
    public void guess(User user, Integer time, Double guessX, Double guessY, Building building, Integer floor) {
        //create guess -> persist
    }





}
