package com.gooners.watguessr.dto;

public class RoundResult {
    private int points;
    private double distance;

    public RoundResult(int points, double distance) {
        this.points = points;
        this.distance = distance;
    }

    public int getPoints() {
        return points;
    }

    public double getDistance() {
        return distance;
    }
}
