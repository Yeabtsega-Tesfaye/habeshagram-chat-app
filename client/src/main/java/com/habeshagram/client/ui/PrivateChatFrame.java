package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.MessageBubble;
import com.habeshagram.common.model.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingConstants;
import com.habeshagram.common.model.MessageType;

public class PrivateChatFrame extends JFrame {
    private ChatClient client;
    private String recipient;
    private JTextArea messageArea;
    private JTextField inputField;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private Set<String> displayedMessageIds = new HashSet<>();
    private static final int HISTORY_LIMIT = 50;
    private JLabel placeholderLabel;
    
    public PrivateChatFrame(ChatClient client, String recipient) {
        this.client = client;
        this.recipient = recipient;
        
        initializeUI();
        setupCallback();
    }
    
    private void initializeUI() {
        setTitle("Private Chat - " + recipient);
        setSize(400, 500);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Messages area
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(Color.WHITE);
        
        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(Color.WHITE);

        placeholderLabel = new JLabel("No messages yet. say hello!");
        placeholderLabel.setForeground(Color.GRAY);
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setVisible(false);
        messagesPanel.add(placeholderLabel);
        
        add(mainPanel);
        
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
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
                }
            });
        }
    });
    
    // Load history after window is visible
    SwingUtilities.invokeLater(() -> {
        loadConversationHistory();
    });
    }
    
    private void addMessage(Message message) {
        boolean isOwnMessage = message.getSender().equals(client.getUsername());
        MessageBubble bubble = new MessageBubble(message, isOwnMessage);
        
        messagesPanel.add(bubble);
        messagesPanel.add(Box.createVerticalStrut(5));
        messagesPanel.revalidate();
        
        // Auto-scroll
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
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
                    addMessage(msg);
                }
            }
            
            // Update placeholder visibility
            updatePlaceholder();
            
        } catch (RemoteException e) {
            System.err.println("Failed to load conversation history: " + e.getMessage());
        }
    }

    private void addMessageToUI(Message message) {
    placeholderLabel.setVisible(false);
    
    boolean isOwnMessage = message.getSender().equals(client.getUsername());
    MessageBubble bubble = new MessageBubble(message, isOwnMessage);
    
    messagesPanel.add(bubble);
    messagesPanel.add(Box.createVerticalStrut(5));
    messagesPanel.revalidate();
    messagesPanel.repaint();
    
    // Auto-scroll
    SwingUtilities.invokeLater(() -> {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    });
}

// Update placeholder visibility
private void updatePlaceholder() {
    placeholderLabel.setVisible(displayedMessageIds.isEmpty());
}

    
}

