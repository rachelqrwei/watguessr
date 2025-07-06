package com.gooners.watguessr.controller;

import com.gooners.watguessr.service.RoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gooners.watguessr.entity.Round;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("api/round")
public class RoundController {

    private final RoundService roundService;
    
    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }

    @GetMapping(value = "/create")
    public UUID createRound(UUID gameId) {
        return roundService.create(gameId);
    }

    @GetMapping(value = "/get-rounds-by-game")
    public List<Round> getRoundsByGame(@RequestParam UUID gameId) {
        return roundService.GetRoundsByGame(gameId);
    }
}
