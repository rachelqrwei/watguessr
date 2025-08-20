package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.RoundGuessesDto;
import com.gooners.watguessr.dto.GuessDto;
import com.gooners.watguessr.dto.SceneDto;
import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.entity.Guess;
import com.gooners.watguessr.entity.Round;
import com.gooners.watguessr.mapper.SceneMapper;
import com.gooners.watguessr.service.GameService;
import com.gooners.watguessr.service.RoundService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/round")
public class RoundController {

    private final RoundService roundService;
    private final com.gooners.watguessr.mapper.GuessMapper guessMapper;
    private final SceneMapper sceneMapper;
    private final GameService gameService;

    public RoundController(RoundService roundService,
                           com.gooners.watguessr.mapper.GuessMapper guessMapper, 
                           SceneMapper sceneMapper,
                           GameService gameService) {
        this.roundService = roundService;
        this.guessMapper = guessMapper;
        this.sceneMapper = sceneMapper;
        this.gameService = gameService;
    }

    @GetMapping(value = "/create")
    public UUID createRound(@RequestParam UUID gameId) {
        Round round = roundService.create(gameId);
        return round.getId();
    }

    @GetMapping(value = "/{roundId}/scene")
    public SceneDto getRoundScene(@PathVariable UUID roundId) {
        Round round = roundService.findById(roundId);
        return sceneMapper.toDto(round.getScene());
    }

    @GetMapping(value = "/by-game-with-guesses")
    public List<RoundGuessesDto> getRoundsByGameWithGuesses(@RequestParam UUID gameId) {
        Game game = gameService.findById(gameId);
        var gameRounds = game.getRounds();
        return gameRounds.stream().map(r -> {
            RoundGuessesDto dto = new RoundGuessesDto();
            dto.setRoundId(r.getId());
            List<Guess> guesses = r.getGuesses();
            //use dtos to flatten user->userid, round->roundId
            List<GuessDto> guessDtos = guesses.stream().map(guessMapper::toDto).collect(Collectors.toList());
            dto.setGuesses(guessDtos);
            return dto;
        }).collect(Collectors.toList());
    }
}
