package com.gooners.watguessr.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class LobbyDto {
    private UUID id;
    private String lobbyName;
    private String gameMode;
    private Boolean isPrivate;
    private String lobbyCode;
    private Integer maxPlayers;
    private Integer currentPlayers;
    private Integer multiplayerTimer;
    private Integer multiplayerRoundCount;
    private OffsetDateTime createdAt;
    private List<UserDto> players;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLobbyName() { return lobbyName; }
    public void setLobbyName(String lobbyName) { this.lobbyName = lobbyName; }

    public String getGameMode() { return gameMode; }
    public void setGameMode(String gameMode) { this.gameMode = gameMode; }

    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }

    public String getLobbyCode() { return lobbyCode; }
    public void setLobbyCode(String lobbyCode) { this.lobbyCode = lobbyCode; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public Integer getCurrentPlayers() { return currentPlayers; }
    public void setCurrentPlayers(Integer currentPlayers) { this.currentPlayers = currentPlayers; }

    public Integer getMultiplayerTimer() { return multiplayerTimer; }
    public void setMultiplayerTimer(Integer multiplayerTimer) { this.multiplayerTimer = multiplayerTimer; }

    public Integer getMultiplayerRoundCount() { return multiplayerRoundCount; }
    public void setMultiplayerRoundCount(Integer multiplayerRoundCount) { this.multiplayerRoundCount = multiplayerRoundCount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public List<UserDto> getPlayers() { return players; }
    public void setPlayers(List<UserDto> players) { this.players = players; }
}
