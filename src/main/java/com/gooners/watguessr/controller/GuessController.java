package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.GuessDto;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.RoundGuess;
import com.gooners.watguessr.mapper.GuessMapper;
import com.gooners.watguessr.service.GuessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/guess")
public class GuessController {

    @Autowired
    private GuessService guessService;

    @Autowired
    private GuessMapper guessMapper;

    @PostMapping(value = "/create")
    public void guess(@RequestBody GuessDto guessDto) {
        Guess guess = guessMapper.toEntity(guessDto);
        guessService.create(guess, guessDto.getUserId(), guessDto.getRoundId());
    }

    @GetMapping(value = "/get-all-guess")
    public List<GuessDto> getAllGuess(@RequestParam UUID roundId) {
        List<Guess> guesses = guessService.findGuessByRoundId(roundId);
        return guesses.stream()
                .map(guessMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @GetMapping
    public String test() {
        return "Guess Controller is working!";
    }
}
