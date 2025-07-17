package com.gooners.watguessr.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private String username;
    private String emailAddress;
    private int elo;
    private int streak;
    private OffsetDateTime lastLoginAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }
    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }
    public OffsetDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(OffsetDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}