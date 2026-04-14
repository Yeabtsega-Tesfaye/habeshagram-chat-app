package com.habeshagram.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String creator;
    private Set<String> members;
    private LocalDateTime createdAt;
    
    public Group() {
        this.members = new HashSet<>();
    }
    
    public Group(String name, String creator) {
        this();
        this.name = name;
        this.creator = creator;
        this.createdAt = LocalDateTime.now();
        this.members.add(creator);
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    
    public Set<String> getMembers() { return members; }
    public void setMembers(Set<String> members) { this.members = members; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public void addMember(String username) {
        members.add(username);
    }
    
    public void removeMember(String username) {
        members.remove(username);
    }
    
    public boolean hasMember(String username) {
        return members.contains(username);
    }
    
    @Override
    public String toString() {
        return name + " (" + members.size() + " members)";
    }
}