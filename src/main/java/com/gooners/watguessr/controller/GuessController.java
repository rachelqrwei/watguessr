package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.*;
import com.gooners.watguessr.service.GuessService;
import com.gooners.watguessr.service.RoundGuessService;
import com.gooners.watguessr.service.RoundService;
import com.gooners.watguessr.service.UserService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/guess")
public class GuessController {

    @Autowired
    private GuessService guessService;

    // returns status code
    @PostMapping(value = "/create-round-guess")
    public ResponseEntity<?> createRoundGuess(UUID roundId, UUID userId, UUID guessId, Integer points) {
        guessService.createRoundGuess(roundId, userId, guessId, points);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/get-all-round")
    private List<Round> getAllRound() {
        return guessService.getAllRound();
    }
}
