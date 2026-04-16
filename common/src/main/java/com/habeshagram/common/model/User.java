package com.habeshagram.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String passwordHash;
    private LocalDateTime createdAt;
    private UserStatus status;
    private LocalDateTime lastSeen;
    private String customStatus = "";
private LocalDateTime statusExpiry;


    public User() {}
    
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

public String getCustomStatus() { return customStatus; }
public void setCustomStatus(String customStatus) { this.customStatus = customStatus; }
    
    
    @Override
    public String toString() {
        return username;
    }

    public String getStatusText() {
    if (status == UserStatus.ONLINE) {
        return "Online";
    } else {
        if (lastSeen != null) {
            return "Last seen: " + lastSeen.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return "Offline";
    }
}
}