package com.habeshagram.server.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.MessageType;

public class MessageDAO {

    public void saveMessage(Message message) {
        String sql = "INSERT INTO messages (id, type, sender, recipient, content, timestamp, " +
                "reply_to_id, reply_to_sender, reply_to_content) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, message.getId());
            pstmt.setString(2, message.getType().name());
            pstmt.setString(3, message.getSender());
            pstmt.setString(4, message.getRecipient());
            pstmt.setString(5, message.getContent());
            pstmt.setTimestamp(6, Timestamp.valueOf(message.getTimestamp()));
            pstmt.setString(7, message.getReplyToId());
            pstmt.setString(8, message.getReplyToSender());
            pstmt.setString(9, message.getReplyToContent());

            pstmt.executeUpdate();

            // Create delivery records based on message type
            createDeliveryRecords(message);

        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    private void createDeliveryRecords(Message message) {
        List<String> recipients = new ArrayList<>();

        switch (message.getType()) {
            case BROADCAST:
            case SYSTEM:
                // Get all users except sender
                recipients = getAllUsersExcept(message.getSender());
                break;

            case PRIVATE:
                recipients.add(message.getRecipient());
                recipients.add(message.getSender()); // For sent items
                break;

            case GROUP:
                // Get group members except sender
                recipients = getGroupMembersExcept(message.getRecipient(), message.getSender());
                break;
        }

        String sql = "INSERT INTO message_deliveries (message_id, recipient_username) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (String recipient : recipients) {
                pstmt.setString(1, message.getId());
                pstmt.setString(2, recipient);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Error creating delivery records: " + e.getMessage());
        }
    }

    public List<Message> getPendingMessages(String username) {
        List<Message> messages = new ArrayList<>();
        String sql = """
                SELECT m.* FROM messages m
                JOIN message_deliveries md ON m.id = md.message_id
                WHERE md.recipient_username = ? AND md.delivered = FALSE
                ORDER BY m.timestamp
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Just use the extract method instead of manual setting
                Message message = extractMessageFromResultSet(rs);
                messages.add(message);
            }

        } catch (SQLException e) {
            System.err.println("Error getting pending messages: " + e.getMessage());
        }

        return messages;
    }

    public void markAsDelivered(String messageId, String username) {
        String sql = """
                UPDATE message_deliveries
                SET delivered = TRUE, delivered_at = ?
                WHERE message_id = ? AND recipient_username = ?
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(2, messageId);
            pstmt.setString(3, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error marking message as delivered: " + e.getMessage());
        }
    }

    private List<String> getAllUsersExcept(String excludeUser) {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users WHERE username != ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, excludeUser);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }

        return users;
    }

    private List<String> getGroupMembersExcept(String groupName, String excludeUser) {
        List<String> members = new ArrayList<>();
        String sql = """
                SELECT username FROM group_members
                WHERE group_name = ? AND username != ?
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupName);
            pstmt.setString(2, excludeUser);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting group members: " + e.getMessage());
        }

        return members;
    }

    // Add to MessageDAO.java

    public List<Message> getRecentBroadcastMessages(int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = """
                SELECT * FROM messages
                WHERE type IN ('BROADCAST', 'SYSTEM')
                ORDER BY timestamp DESC
                LIMIT ?
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = extractMessageFromResultSet(rs);
                messages.add(message);
            }

        } catch (SQLException e) {
            System.err.println("Error getting recent broadcast messages: " + e.getMessage());
        }

        // Reverse to get chronological order (oldest first)
        Collections.reverse(messages);
        return messages;
    }

    public List<Message> getPrivateConversation(String user1, String user2, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = """
                SELECT * FROM messages
                WHERE type = 'PRIVATE'
                AND ((sender = ? AND recipient = ?) OR (sender = ? AND recipient = ?))
                ORDER BY timestamp DESC
                LIMIT ?
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            pstmt.setInt(5, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = extractMessageFromResultSet(rs);
                messages.add(message);
            }

        } catch (SQLException e) {
            System.err.println("Error getting private conversation: " + e.getMessage());
        }

        Collections.reverse(messages);
        return messages;
    }

    public List<Message> getGroupConversation(String groupName, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = """
                SELECT * FROM messages
                WHERE type = 'GROUP' AND recipient = ?
                ORDER BY timestamp DESC
                LIMIT ?
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, groupName);
            pstmt.setInt(2, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = extractMessageFromResultSet(rs);
                messages.add(message);
            }

        } catch (SQLException e) {
            System.err.println("Error getting group conversation: " + e.getMessage());
        }

        Collections.reverse(messages);
        return messages;
    }

    private Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getString("id"));
        message.setType(MessageType.valueOf(rs.getString("type")));
        message.setSender(rs.getString("sender"));
        message.setRecipient(rs.getString("recipient"));
        message.setContent(rs.getString("content"));
        message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());

        // ADD THESE LINES - Extract reply data
        message.setReplyToId(rs.getString("reply_to_id"));
        message.setReplyToSender(rs.getString("reply_to_sender"));
        message.setReplyToContent(rs.getString("reply_to_content"));

        return message;
    }

    public Message getMessageById(String messageId) {
        String sql = "SELECT * FROM messages WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, messageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractMessageFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting message: " + e.getMessage());
        }

        return null;
    }

    public void deleteMessage(String messageId) {
        String sql = "DELETE FROM messages WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, messageId);
            pstmt.executeUpdate();

            // Also delete from deliveries
            String deleteDeliveries = "DELETE FROM message_deliveries WHERE message_id = ?";
            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteDeliveries)) {
                pstmt2.setString(1, messageId);
                pstmt2.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
        }
    }

    public List<Message> getRecentPrivateMessagesForUser(String username, int limit) {
    List<Message> messages = new ArrayList<>();
    String sql = """
        SELECT * FROM messages 
        WHERE type = 'PRIVATE' 
        AND (sender = ? OR recipient = ?)
        ORDER BY timestamp DESC 
        LIMIT ?
        """;
    
    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, username);
        pstmt.setString(2, username);
        pstmt.setInt(3, limit);
        
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Message message = extractMessageFromResultSet(rs);
            messages.add(message);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting recent private messages: " + e.getMessage());
    }
    
    Collections.reverse(messages);
    return messages;
}

public List<Message> getRecentGroupMessagesForUser(String username, int limit) {
    List<Message> messages = new ArrayList<>();
    String sql = """
        SELECT m.* FROM messages m
        JOIN group_members gm ON m.recipient = gm.group_name
        WHERE m.type = 'GROUP' 
        AND gm.username = ?
        ORDER BY m.timestamp DESC 
        LIMIT ?
        """;
    
    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, username);
        pstmt.setInt(2, limit);
        
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            Message message = extractMessageFromResultSet(rs);
            messages.add(message);
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting recent group messages: " + e.getMessage());
    }
    
    Collections.reverse(messages);
    return messages;
}

}