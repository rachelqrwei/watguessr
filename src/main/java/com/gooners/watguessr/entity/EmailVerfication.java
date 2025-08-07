package com.gooners.watguessr.entity;

import jakarta.persistence.*;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "email_verification")
public class EmailVerfication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @OneToOne
    @Column(name = "user_id")
    private User user;

    @Column(name = "code")
    private String code;

    @Column(name = "expiry")
    private LocalDateTime expiry;

    @Column(name = "verified")
    private boolean verified = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
