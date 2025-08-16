package com.gooners.watguessr.dto;

import java.util.UUID;

//for use in the frontend -- no sensitive information included
public class UserDto {
    private UUID id;
    private String username;
    private int elo;
    private int streak;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
   
}