package com.gooners.watguessr.controller;

import com.gooners.watguessr.dto.SingleplayerGameState;
import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @GetMapping(value = "/state/singleplayer")
    public SingleplayerGameState getSingleplayerGameState(@RequestParam UUID gameId, @RequestParam UUID userId) {
        return gameService.getSingleplayerGameState(gameId, userId);
    }

    @PostMapping(value = "/finish/singleplayer")
    public Integer finishSingleplayerGame(@RequestBody UUID gameId) {
        return gameService.resolveSingleplayerGame(gameId);
    }

    @PostMapping(value = "/finish/multiplayer")
    public HashMap<UUID, Integer> finishMultiplayerGame(@RequestBody UUID gameId) { return gameService.resolveMultiplayerGame(gameId);}

    @PostMapping(value = "/finish/ranked")
    public HashMap<UUID, Integer> finishRankedGame(@RequestBody UUID gameId) { return gameService.resolveRankedGame(gameId);}

}
