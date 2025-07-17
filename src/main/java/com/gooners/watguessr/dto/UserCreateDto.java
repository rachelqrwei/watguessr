package com.gooners.watguessr.dto;

public class UserCreateDto {
    private String username;
    private String emailAddress;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}