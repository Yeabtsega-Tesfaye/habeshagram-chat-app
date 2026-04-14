package com.habeshagram.client.ui.components;

import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MessageBubble extends JPanel {
    private static final Color SYSTEM_COLOR = new Color(220, 220, 220);
    private static final Color SENT_COLOR = new Color(0, 153, 76);
    private static final Color RECEIVED_COLOR = new Color(230, 230, 230);
    private static final Color BROADCAST_COLOR = new Color(255, 235, 156);
    
    public MessageBubble(Message message, boolean isOwnMessage) {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        JPanel bubblePanel = new JPanel();
        bubblePanel.setLayout(new BoxLayout(bubblePanel, BoxLayout.Y_AXIS));
        bubblePanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // Set background color based on message type
        Color bgColor;
        if (message.getType() == MessageType.SYSTEM) {
            bgColor = SYSTEM_COLOR;
        } else if (message.getType() == MessageType.BROADCAST) {
            bgColor = BROADCAST_COLOR;
        } else if (isOwnMessage) {
            bgColor = SENT_COLOR;
        } else {
            bgColor = RECEIVED_COLOR;
        }
        
        bubblePanel.setBackground(bgColor);
        
        // Create rounded border
        bubblePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        // Header (sender and time)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        headerPanel.setOpaque(false);
        
        String headerText = message.getSender();
        if (message.getType() == MessageType.GROUP) {
            headerText += " @" + message.getRecipient();
        } else if (message.getType() == MessageType.PRIVATE && !isOwnMessage) {
            headerText += " (private)";
        }
        
        JLabel senderLabel = new JLabel(headerText);
        senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD, 11f));
        headerPanel.add(senderLabel);
        
        JLabel timeLabel = new JLabel(message.getFormattedTime());
        timeLabel.setFont(timeLabel.getFont().deriveFont(10f));
        timeLabel.setForeground(Color.GRAY);
        headerPanel.add(timeLabel);
        
        bubblePanel.add(headerPanel);
        
        // Message content
        JLabel contentLabel = new JLabel("<html>" + message.getContent().replaceAll("\n", "<br>") + "</html>");
        contentLabel.setFont(contentLabel.getFont().deriveFont(12f));
        bubblePanel.add(contentLabel);
        
        // Align based on ownership
        if (isOwnMessage && message.getType() != MessageType.SYSTEM) {
            add(bubblePanel, BorderLayout.EAST);
        } else {
            add(bubblePanel, BorderLayout.WEST);
        }
    }
}