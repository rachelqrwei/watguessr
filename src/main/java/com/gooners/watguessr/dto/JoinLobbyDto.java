package com.gooners.watguessr.dto;

import java.util.UUID;

public class JoinLobbyDto {
    private String lobbyCode;
    private UUID userId;

    public String getLobbyCode() { return lobbyCode; }
    public void setLobbyCode(String lobbyCode) { this.lobbyCode = lobbyCode; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
