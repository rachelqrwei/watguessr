package com.gooners.watguessr.controller;

import com.gooners.watguessr.config.RateLimit;
import com.gooners.watguessr.dto.GuessCreateDto;
import com.gooners.watguessr.dto.GuessDto;
import com.gooners.watguessr.dto.RoundResult;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.mapper.GuessMapper;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.service.GuessService;
import com.gooners.watguessr.service.RoundService;
import com.gooners.watguessr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@RestController
@RequestMapping("api/guess")
public class GuessController {

    private final GuessService guessService;
    private final RoundService roundService;
    private final UserService userService;
    private final GuessMapper guessMapper;

    public GuessController(GuessService guessService, RoundService roundService, UserService userService,
            GuessMapper guessMapper) {
        this.guessService = guessService;
        this.roundService = roundService;
        this.userService = userService;
        this.guessMapper = guessMapper;
    }

    @PostMapping
    @RateLimit(requests = 30, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many guesses. Please slow down.")
    public ResponseEntity<GuessDto> createGuess(
            @RequestBody @Valid GuessCreateDto createDto) {
        // Fetch managed entities from database
        Round round = roundService.findById(createDto.getRoundId());
        User user = userService.findById(createDto.getUserId());

        // Create guess entity manually to ensure proper entity relationships
        Guess toSave = new Guess();
        toSave.setTime(createDto.getTime());
        toSave.setGuessX(createDto.getGuessX());
        toSave.setGuessY(createDto.getGuessY());
        toSave.setBuilding(createDto.getBuilding());
        toSave.setFloor(createDto.getFloor());
        toSave.setRound(round); // Set managed Round entity
        toSave.setUser(user); // Set managed User entity

        Guess saved = guessService.create(toSave);

        GuessDto result = guessMapper.toDto(saved);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @MessageMapping("/guess") // client sends to /app/guess
    @SendTo("/topic/guesses")
    public Guess processGuessInMultiplayerGame(Guess guess) {
        // Check for existing guess to prevent duplicates in multiplayer
        if (guess.getUser() != null && guess.getUser().getId() != null && 
            guess.getRound() != null && guess.getRound().getId() != null) {
            
            Optional<Guess> existingGuess = guessService.findByRoundIdAndUserId(
                guess.getRound().getId(), 
                guess.getUser().getId()
            );
            
            if (existingGuess.isPresent()) {
                // Return existing guess instead of processing duplicate
                return existingGuess.get();
            }
        }
        
        return guess;
    }

    @PostMapping("/evaluate-guess")
    @RateLimit(requests = 60, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many evaluation requests.")
    public ResponseEntity<RoundResult> evaluateGuess(
            @RequestParam UUID roundId,
            @RequestBody Guess guess) {
        Round round = roundService.findById(roundId);
        RoundResult result = guessService.evaluateGuess(round, guess);

        return ResponseEntity.ok(result);
    }

}
