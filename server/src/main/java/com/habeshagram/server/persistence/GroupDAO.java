package com.habeshagram.server.persistence;

import com.habeshagram.common.model.Group;
import java.sql.*;
import java.util.*;

public class GroupDAO {
    
    public void saveGroup(Group group) {
        String sql = "INSERT INTO groups (name, creator, created_at) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, group.getName());
            pstmt.setString(2, group.getCreator());
            pstmt.setTimestamp(3, Timestamp.valueOf(group.getCreatedAt()));
            
            pstmt.executeUpdate();
            
            // Add members
            for (String member : group.getMembers()) {
                addGroupMember(group.getName(), member);
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving group: " + e.getMessage());
        }
    }
    
    public Group getGroup(String groupName) {
        String sql = "SELECT * FROM groups WHERE name = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Group group = new Group();
                group.setName(rs.getString("name"));
                group.setCreator(rs.getString("creator"));
                group.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                
                // Load members
                Set<String> members = getGroupMembers(groupName);
                group.setMembers(members);
                
                return group;
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting group: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT name FROM groups ORDER BY name";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Group group = getGroup(rs.getString("name"));
                if (group != null) {
                    groups.add(group);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all groups: " + e.getMessage());
        }
        
        return groups;
    }
    
    public void updateGroup(Group group) {
        String deleteSql = "DELETE FROM group_members WHERE group_name = ?";
        String insertSql = "INSERT INTO group_members (group_name, username) VALUES (?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            
            // Delete existing members
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setString(1, group.getName());
                pstmt.executeUpdate();
            }
            
            // Insert current members
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                for (String member : group.getMembers()) {
                    pstmt.setString(1, group.getName());
                    pstmt.setString(2, member);
                    pstmt.executeUpdate();
                }
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            System.err.println("Error updating group: " + e.getMessage());
        }
    }
    
    private void addGroupMember(String groupName, String username) {
        String sql = "INSERT INTO group_members (group_name, username) VALUES (?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, groupName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error adding group member: " + e.getMessage());
        }
    }
    
    private Set<String> getGroupMembers(String groupName) {
        Set<String> members = new HashSet<>();
        String sql = "SELECT username FROM group_members WHERE group_name = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                members.add(rs.getString("username"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting group members: " + e.getMessage());
        }
        
        return members;
    }

public void deleteGroup(String groupName) {
    try (Connection conn = DatabaseManager.getInstance().getConnection()) {
        conn.setAutoCommit(false);
        
        // Delete group members first
        String deleteMembersSQL = "DELETE FROM group_members WHERE group_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteMembersSQL)) {
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
        }
        
        // Delete group
        String deleteGroupSQL = "DELETE FROM groups WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteGroupSQL)) {
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
        }
        
        conn.commit();
        System.out.println("Group deleted: " + groupName);
        
    } catch (SQLException e) {
        System.err.println("Error deleting group: " + e.getMessage());
    }
}

}