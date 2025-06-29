package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.Game;
import com.gooners.watguessr.repository.GameRepository;
import com.gooners.watguessr.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("api/game")
public class GameController {
    
    private final GameService gameService;
    
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping(value = "/create")
    public UUID createGame(Game sourceGame) {
        return switch (sourceGame.getGameMode()) {
            case "Singleplayer" -> gameService.createSingleplayerGame();
            case "Multiplayer" ->
                    gameService.createMultiplayerGame(sourceGame.getMultiplayerTimer(), sourceGame.getMultiplayerRoundCount());
            case "Ranked" -> gameService.createRankedGame(sourceGame.getRankedAverageElo());
            default -> null;
        };
    }

    @PostMapping(value = "/finish")
    public HashMap<String, Object> finishGame(UUID gameId) {
        return gameService.finishGame(gameId);
    }
    //if you want to get the results for the singleplayer game, its in "singlePlayerRoundsSurvived", roundsSurvived
    //if multiplayer or ranked, its ordered in points descending in

}
