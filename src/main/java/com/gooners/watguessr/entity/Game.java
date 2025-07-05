package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "game_mode", nullable = false)
    private String gameMode;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "winner_user_id", foreignKey = @ForeignKey(name = "fk_game_winner_user") )
    private User winner;

    @Column(name = "ranked_average_elo")
    private Integer rankedAverageElo;

    @Column(name = "multiplayer_timer")
    private Integer multiplayerTimer;

    @Column(name = "multiplayer_round_count")
    private Integer multiplayerRoundCount;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public Integer getRankedAverageElo() {
        return rankedAverageElo;
    }

    public void setRankedAverageElo(Integer rankedAverageElo) {
        this.rankedAverageElo = rankedAverageElo;
    }

    public Integer getMultiplayerTimer() {
        return multiplayerTimer;
    }

    public void setMultiplayerTimer(Integer multiplayerTimer) {
        this.multiplayerTimer = multiplayerTimer;
    }

    public Integer getMultiplayerRoundCount() {
        return multiplayerRoundCount;
    }

    public void setMultiplayerRoundCount(Integer multiplayerRoundCount) {
        this.multiplayerRoundCount = multiplayerRoundCount;
    }

}
