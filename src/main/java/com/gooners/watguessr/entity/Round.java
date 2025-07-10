package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "scene_id", foreignKey = @ForeignKey(name = "fk_round_scene"))
    private Scene scene;

    @ManyToOne
    @JoinColumn(name = "game_id", foreignKey = @ForeignKey(name = "fk_round_game"))
    private Game game;

    public UUID getId() {
        return id;
    }

    public void setId(UUID roundId) {
        this.id = roundId;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
