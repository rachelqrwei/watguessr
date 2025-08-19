package com.gooners.watguessr.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MatchHistoryItem {
    private UUID gameId;
    private String gameMode;
    private OffsetDateTime playedAt;
    private Integer roundsSurvived; // singleplayer only
    private Boolean won; // ranked/multiplayer only
    private Integer numPlayers; // multiplayer/ranked only
    private Boolean finished;

    public UUID getGameId() { return gameId; }
    public void setGameId(UUID gameId) { this.gameId = gameId; }

    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }

    public OffsetDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(OffsetDateTime playedAt) { this.playedAt = playedAt; }

    public Integer getRoundsSurvived() { return roundsSurvived; }
    public void setRoundsSurvived(Integer roundsSurvived) { this.roundsSurvived = roundsSurvived; }

    public Boolean getWon() { return won; }
    public void setWon(Boolean won) { this.won = won; }

    public Integer getNumPlayers() { return numPlayers; }
    public void setNumPlayers(Integer numPlayers) { this.numPlayers = numPlayers; }

    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }
} 