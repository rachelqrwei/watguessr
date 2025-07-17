package com.gooners.watguessr.dto;

import java.util.UUID;

public class RoundCreateDto {
    private UUID sceneId;
    private UUID gameId;

    public UUID getSceneId() { return sceneId; }
    public void setSceneId(UUID sceneId) { this.sceneId = sceneId; }
    public UUID getGameId() { return gameId; }
    public void setGameId(UUID gameId) { this.gameId = gameId; }
}
