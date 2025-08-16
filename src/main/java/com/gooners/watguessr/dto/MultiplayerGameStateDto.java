package com.gooners.watguessr.dto;

import com.gooners.watguessr.service.MultiplayerGameStateService.PlayerStateDto;

import java.util.Map;

public class MultiplayerGameStateDto {
    private String gameId;
    private Map<String, PlayerStateDto> players; // key: player id, value: player info
    private Integer currentRound;
    private Integer maxRounds;
    private Integer timer;
    private String finalWinner;
    private Boolean shouldEnd;
    private String gameStatus; // "loading", "playing", "round-complete", "game-complete"
    private String currentSceneId;

    public MultiplayerGameStateDto() {}

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Map<String, PlayerStateDto> getPlayers() {
        return players;
    }

    public void setPlayers(Map<String, PlayerStateDto> players) {
        this.players = players;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public Integer getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(Integer maxRounds) {
        this.maxRounds = maxRounds;
    }

    public Integer getTimer() {
        return timer;
    }

    public void setTimer(Integer timer) {
        this.timer = timer;
    }

    public String getFinalWinner() {
        return finalWinner;
    }

    public void setFinalWinner(String finalWinner) {
        this.finalWinner = finalWinner;
    }

    public Boolean getShouldEnd() {
        return shouldEnd;
    }

    public void setShouldEnd(Boolean shouldEnd) {
        this.shouldEnd = shouldEnd;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getCurrentSceneId() {
        return currentSceneId;
    }

    public void setCurrentSceneId(String currentSceneId) {
        this.currentSceneId = currentSceneId;
    }
}