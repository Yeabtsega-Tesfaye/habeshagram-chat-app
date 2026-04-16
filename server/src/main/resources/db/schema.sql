-- Users table
CREATE TABLE IF NOT EXISTS users (
    username TEXT PRIMARY KEY,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    custom_status TEXT DEFAULT ''
);

-- Groups table
CREATE TABLE IF NOT EXISTS groups (
    name TEXT PRIMARY KEY,
    creator TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator) REFERENCES users(username)
);

-- Group members table
CREATE TABLE IF NOT EXISTS group_members (
    group_name TEXT,
    username TEXT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_name, username),
    FOREIGN KEY (group_name) REFERENCES groups(name),
    FOREIGN KEY (username) REFERENCES users(username)
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    sender TEXT NOT NULL,
    recipient TEXT,
    content TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender) REFERENCES users(username)
);

-- Message deliveries table (for tracking offline messages)
CREATE TABLE IF NOT EXISTS message_deliveries (
    message_id TEXT,
    recipient_username TEXT,
    delivered BOOLEAN DEFAULT FALSE,
    delivered_at TIMESTAMP,
    PRIMARY KEY (message_id, recipient_username),
    FOREIGN KEY (message_id) REFERENCES messages(id),
    FOREIGN KEY (recipient_username) REFERENCES users(username)
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_messages_timestamp ON messages(timestamp);
CREATE INDEX IF NOT EXISTS idx_message_deliveries_recipient ON message_deliveries(recipient_username);
CREATE INDEX IF NOT EXISTS idx_message_deliveries_undelivered ON message_deliveries(recipient_username, delivered);


-- ALTER TABLE messages ADD COLUMN reply_to_id TEXT;
-- ALTER TABLE messages ADD COLUMN reply_to_sender TEXT;
-- ALTER TABLE messages ADD COLUMN reply_to_content TEXT;