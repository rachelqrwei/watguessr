package com.gooners.watguessr.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class GameDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private String gameMode;
    private UUID winnerId;
    private int rankedAverageElo;
    private Integer multiplayerTimer;
    private Integer multiplayerRoundCount;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }
    public UUID getWinnerId() { return winnerId; }
    public void setWinnerId(UUID winnerId) { this.winnerId = winnerId; }
    public int getRankedAverageElo() { return rankedAverageElo; }
    public void setRankedAverageElo(int rankedAverageElo) { this.rankedAverageElo = rankedAverageElo; }
    public Integer getMultiplayerTimer() { return multiplayerTimer; }
    public void setMultiplayerTimer(Integer multiplayerTimer) { this.multiplayerTimer = multiplayerTimer; }
    public Integer getMultiplayerRoundCount() { return multiplayerRoundCount; }
    public void setMultiplayerRoundCount(Integer multiplayerRoundCount) { this.multiplayerRoundCount = multiplayerRoundCount; }
}