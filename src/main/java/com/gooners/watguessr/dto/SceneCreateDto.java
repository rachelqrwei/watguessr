package com.gooners.watguessr.dto;

import java.util.UUID;

public class SceneCreateDto {
    private String image;
    private double locationX;
    private double locationY;
    private int floor;
    private UUID buildingId;

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public double getLocationX() { return locationX; }
    public void setLocationX(double locationX) { this.locationX = locationX; }
    public double getLocationY() { return locationY; }
    public void setLocationY(double locationY) { this.locationY = locationY; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public UUID getBuildingId() { return buildingId; }
    public void setBuildingId(UUID buildingId) { this.buildingId = buildingId; }
}