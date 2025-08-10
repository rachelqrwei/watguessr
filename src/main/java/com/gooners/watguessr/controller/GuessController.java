package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.GuessCreateDto;
import com.gooners.watguessr.dto.GuessDto;
import com.gooners.watguessr.dto.RoundResult;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.mapper.GuessMapper;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.service.GameService;
import com.gooners.watguessr.service.GuessService;
import com.gooners.watguessr.service.RoundService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/guess")
public class GuessController {

    private final GuessService guessService;
    private final RoundService roundService;
    private final GuessMapper guessMapper;

    public GuessController(GuessService guessService, RoundService roundService, GuessMapper guessMapper) {
        this.guessService = guessService;
        this.roundService = roundService;
        this.guessMapper = guessMapper;
    }
//    @PostMapping
//    public void guess(@RequestBody GuessDto guessDto) {
//        guessService.create(guessDto);
//    }

    @PostMapping
    public ResponseEntity<GuessDto> createGuess(
            @RequestBody @Valid GuessCreateDto createDto
    ) {
        // 1. DTO → Entity
        Guess toSave = guessMapper.toEntity(createDto);

        // 2. persist
        Guess saved  = guessService.create(toSave);

        // 3. Entity → full DTO (with generated id + points)
        GuessDto result = guessMapper.toDto(saved);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }

    @PostMapping("/evaluate-guess")
    public ResponseEntity<RoundResult> evaluateGuess(
            @RequestParam UUID roundId,
            @RequestBody Guess guess
    ) {
        Round round = roundService.findById(roundId);
        RoundResult result = guessService.evaluateGuess(round, guess);

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/get-all-guess")
    public List<Guess> getAllGuess(@RequestParam UUID roundId) {
        return guessService.findAllGuessByRoundId(roundId);
    }

}
