package com.gooners.watguessr.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "game_mode", nullable = false)
    private String gameMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_user_id", foreignKey = @ForeignKey(name = "fk_game_winner_user"))
    @JsonIgnoreProperties({"password"})
    private User winner;

    @Column(name = "ranked_average_elo")
    private Integer rankedAverageElo;

    @Column(name = "multiplayer_timer")
    private Integer multiplayerTimer;

    @Column(name = "multiplayer_round_count")
    private Integer multiplayerRoundCount;

    @Column(name = "lobby_code", length = 8)
    private String lobbyCode;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @Column(name = "lobby_name", length = 100)
    private String lobbyName;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers = 8;

    @OneToMany(mappedBy = "game")
    @JsonIgnoreProperties({"game"})
    private List<Round> rounds;

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

    public List<Round> getRounds() {
        return rounds;
    }

    public String getLobbyCode() {
        return lobbyCode;
    }

    public void setLobbyCode(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public void setLobbyName(String lobbyName) {
        this.lobbyName = lobbyName;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
