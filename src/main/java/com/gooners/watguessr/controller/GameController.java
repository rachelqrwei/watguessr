package com.gooners.watguessr.controller;

import com.gooners.watguessr.entity.Game;
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

    @Autowired
    private GameService gameService;

    @GetMapping(value = "/create")
    public UUID createGame(Game sourceGame) {
        Game newGame = new Game();
        newGame.setGameMode(sourceGame.getGameMode());
        if (newGame.getGameMode().equals("Multiplayer")) {
            newGame.setMultiplayerTimer(sourceGame.getMultiplayerTimer());
            newGame.setMultiplayerRoundCount(sourceGame.getMultiplayerRoundCount());
        }
        if (newGame.getGameMode().equals("Ranked")) {
            newGame.setRankedAverageElo(sourceGame.getRankedAverageElo());
        }
        return gameService.create(newGame);
    }


    @PostMapping(value = "/finish")
    public HashMap<String, Object> finishGame(UUID gameId) {
        return gameService.finishGame(gameId);
    }
    //if you want to get the results for the singleplayer game, its in "singlePlayerRoundsSurvived", roundsSurvived
    //if multiplayer or ranked, its ordered in points descending in

}
