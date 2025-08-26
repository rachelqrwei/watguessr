package com.gooners.watguessr.controller;

import com.gooners.watguessr.config.RateLimit;
import com.gooners.watguessr.dto.GuessCreateDto;
import com.gooners.watguessr.dto.RoundResult;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.entity.User;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.service.GuessService;
import com.gooners.watguessr.service.RoundService;
import com.gooners.watguessr.service.UserService;
import com.gooners.watguessr.utils.CustomException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/guess")
public class GuessController {

    private final GuessService guessService;
    private final RoundService roundService;
    private final UserService userService;

    public GuessController(GuessService guessService, RoundService roundService, UserService userService) {
        this.guessService = guessService;
        this.roundService = roundService;
        this.userService = userService;
    }

    @PostMapping
    @RateLimit(requests = 30, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many guesses. Please slow down.")
    public ResponseEntity<RoundResult> submitGuess(
            @RequestBody @Valid GuessCreateDto createDto,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Extract authenticated user from JWT token
        String username = jwt.getSubject();
        User authenticatedUser = userService.findByUsername(username);
        
        // Security check: ensure authenticated user matches the userId in request
        if (!authenticatedUser.getId().equals(createDto.getUserId())) {
            throw new CustomException("You can only submit guesses for yourself");
        }
        
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

        // Create and evaluate the guess in one operation
        RoundResult result = guessService.createAndEvaluateGuess(toSave);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

}
