package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class GameRound {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id", foreignKey = @ForeignKey(name = "fk_game_round_game"), nullable = false)
    private Game gameId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "round_id", foreignKey = @ForeignKey(name = "fk_game_round_round"))
    private Round round;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Game getGameId() {
        return gameId;
    }

    public void setGameId(Game gameId) {
        this.gameId = gameId;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }
}
