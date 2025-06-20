package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)// check
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "created_at")
    private OffsetDateTime created_at;

    @Column(name = "username")
    private String username;

    @Column(name = "email_address")
    private String email_address;

    @Column(name = "password")
    private String password;

    @Column(name = "elo")
    private String elo;

    @Column(name = "streak")
    private Integer streak;

    @Column(name = "last_login_at")
    private OffsetDateTime last_login_at;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OffsetDateTime getLast_login_at() {
        return last_login_at;
    }

    public void setLast_login_at(OffsetDateTime last_login_at) {
        this.last_login_at = last_login_at;
    }

    public OffsetDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(OffsetDateTime created_at) {
        this.created_at = created_at;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getElo() {
        return elo;
    }

    public void setElo(String elo) {
        this.elo = elo;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

}
