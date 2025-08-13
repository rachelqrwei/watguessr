// com/gooners/watguessr/dto/GuessCreateDto.java
package com.gooners.watguessr.dto;

import java.util.UUID;

public class GuessCreateDto {
    private UUID userId;
    private Integer time;
    private Double guessX;
    private Double guessY;
    private String building;
    private String floor;
    private UUID roundId;
// no points

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Integer getTime() { return time; }
    public void setTime(Integer time) { this.time = time; }

    public Double getGuessX() { return guessX; }
    public void setGuessX(Double guessX) { this.guessX = guessX; }

    public Double getGuessY() { return guessY; }
    public void setGuessY(Double guessY) { this.guessY = guessY; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public UUID getRoundId() { return roundId; }
    public void setRoundId(UUID roundId) { this.roundId = roundId; }
}
