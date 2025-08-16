package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.JoinLobbyDto;
import com.gooners.watguessr.dto.LobbyCreateDto;
import com.gooners.watguessr.dto.LobbyDto;
import com.gooners.watguessr.dto.SingleplayerGameState;
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
    public UUID createSingleplayerGame() {
        return gameService.createSingleplayerGame();
    }

    @GetMapping(value = "/create/multiplayer")
    public UUID createMultiplayerGame(@RequestParam Integer roundCount, @RequestParam Integer timer) {
        return gameService.createMultiplayerGame(roundCount, timer);
    }

    @GetMapping(value = "/create/ranked")
    public UUID createRankedGame(@RequestParam Integer averageElo) {
        return gameService.createRankedGame(averageElo);
    }

    @PostMapping(value = "/lobby/create")
    public LobbyDto createLobby(@RequestBody LobbyCreateDto lobbyCreateDto) {
        return gameService.createLobby(lobbyCreateDto);
    }

    @GetMapping(value = "/lobby/public")
    public List<LobbyDto> getPublicLobbies() {
        return gameService.getPublicLobbies();
    }

    @GetMapping(value = "/lobby/{lobbyId}")
    public LobbyDto getLobbyById(@PathVariable UUID lobbyId) {
        return gameService.getLobbyById(lobbyId);
    }

    @PostMapping(value = "/lobby/join")
    public LobbyDto joinLobby(@RequestBody JoinLobbyDto joinLobbyDto) {
        return gameService.getLobbyByCode(joinLobbyDto.getLobbyCode());
    }

    @GetMapping(value = "/state/singleplayer")
    public SingleplayerGameState getSingleplayerGameState(@RequestParam UUID gameId, @RequestParam UUID userId) {
        return gameService.getSingleplayerGameState(gameId, userId);
    }

    @PostMapping(value = "/finish/singleplayer")
    public Integer finishSingleplayerGame(@RequestParam UUID gameId, @RequestParam UUID userId) {
        return gameService.resolveSingleplayerGame(gameId, userId);
    }

    @PostMapping(value = "/finish/multiplayer")
    public HashMap<UUID, Integer> finishMultiplayerGame(@RequestParam UUID gameId) { 
        return gameService.resolveMultiplayerGame(gameId);
    }

    @PostMapping(value = "/finish/ranked")
    public HashMap<UUID, Integer> finishRankedGame(@RequestParam UUID gameId) { 
        return gameService.resolveRankedGame(gameId);
    }

}
