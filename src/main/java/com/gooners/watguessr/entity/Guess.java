package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Guess {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_guess_user"))
    private User user;

    @Column(name = "time")
    private Integer time;

    @Column(name = "guess_x")
    private Double guessX;

    @Column(name = "guess_y")
    private Double guessY;

    @ManyToOne
    @JoinColumn(name = "building_id", foreignKey = @ForeignKey(name = "fk_guess_building"))
    private Building building;

    @Column(name = "floor")
    private Integer floor;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Double getGuessX() {
        return guessX;
    }

    public void setGuessX(Double guessX) {
        this.guessX = guessX;
    }

    public Double getGuessY() {
        return guessY;
    }

    public void setGuessY(Double guessY) {
        this.guessY = guessY;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

}
