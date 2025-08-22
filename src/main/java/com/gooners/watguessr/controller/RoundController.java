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
import com.gooners.watguessr.utils.CustomException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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
    public UUID createRound(@RequestParam UUID gameId, @RequestParam UUID userId) {
        // Check to see if round exists where they haven't made a guess yet.
        // If so, they should not be allowed to create a new round.
        Game currentGame = gameService.findById(gameId);

        // Check if there's any round in this game where the user hasn't made a guess
        List<Round> rounds = currentGame.getRounds();
        for (Round round : rounds) {
            // Check if user has made a guess for this round
            Optional<Guess> guess = roundService.findGuessForUserAndRound(userId, round.getId());
            if (!guess.isPresent()) {
                // User hasn't made a guess for this round, return its ID instead of creating a
                // new one
                return round.getId();
            }
        }

        // If we get here, the user has made guesses for all existing rounds, so create
        // a new one
        Round round = roundService.create(gameId);
        return round.getId();
    }

    @GetMapping(value = "/{roundId}/scene")
    public SceneDto getRoundScene(@PathVariable UUID roundId, @RequestParam(required = false) UUID userId) {
        // Make sure all guesses are in before allowing return scene.
        Round round = roundService.findById(roundId);
        Game game = round.getGame();
        String gameMode = game.getGameMode();

        // For singleplayer, check if the requesting user has made a guess
        if ("singleplayer".equalsIgnoreCase(gameMode)) {
            if (userId == null) {
                throw new CustomException("User ID is required for singleplayer mode");
            }

            // Check if user has made a guess for this round
            Optional<Guess> guess = roundService.findGuessForUserAndRound(userId, roundId);
            if (!guess.isPresent()) {
                throw new CustomException("You must make a guess before viewing the correct answer");
            }
        }
        // For multiplayer and ranked, check if all players have submitted guesses
        else if ("multiplayer".equalsIgnoreCase(gameMode) || "ranked".equalsIgnoreCase(gameMode)) {
            // Get all players in the game
            List<UUID> playerIds = gameService.getPlayerIdsForGame(game.getId());

            // Check if all players have made guesses
            for (UUID playerId : playerIds) {
                Optional<Guess> guess = roundService.findGuessForUserAndRound(playerId, roundId);
                if (!guess.isPresent()) {
                    throw new CustomException("All players must make guesses before viewing the correct answer");
                }
            }
        }

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
            // use dtos to flatten user->userid, round->roundId
            List<GuessDto> guessDtos = guesses.stream().map(guessMapper::toDto).collect(Collectors.toList());
            dto.setGuesses(guessDtos);
            return dto;
        }).collect(Collectors.toList());
    }
}
