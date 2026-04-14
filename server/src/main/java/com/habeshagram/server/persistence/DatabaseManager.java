package com.habeshagram.server.persistence;

import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:habeshagram.db";
    
    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    public void initializeDatabase() {
        String schema = loadSchema();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Execute schema.sql
            for (String sql : schema.split(";")) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql.trim());
                }
            }
            
            System.out.println("Database schema initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String loadSchema() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db/schema.sql")) {
            if (is == null) {
                // Use default schema if file not found
                return getDefaultSchema();
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining("\n"));
            
        } catch (Exception e) {
            System.err.println("Error loading schema file: " + e.getMessage());
            return getDefaultSchema();
        }
    }
    
    private String getDefaultSchema() {
        return """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS groups (
                name TEXT PRIMARY KEY,
                creator TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (creator) REFERENCES users(username)
            );
            
            CREATE TABLE IF NOT EXISTS group_members (
                group_name TEXT,
                username TEXT,
                joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (group_name, username),
                FOREIGN KEY (group_name) REFERENCES groups(name),
                FOREIGN KEY (username) REFERENCES users(username)
            );
            
            CREATE TABLE IF NOT EXISTS messages (
                id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                sender TEXT NOT NULL,
                recipient TEXT,
                content TEXT NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (sender) REFERENCES users(username)
            );
            
            CREATE TABLE IF NOT EXISTS message_deliveries (
                message_id TEXT,
                recipient_username TEXT,
                delivered BOOLEAN DEFAULT FALSE,
                delivered_at TIMESTAMP,
                PRIMARY KEY (message_id, recipient_username),
                FOREIGN KEY (message_id) REFERENCES messages(id),
                FOREIGN KEY (recipient_username) REFERENCES users(username)
            );
            
            CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON messages(timestamp);
            CREATE INDEX IF NOT EXISTS idx_message_deliveries_recipient ON message_deliveries(recipient_username);
            """;
    }
}