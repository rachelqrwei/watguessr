package com.gooners.watguessr.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "image")
    private String image;

    @Column(name = "location_x")
    private Double locationX;

    @Column(name = "location_y")
    private Double locationY;

    @Column(name = "floor")
    private Integer floor;

    @ManyToOne
    @JoinColumn(name = "building", foreignKey = @ForeignKey(name = "fk_scene_building"))
    private Building building;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building buildingId) {
        this.building = building;
    }

    public Double getLocationY() {
        return locationY;
    }

    public void setLocationY(Double locationY) {
        this.locationY = locationY;
    }

    public Double getLocationX() {
        return locationX;
    }

    public void setLocationX(Double locationX) {
        this.locationX = locationX;
    }

}