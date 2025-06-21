package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/game")
public class GameController {

//    @Autowired
//    private UserService userService;

    @PostMapping(value = "/start")
    public void startGame(User user) {
        //Output: SingleplayerRound with sceneId, etc.
    }


}
