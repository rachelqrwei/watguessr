package com.gooners.watguessr.entity

import jakarta.persistence.*;
import org.springframework.data.annotation.Id
import java.util.*

@Entity
class Scene {

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

    @Column(name = "building_id")
    @ForeignKey(value = "building_id") //check
    private UUID buildingId;

    @Column(name = "floor")
    private Double floor;

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

    public Double getFloor() {
        return floor;
    }

    public void setFloor(Double floor) {
        this.floor = floor;
    }

    public UUID getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(UUID buildingId) {
        this.buildingId = buildingId;
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