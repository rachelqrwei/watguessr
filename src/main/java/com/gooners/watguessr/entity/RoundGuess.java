package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class RoundGuess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "round_id", nullable = false, foreignKey = @ForeignKey(name = "fk_round_guess_round"))
    private Round round;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_round_guess_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "guess_id", nullable = false, foreignKey = @ForeignKey(name = "fk_round_guess_guess"))
    private Guess guess;

    @Column(name = "points", nullable = false)
    private Integer points;

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Guess getGuess() {
        return guess;
    }

    public void setGuess(Guess guess) {
        this.guess = guess;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
