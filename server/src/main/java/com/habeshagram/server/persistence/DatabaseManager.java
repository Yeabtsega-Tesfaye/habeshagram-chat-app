package com.habeshagram.server.persistence;

import java.sql.*;

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
        String[] schemaStatements = {
            // Users table
            "CREATE TABLE IF NOT EXISTS users (" +
            "username TEXT PRIMARY KEY," +
            "password_hash TEXT NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "custom_status TEXT DEFAULT '')",
            
            // Groups table
            "CREATE TABLE IF NOT EXISTS groups (" +
            "name TEXT PRIMARY KEY," +
            "creator TEXT NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (creator) REFERENCES users(username))",
            
            // Group members table
            "CREATE TABLE IF NOT EXISTS group_members (" +
            "group_name TEXT," +
            "username TEXT," +
            "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "PRIMARY KEY (group_name, username)," +
            "FOREIGN KEY (group_name) REFERENCES groups(name)," +
            "FOREIGN KEY (username) REFERENCES users(username))",
            
            // Messages table WITH reply columns
            "CREATE TABLE IF NOT EXISTS messages (" +
            "id TEXT PRIMARY KEY," +
            "type TEXT NOT NULL," +
            "sender TEXT NOT NULL," +
            "recipient TEXT," +
            "content TEXT NOT NULL," +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "reply_to_id TEXT," +
            "reply_to_sender TEXT," +
            "reply_to_content TEXT," +
            "FOREIGN KEY (sender) REFERENCES users(username))",
            
            // Message deliveries table
            "CREATE TABLE IF NOT EXISTS message_deliveries (" +
            "message_id TEXT," +
            "recipient_username TEXT," +
            "delivered BOOLEAN DEFAULT FALSE," +
            "delivered_at TIMESTAMP," +
            "PRIMARY KEY (message_id, recipient_username)," +
            "FOREIGN KEY (message_id) REFERENCES messages(id)," +
            "FOREIGN KEY (recipient_username) REFERENCES users(username))",
            
            // Indexes
            "CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON messages(timestamp)",
            "CREATE INDEX IF NOT EXISTS idx_message_deliveries_recipient ON message_deliveries(recipient_username)"
        };
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            for (String sql : schemaStatements) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    System.err.println("Error executing: " + sql.substring(0, Math.min(60, sql.length())) + "...");
                    System.err.println("Error: " + e.getMessage());
                }
            }
            
            // Try to add reply columns if they don't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE messages ADD COLUMN reply_to_id TEXT");
                System.out.println("Added reply_to_id column");
            } catch (SQLException e) {
                // Column already exists - ignore
            }
            
            try {
                stmt.execute("ALTER TABLE messages ADD COLUMN reply_to_sender TEXT");
                System.out.println("Added reply_to_sender column");
            } catch (SQLException e) {
                // Column already exists - ignore
            }
            
            try {
                stmt.execute("ALTER TABLE messages ADD COLUMN reply_to_content TEXT");
                System.out.println("Added reply_to_content column");
            } catch (SQLException e) {
                // Column already exists - ignore
            }
            
            System.out.println("Database schema initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}