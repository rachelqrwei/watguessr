package com.gooners.watguessr.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

// Full user settings without sensitive or excluded fields
public class UserSettingsDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private String username;
    private String emailAddress;
    private Integer elo;
    private Integer streak;
    private OffsetDateTime usernameChangedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Integer getElo() {
        return elo;
    }

    public void setElo(Integer elo) {
        this.elo = elo;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public OffsetDateTime getUsernameChangedAt() { return usernameChangedAt; }

    public void setUsernameChangedAt(OffsetDateTime usernameChangedAt) { this.usernameChangedAt = usernameChangedAt; }
}


