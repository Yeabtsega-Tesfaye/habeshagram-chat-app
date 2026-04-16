package com.habeshagram.server.persistence;

import com.habeshagram.common.model.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserDAO {
    
    public void saveUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, created_at, last_seen) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setTimestamp(3, Timestamp.valueOf(user.getCreatedAt()));
            pstmt.setTimestamp(4, Timestamp.valueOf(user.getLastSeen()));
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }
    
public User getUser(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";
    
    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            User user = new User();
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setLastSeen(rs.getTimestamp("last_seen").toLocalDateTime());
            
            // Load custom status
            String customStatus = rs.getString("custom_status");
            user.setCustomStatus(customStatus != null ? customStatus : "");
            
            return user;
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting user: " + e.getMessage());
    }
    
    return null;
}
    
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }
        
        return false;
    }
    
    public void updateLastSeen(String username) {
        String sql = "UPDATE users SET last_seen = ? WHERE username = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating last seen: " + e.getMessage());
        }
    }

    public void updateUser(User user) {
    String sql = "UPDATE users SET password_hash = ?, last_seen = ?, custom_status = ? WHERE username = ?";
    
    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, user.getPasswordHash());
        pstmt.setTimestamp(2, Timestamp.valueOf(user.getLastSeen()));
        pstmt.setString(3, user.getCustomStatus());
        pstmt.setString(4, user.getUsername());
        
        pstmt.executeUpdate();
        
    } catch (SQLException e) {
        System.err.println("Error updating user: " + e.getMessage());
    }
}

public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    String sql = "SELECT * FROM users ORDER BY username";
    
    try (Connection conn = DatabaseManager.getInstance().getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            User user = new User();
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setLastSeen(rs.getTimestamp("last_seen").toLocalDateTime());
            
            String customStatus = rs.getString("custom_status");
            user.setCustomStatus(customStatus != null ? customStatus : "");
            
            users.add(user);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting all users: " + e.getMessage());
    }
    
    return users;
}

}