package com.gooners.watguessr.dto;

import com.gooners.watguessr.entity.Scene;
import java.util.UUID;

public class RoundDto {
    private UUID id;
    private Scene scene;
    
    // Constructors
    // public RoundDTO() {}
    
    // public RoundDTO(UUID id, Scene scene) {
    //     this.id = id;
    //     this.scene = scene;
    // }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Scene getScene() {
        return scene;
    }
    
    public void setScene(Scene scene) {
        this.scene = scene;
    }
}