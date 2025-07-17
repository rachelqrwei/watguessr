package com.gooners.watguessr.dto;


import java.util.UUID;

public class GuessDto {
    private UUID id;
    private UUID userId;
    private Integer time;
    private Double guessX;
    private Double guessY;
    private UUID buildingId;
    private Integer floor;
    private UUID roundId;
    private Integer points;

    // getters & setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Integer getTime() { return time; }
    public void setTime(Integer time) { this.time = time; }

    public Double getGuessX() { return guessX; }
    public void setGuessX(Double guessX) { this.guessX = guessX; }

    public Double getGuessY() { return guessY; }
    public void setGuessY(Double guessY) { this.guessY = guessY; }

    public UUID getBuildingId() { return buildingId; }
    public void setBuildingId(UUID buildingId) { this.buildingId = buildingId; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public UUID getRoundId() { return roundId; }
    public void setRoundId(UUID roundId) { this.roundId = roundId; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
}
