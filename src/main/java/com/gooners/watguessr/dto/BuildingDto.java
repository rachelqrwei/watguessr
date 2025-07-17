package com.gooners.watguessr.dto;

import java.util.UUID;

public class BuildingDto {
    private UUID id;
    private String name;
    private int floors;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getFloors() { return floors; }
    public void setFloors(int floors) { this.floors = floors; }
}