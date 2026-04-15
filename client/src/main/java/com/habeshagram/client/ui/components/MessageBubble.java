package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;

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
                
                Color bgColor = getBubbleColor(message, isOwnMessage);
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(BUBBLE_PADDING, BUBBLE_PADDING, BUBBLE_PADDING, BUBBLE_PADDING));
        
        // Header (only for group/private messages)
        if (message.getType() != MessageType.SYSTEM && !isOwnMessage) {
            JPanel headerPanel = createHeader(message);
            panel.add(headerPanel);
            panel.add(Box.createVerticalStrut(4));
        }
        
        // Message content
        JLabel contentLabel = new JLabel("<html><div style='width:" + (MAX_BUBBLE_WIDTH - 40) + "px;'>" 
                                        + message.getContent().replaceAll("\n", "<br>") + "</div></html>");
        contentLabel.setFont(ModernTheme.FONT_MESSAGE);
        contentLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        panel.add(contentLabel);
        
        // Footer with time
        JPanel footerPanel = createFooter(message, isOwnMessage);
        panel.add(Box.createVerticalStrut(4));
        panel.add(footerPanel);
        
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
    
    private JPanel createHeader(Message message) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        
        String headerText = message.getSender();
        if (message.getType() == MessageType.GROUP) {
            headerText += " • " + message.getRecipient();
        }
        
        JLabel nameLabel = new JLabel(headerText);
        nameLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
        nameLabel.setForeground(ModernTheme.ACCENT_BLUE);
        header.add(nameLabel);
        
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