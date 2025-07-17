package com.gooners.watguessr.dto;

import java.util.UUID;

public class GameCreateDto {
    private String gameMode;
    private UUID winnerId;
    private Integer multiplayerTimer;
    private Integer multiplayerRoundCount;

    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }
    public UUID getWinnerId() { return winnerId; }
    public void setWinnerId(UUID winnerId) { this.winnerId = winnerId; }
    public Integer getMultiplayerTimer() { return multiplayerTimer; }
    public void setMultiplayerTimer(Integer multiplayerTimer) { this.multiplayerTimer = multiplayerTimer; }
    public Integer getMultiplayerRoundCount() { return multiplayerRoundCount; }
    public void setMultiplayerRoundCount(Integer multiplayerRoundCount) { this.multiplayerRoundCount = multiplayerRoundCount; }
}