package com.habeshagram.client.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.MessageType;

public class MessageBubble extends JPanel {
    private static final int MAX_BUBBLE_WIDTH = 400;
    private static final int BUBBLE_PADDING = 12;
    
    public MessageBubble(Message message, boolean isOwnMessage) {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        JPanel bubblePanel = createBubblePanel(message, isOwnMessage);
        
        // Add tooltip with full timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        bubblePanel.setToolTipText(message.getTimestamp().format(formatter));
        
        // Align based on ownership
        if (isOwnMessage && message.getType() != MessageType.SYSTEM) {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(bubblePanel);
            add(wrapper, BorderLayout.CENTER);
        } else if (message.getType() == MessageType.SYSTEM) {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(bubblePanel);
            add(wrapper, BorderLayout.CENTER);
        } else {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(bubblePanel);
            add(wrapper, BorderLayout.CENTER);
        }
    }
    
private JPanel createBubblePanel(Message message, boolean isOwnMessage) {
    JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw shadow
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fill(new RoundRectangle2D.Double(2, 2, getWidth() - 1, getHeight() - 1, 12, 12));
            
            // Draw bubble
            Color bgColor = getBubbleColor(message, isOwnMessage);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 3, getHeight() - 3, 12, 12));
            
            g2.dispose();
        }
    };
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(BUBBLE_PADDING, BUBBLE_PADDING, BUBBLE_PADDING, BUBBLE_PADDING));
    
    // Header for non-system messages
    if (message.getType() != MessageType.SYSTEM) {
        if (isOwnMessage) {
            // Simplified header for own messages
            JPanel ownHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            ownHeader.setOpaque(false);
            
            // Icon indicator
            if (message.getType() == MessageType.PRIVATE) {
                JLabel icon = new JLabel("🔒");
                icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
                icon.setToolTipText("Private Message");
                ownHeader.add(icon);
            } else if (message.getType() == MessageType.GROUP) {
                JLabel icon = new JLabel("👥");
                icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
                icon.setToolTipText("Group: " + message.getRecipient());
                ownHeader.add(icon);
            } else if (message.getType() == MessageType.BROADCAST) {
                JLabel icon = new JLabel("📢");
                icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
                icon.setToolTipText("Broadcast Message");
                ownHeader.add(icon);
            }
            
            JLabel timeLabel = new JLabel(message.getFormattedTime());
            timeLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(10f));
            timeLabel.setForeground(ModernTheme.TEXT_MUTED);
            ownHeader.add(timeLabel);
            
            panel.add(ownHeader);
            panel.add(Box.createVerticalStrut(4));
        } else {
            // Full header for others' messages
            JPanel headerPanel = createHeader(message, isOwnMessage);
            panel.add(headerPanel);
            panel.add(Box.createVerticalStrut(4));
        }
    }
    
    // Message content
    JLabel contentLabel = new JLabel("<html><div style='width:" + (MAX_BUBBLE_WIDTH - 40) + "px;'>" 
                                    + message.getContent().replaceAll("\n", "<br>") + "</div></html>");
    contentLabel.setFont(ModernTheme.FONT_MESSAGE);
    contentLabel.setForeground(ModernTheme.TEXT_PRIMARY);
    panel.add(contentLabel);
    
    // Calculate preferred size
    int width = Math.min(MAX_BUBBLE_WIDTH, panel.getPreferredSize().width);
    panel.setPreferredSize(new Dimension(width, panel.getPreferredSize().height));
    
    return panel;
}
    
    private Color getBubbleColor(Message message, boolean isOwnMessage) {
        if (message.getType() == MessageType.SYSTEM) {
            return ModernTheme.BUBBLE_SYSTEM;
        } else if (isOwnMessage) {
            return ModernTheme.BUBBLE_SENT;
        } else {
            return ModernTheme.BUBBLE_RECEIVED;
        }
    }
    
private JPanel createHeader(Message message, boolean isOwnMessage) {
    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    header.setOpaque(false);
    
    // Sender name
    JLabel nameLabel = new JLabel(message.getSender());
    nameLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
    nameLabel.setForeground(ModernTheme.ACCENT_BLUE);
    header.add(nameLabel);
    
    // Icon indicators
    if (message.getType() == MessageType.PRIVATE) {
        JLabel privateIcon = new JLabel("🔒");
        privateIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        privateIcon.setToolTipText("Private Message");
        header.add(privateIcon);
    } else if (message.getType() == MessageType.GROUP) {
        JLabel groupIcon = new JLabel("👥");
        groupIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        groupIcon.setToolTipText("Group: " + message.getRecipient());
        header.add(groupIcon);
    }   else if (message.getType() == MessageType.BROADCAST) {
        JLabel broadcastIcon = new JLabel("📢");
        broadcastIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        broadcastIcon.setToolTipText("Broadcast Message");
        header.add(broadcastIcon);
    }
    
    
    // Separator dot
    JLabel dotLabel = new JLabel("•");
    dotLabel.setFont(ModernTheme.FONT_SMALL);
    dotLabel.setForeground(ModernTheme.TEXT_MUTED);
    header.add(dotLabel);
    
    // Time
    JLabel timeLabel = new JLabel(message.getFormattedTime());
    timeLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(10f));
    timeLabel.setForeground(ModernTheme.TEXT_MUTED);
    header.add(timeLabel);
    
    return header;
}
    
    private JPanel createFooter(Message message, boolean isOwnMessage) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        footer.setOpaque(false);
        
        JLabel timeLabel = new JLabel(message.getFormattedTime());
        timeLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(10f));
        timeLabel.setForeground(isOwnMessage ? new Color(255, 255, 255, 180) : ModernTheme.TEXT_MUTED);
        footer.add(timeLabel);
        
        return footer;
    }
}