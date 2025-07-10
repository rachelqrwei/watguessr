package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.service.GuessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/guess")
public class GuessController {

    @Autowired
    private GuessService guessService;

    @PostMapping
    public void guess(@RequestBody Guess guess) {
        guessService.create(guess);
    }

    @GetMapping(value = "/get-all-guess")
    public List<Guess> getAllGuess(@RequestParam UUID roundId) {
        return guessService.findAllGuessByRoundId(roundId);
    }

}
