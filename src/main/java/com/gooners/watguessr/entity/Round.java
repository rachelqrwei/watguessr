package com.gooners.watguessr.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
