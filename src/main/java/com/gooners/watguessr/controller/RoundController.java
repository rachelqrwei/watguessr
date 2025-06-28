package com.gooners.watguessr.controller;

import com.gooners.watguessr.service.RoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/round")
public class RoundController {

    @Autowired
    private RoundService roundService;

    @GetMapping(value = "/create")
    public UUID createRound(UUID gameId) {
        return roundService.create(gameId);
    }
}
