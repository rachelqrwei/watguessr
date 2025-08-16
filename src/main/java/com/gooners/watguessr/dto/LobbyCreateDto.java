package com.gooners.watguessr.dto;

import java.util.UUID;

public class LobbyCreateDto {
    private String gameMode;
    private String lobbyName;
    private Boolean isPrivate;
    private Integer maxPlayers;
    private Integer multiplayerTimer;
    private Integer multiplayerRoundCount;
    private UUID creatorId;

    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }

    public String getLobbyName() { return lobbyName; }
    public void setLobbyName(String lobbyName) { this.lobbyName = lobbyName; }

    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public Integer getMultiplayerTimer() { return multiplayerTimer; }
    public void setMultiplayerTimer(Integer multiplayerTimer) { this.multiplayerTimer = multiplayerTimer; }

    public Integer getMultiplayerRoundCount() { return multiplayerRoundCount; }
    public void setMultiplayerRoundCount(Integer multiplayerRoundCount) { this.multiplayerRoundCount = multiplayerRoundCount; }

    public UUID getCreatorId() { return creatorId; }
    public void setCreatorId(UUID creatorId) { this.creatorId = creatorId; }
}
