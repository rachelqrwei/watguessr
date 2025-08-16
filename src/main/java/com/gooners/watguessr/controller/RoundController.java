package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.RoundDto;
import com.gooners.watguessr.dto.RoundGuessesDto;
import com.gooners.watguessr.dto.GuessDto;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.mapper.RoundMapper;
import com.gooners.watguessr.service.RoundService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/round")
public class RoundController {

    private final RoundService roundService;
    private final RoundMapper roundMapper;
    private final com.gooners.watguessr.service.GuessService guessService;
    private final com.gooners.watguessr.mapper.GuessMapper guessMapper;

    public RoundController(RoundService roundService, RoundMapper roundMapper,
                           com.gooners.watguessr.service.GuessService guessService,
                           com.gooners.watguessr.mapper.GuessMapper guessMapper) {
        this.roundService = roundService;
        this.roundMapper = roundMapper;
        this.guessService = guessService;
        this.guessMapper = guessMapper;
    }

    @GetMapping(value = "/create")
    public Round createRound(@RequestParam UUID gameId) {
        return roundService.create(gameId);
    }

    @GetMapping(value = "/by-game")
    public List<RoundDto> getRoundsByGame(@RequestParam UUID gameId) {
        // Rely on JPA relationship: Game -> Round
        // Avoids repository-level "findByGameId"; just use service to find the Game and then get its rounds
        var gameRounds = roundService.getRoundsForGame(gameId);
        return gameRounds.stream().map(roundMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping(value = "/by-game-with-guesses")
    public List<RoundGuessesDto> getRoundsByGameWithGuesses(@RequestParam UUID gameId) {
        var gameRounds = roundService.getRoundsForGame(gameId);
        return gameRounds.stream().map(r -> {
            RoundGuessesDto dto = new RoundGuessesDto();
            dto.setRoundId(r.getId());
            List<Guess> guesses = guessService.findAllGuessByRoundId(r.getId());
            List<GuessDto> guessDtos = guesses.stream().map(guessMapper::toDto).collect(Collectors.toList());
            dto.setGuesses(guessDtos);
            return dto;
        }).collect(Collectors.toList());
    }

}
