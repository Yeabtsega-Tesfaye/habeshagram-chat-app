package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.MessageBubble;
import com.habeshagram.common.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingConstants;

import com.habeshagram.client.ui.components.ModernButton;
import com.habeshagram.client.ui.theme.ModernTheme;
import java.awt.geom.RoundRectangle2D;

public class PrivateChatFrame extends JFrame {
    private ChatClient client;
    private String recipient;
    private JTextField inputField;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private Set<String> displayedMessageIds = new HashSet<>();
    private static final int HISTORY_LIMIT = 50;
    private JLabel placeholderLabel;
    private int unreadCount = 0;
    private boolean hasFocus = false;
    
    public PrivateChatFrame(ChatClient client, String recipient) {
        this.client = client;
        this.recipient = recipient;
        
        initializeUI();
        setupCallback();

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                hasFocus = true;
                unreadCount = 0;
                updateTitle();
            }

            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                hasFocus = false;
            }
        });
    }
    
private void initializeUI() {
    ModernTheme.applyTheme();
    setTitle("Private Chat - " + recipient);
    setSize(450, 600);
    getContentPane().setBackground(ModernTheme.BACKGROUND_DARK);
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(ModernTheme.BACKGROUND_DARK);
    
    // Header with recipient name and status
    JPanel headerPanel = createHeaderPanel();
    mainPanel.add(headerPanel, BorderLayout.NORTH);
    
    // Messages area
    messagesPanel = new JPanel();
    messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
    messagesPanel.setBackground(ModernTheme.BACKGROUND_CHAT);
    messagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Add vertical glue at the top
    messagesPanel.add(Box.createVerticalGlue());
    
    // Add placeholder
    placeholderLabel = new JLabel("No messages yet. Say hello!");
    placeholderLabel.setForeground(ModernTheme.TEXT_MUTED);
    placeholderLabel.setFont(ModernTheme.FONT_BODY);
    placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
    placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    placeholderLabel.setVisible(false);
    messagesPanel.add(placeholderLabel);
    
    scrollPane = new JScrollPane(messagesPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(null);
    scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_CHAT);
    
    // Input area
    JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
    inputPanel.setBackground(ModernTheme.BACKGROUND_DARK);
    inputPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    
    inputField = new JTextField() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ModernTheme.BACKGROUND_MEDIUM);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
            g2.dispose();
            super.paintComponent(g);
        }
    };
    inputField.setOpaque(false);
    inputField.setForeground(ModernTheme.TEXT_PRIMARY);
    inputField.setCaretColor(ModernTheme.TEXT_PRIMARY);
    inputField.setFont(ModernTheme.FONT_BODY);
    inputField.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    inputField.addActionListener(e -> sendMessage());
    
    ModernButton sendButton = new ModernButton("Send");
    sendButton.setPreferredSize(new Dimension(80, 40));
    sendButton.addActionListener(e -> sendMessage());
    
    inputPanel.add(inputField, BorderLayout.CENTER);
    inputPanel.add(sendButton, BorderLayout.EAST);
    
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    mainPanel.add(inputPanel, BorderLayout.SOUTH);
    
    add(mainPanel);
    setLocationRelativeTo(null);
    
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            dispose();
        }
    });
}

private JPanel createHeaderPanel() {
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(ModernTheme.BACKGROUND_MEDIUM);
    header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
    
    JLabel nameLabel = new JLabel(recipient);
    nameLabel.setFont(ModernTheme.FONT_HEADER);
    nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
    
    // Check online status
    try {
        java.util.List<com.habeshagram.common.model.User> users = client.getAllUsers();
        for (com.habeshagram.common.model.User user : users) {
            if (user.getUsername().equals(recipient)) {
                String status = user.getStatus() == com.habeshagram.common.model.UserStatus.ONLINE ? "● Online" : "○ Offline";
                JLabel statusLabel = new JLabel(status);
                statusLabel.setFont(ModernTheme.FONT_SMALL);
                statusLabel.setForeground(user.getStatus() == com.habeshagram.common.model.UserStatus.ONLINE ? 
                                         ModernTheme.ONLINE : ModernTheme.OFFLINE);
                header.add(statusLabel, BorderLayout.EAST);
                break;
            }
        }
    } catch (Exception e) {
        // Ignore
    }
    
    header.add(nameLabel, BorderLayout.WEST);
    
    return header;
}
    
private void setupCallback() {
    client.getCallbackImpl().addMessageListener(message -> {
        if (message.getType() == MessageType.PRIVATE &&
            (message.getSender().equals(recipient) || 
             (message.getRecipient() != null && message.getRecipient().equals(recipient)))) {
            SwingUtilities.invokeLater(() -> {
                if (!displayedMessageIds.contains(message.getId())) {
                    displayedMessageIds.add(message.getId());
                    addMessageToUI(message);
                    
                    // Count unread messages from recipient
                    if (!hasFocus && message.getSender().equals(recipient)) {
                        unreadCount++;
                        updateTitle();
                    }
                }
            });

            if(!hasFocus && message.getSender().equals(recipient)) {
                playNotificationSound();
            }
        }
    });
    
    SwingUtilities.invokeLater(() -> {
        loadConversationHistory();
    });
}

private void updateTitle() {
    if (unreadCount > 0) {
        setTitle("Private Chat - " + recipient + " (" + unreadCount + ")");
    } else {
        setTitle("Private Chat - " + recipient);
    }
}
  
    
    private void loadConversationHistory() {
        try {
            List<Message> history = client.getPrivateHistory(
                client.getUsername(), 
                recipient, 
                HISTORY_LIMIT
            );
            
            for (Message msg : history) {
                if (!displayedMessageIds.contains(msg.getId())) {
                    displayedMessageIds.add(msg.getId());
                    addMessageToUI(msg);
                }
            }
            
            updatePlaceholder();
            
        } catch (RemoteException e) {
            System.err.println("Failed to load conversation history: " + e.getMessage());
        }
    }
    
    private void addMessageToUI(Message message) {
        placeholderLabel.setVisible(false);
        
        boolean isOwnMessage = message.getSender().equals(client.getUsername());
        MessageBubble bubble = new MessageBubble(message, isOwnMessage);
        

        int insertPosition = messagesPanel.getComponentCount() - 1; // Before the vertical glue
        messagesPanel.add(bubble, insertPosition);
        messagesPanel.add(Box.createVerticalStrut(5), insertPosition + 1);
        messagesPanel.revalidate();
        messagesPanel.repaint();
        
        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    private void updatePlaceholder() {
        placeholderLabel.setVisible(displayedMessageIds.isEmpty());
    }
    
    private void sendMessage() {
        String content = inputField.getText().trim();
        if (!content.isEmpty()) {
            try {
                client.sendPrivate(recipient, content);
                inputField.setText("");
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to send message: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

private void playNotificationSound() {
    Toolkit.getDefaultToolkit().beep();
}


}