package com.gooners.watguessr.dto;

import java.util.UUID;

public class SceneDto {
    private UUID id;
    private String image;
    private double locationX;
    private double locationY;
    private String floor;
    private UUID buildingId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public double getLocationX() { return locationX; }
    public void setLocationX(double locationX) { this.locationX = locationX; }
    public double getLocationY() { return locationY; }
    public void setLocationY(double locationY) { this.locationY = locationY; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public UUID getBuildingId() { return buildingId; }
    public void setBuildingId(UUID buildingId) { this.buildingId = buildingId; }
}