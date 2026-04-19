package com.habeshagram.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Message implements Serializable {
   private static final long serialVersionUID = 1L;
    
    public enum MessageStatus {
        SENT,      // Message sent by client
        DELIVERED, // Reached server and stored
        READ       // Recipient opened the chat
    }
    
    private String id;
    private MessageType type;
    private String sender;
    private String recipient;
    private String content;
    private LocalDateTime timestamp;
    
    // Reply fields
    private String replyToId;
    private String replyToSender;
    private String replyToContent;
    
    // Status fields
    private MessageStatus status = MessageStatus.SENT;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    
    public Message() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }
    
    public Message(MessageType type, String sender, String content) {
        this();
        this.type = type;
        this.sender = sender;
        this.content = content;
    }
    
    public Message(MessageType type, String sender, String recipient, String content) {
        this(type, sender, content);
        this.recipient = recipient;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

public MessageStatus getStatus() { return status; }
public void setStatus(MessageStatus status) { this.status = status; }

public LocalDateTime getDeliveredAt() { return deliveredAt; }
public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

public LocalDateTime getReadAt() { return readAt; }
public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public boolean isRead() { return status == MessageStatus.READ; }
    public boolean isDelivered() { return status == MessageStatus.DELIVERED || status == MessageStatus.READ; }
    
    public String getReplyToId() { return replyToId; }
    public void setReplyToId(String replyToId) { this.replyToId = replyToId; }
    
    public String getReplyToSender() { return replyToSender; }
    public void setReplyToSender(String replyToSender) { this.replyToSender = replyToSender; }
    
    public String getReplyToContent() { return replyToContent; }
    public void setReplyToContent(String replyToContent) { this.replyToContent = replyToContent; }
    
    public boolean isReply() { 
        return replyToId != null && !replyToId.isEmpty(); 
    }
    
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return timestamp.format(formatter);
    }
    
    @Override
    public String toString() {
        if (type == MessageType.BROADCAST) {
            return String.format("[%s] %s: %s", getFormattedTime(), sender, content);
        } else if (type == MessageType.PRIVATE) {
            return String.format("[%s] %s -> %s: %s", getFormattedTime(), sender, recipient, content);
        } else if (type == MessageType.GROUP) {
            return String.format("[%s] %s @%s: %s", getFormattedTime(), sender, recipient, content);
        }
        return String.format("[%s] %s", getFormattedTime(), content);
    }
}