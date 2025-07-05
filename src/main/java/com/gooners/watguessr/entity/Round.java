package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "scene_id", foreignKey = @ForeignKey(name = "fk_round_scene"))
    private Scene scene;

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

}
