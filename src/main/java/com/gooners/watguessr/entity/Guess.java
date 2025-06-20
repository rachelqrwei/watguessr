package com.gooners.watguessr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Guess {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "userId")
    private UUID userId;

    @Column(name = "time")
    private Integer time;

    @Column(name = "guess_x")
    private Double guessX;

    @Column(name = "guess_y")
    private Double guessY;

    @Column(name = "building_id")
    private Integer building_id;

    @Column(name = "floor")
    private Integer floor;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Double getGuessY() {
        return guessY;
    }

    public void setGuessY(Double guessY) {
        this.guessY = guessY;
    }

    public Double getGuessX() {
        return guessX;
    }

    public void setGuessX(Double guessX) {
        this.guessX = guessX;
    }

    public Integer getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(Integer building_id) {
        this.building_id = building_id;
    }


}
