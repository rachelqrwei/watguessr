package com.gooners.watguessr.controller;

import com.gooners.watguessr.config.RateLimit;
import com.gooners.watguessr.dto.*;
import com.gooners.watguessr.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping(value = "/create/singleplayer")
    @RateLimit(requests = 10, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many game creation requests.")
    public UUID createSingleplayerGame() {
        // TODO: should not be able to create if already in a game or another queue.
        return gameService.createSingleplayerGame();
    }

    @PostMapping(value = "/lobby/create")
    @RateLimit(requests = 5, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many lobby creation requests.")
    public LobbyDto createLobby(@RequestBody LobbyCreateDto lobbyCreateDto) {
        return gameService.createLobby(lobbyCreateDto);
    }

    @GetMapping(value = "/lobby/public")
    @RateLimit(requests = 60, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.IP_ADDRESS, message = "Too many lobby list requests.")
    public List<LobbyDto> getPublicLobbies() {
        return gameService.getPublicLobbies();
    }

    @GetMapping(value = "/lobby/{lobbyId}")
    @RateLimit(requests = 100, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many lobby fetch requests.")
    public LobbyDto getLobbyById(@PathVariable UUID lobbyId) {
        return gameService.getLobbyById(lobbyId);
    }

    @PostMapping(value = "/lobby/join")
    @RateLimit(requests = 20, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many lobby join attempts.")
    public LobbyDto joinLobby(@RequestBody JoinLobbyDto joinLobbyDto) {
        return gameService.getLobbyByCode(joinLobbyDto.getLobbyCode());
    }

    @GetMapping(value = "/state/singleplayer")
    @RateLimit(requests = 100, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many game state requests.")
    public SingleplayerGameState getSingleplayerGameState(@RequestParam UUID gameId, @RequestParam UUID userId) {
        return gameService.getSingleplayerGameState(gameId, userId);
    }

    @PostMapping(value = "/finish/singleplayer")
    @RateLimit(requests = 30, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many game finish requests.")
    public Integer finishSingleplayerGame(@RequestParam UUID gameId, @RequestParam UUID userId) {
        return gameService.resolveSingleplayerGame(gameId, userId);
    }

    @PostMapping(value = "/finish/multiplayer")
    @RateLimit(requests = 20, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many game finish requests.")
    public HashMap<UUID, Integer> finishMultiplayerGame(@RequestParam UUID gameId) {
        return gameService.resolveMultiplayerGame(gameId);
    }

    @PostMapping(value = "/finish/ranked")
    @RateLimit(requests = 20, timeWindow = 1, keyStrategy = RateLimit.KeyStrategy.USER_ID, message = "Too many game finish requests.")
    public RankedGameResultDto finishRankedGame(@RequestParam UUID gameId) {
        return gameService.resolveRankedGame(gameId);
    }

}
