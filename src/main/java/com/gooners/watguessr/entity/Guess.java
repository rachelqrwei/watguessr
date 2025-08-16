package com.gooners.watguessr.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "guess")
public class Guess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_guess_user"))
    @JsonIgnoreProperties({"password"})
    private User user;

    @Column(name = "time", nullable = false)
    private Integer time;

    @Column(name = "guess_x", nullable = false)
    private Double guessX;

    @Column(name = "guess_y", nullable = false)
    private Double guessY;

    @Column(name = "building", nullable = false)
    private String building;

    @Column(name = "floor")
    private String floor;

    @ManyToOne
    @JoinColumn(name = "round_id", foreignKey = @ForeignKey(name = "fk_guess_round"))
    @JsonIgnoreProperties({"game"})
    private Round round;

    @Column(name = "points")
    private Integer points;

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

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }


    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
