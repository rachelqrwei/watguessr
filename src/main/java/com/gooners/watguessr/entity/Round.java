package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Round {

    @Id
    @Column(name = "RoundId", nullable = false, unique = true)
    private UUID roundId;

    @ManyToOne
    @JoinColumn(name = "scene_id", foreignKey = @ForeignKey(name = "fk_round_scene"))
    private Scene scene;

    public UUID getRoundId() {
        return roundId;
    }

    public void setRoundId(UUID roundId) {
        this.roundId = roundId;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

}
