package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.RoundGuess;
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


    @PostMapping(value = "/create")
    public void guess(UUID userId, UUID roundId, Guess sourceGuess) {
        Guess guess = new Guess();
        guess.setGuessX(sourceGuess.getGuessX());
        guess.setGuessY(sourceGuess.getGuessY());
        if (sourceGuess.getBuilding() != null ) {
            guess.setBuilding(sourceGuess.getBuilding());
            guess.setFloor(sourceGuess.getFloor());
        }
        guess.setTime(sourceGuess.getTime());
        guessService.create(guess, userId, roundId);
    }

    @GetMapping(value = "/get-all-guess")
    public List<Guess> getAllGuess(@RequestParam UUID roundId) {
        return guessService.findGuessByRoundId(roundId);
    }


}
