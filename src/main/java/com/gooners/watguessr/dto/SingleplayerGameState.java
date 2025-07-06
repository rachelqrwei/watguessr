package com.gooners.watguessr.dto;

import java.util.UUID;

public class SingleplayerGameState {
    private UUID gameId;
    private Integer currentScore;
    private Integer roundsCompleted;
    private boolean shouldEnd;
    private boolean isGameEnded;

    public SingleplayerGameState() {
    }

    public SingleplayerGameState(UUID gameId, Integer currentScore, Integer roundsCompleted, boolean shouldEnd, boolean isGameEnded) {
        this.gameId = gameId;
        this.currentScore = currentScore;
        this.roundsCompleted = roundsCompleted;
        this.shouldEnd = shouldEnd;
        this.isGameEnded = isGameEnded;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public Integer getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(Integer currentScore) {
        this.currentScore = currentScore;
    }

    public Integer getRoundsCompleted() {
        return roundsCompleted;
    }

    public void setRoundsCompleted(Integer roundsCompleted) {
        this.roundsCompleted = roundsCompleted;
    }

    public boolean isShouldEnd() {
        return shouldEnd;
    }

    public void setShouldEnd(boolean shouldEnd) {
        this.shouldEnd = shouldEnd;
    }

    public boolean isGameEnded() {
        return isGameEnded;
    }

    public void setGameEnded(boolean gameEnded) {
        isGameEnded = gameEnded;
    }
}