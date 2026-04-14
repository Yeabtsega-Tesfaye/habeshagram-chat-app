package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.MessageBubble;
import com.habeshagram.common.exception.GroupNotFoundException;
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

public class GroupChatFrame extends JFrame {
    private ChatClient client;
    private String groupName;
    private JTextField inputField;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextArea membersArea;
    private Set<String> displayedMessageIds = new HashSet<>();
    private static final int HISTORY_LIMIT = 50;
    private JLabel placeholderLabel;
    
    public GroupChatFrame(ChatClient client, String groupName) {
        this.client = client;
        this.groupName = groupName;
        
        initializeUI();
        setupCallback();
        loadGroupMembers();
    }
    
private void initializeUI() {
    setTitle("Group Chat - " + groupName);
    setSize(600, 500);
    
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    
    // Messages area
    JPanel chatPanel = new JPanel(new BorderLayout());
    
    messagesPanel = new JPanel();
    messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
    messagesPanel.setBackground(Color.WHITE);
    
    // Add placeholder
    placeholderLabel = new JLabel("No messages in this group yet. Start the conversation!");
    placeholderLabel.setForeground(Color.GRAY);
    placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
    placeholderLabel.setVisible(false);
    messagesPanel.add(placeholderLabel);
    
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
    
    chatPanel.add(scrollPane, BorderLayout.CENTER);
    chatPanel.add(inputPanel, BorderLayout.SOUTH);
    
    // Members area
    JPanel membersPanel = new JPanel(new BorderLayout());
    membersPanel.setBorder(BorderFactory.createTitledBorder("Members"));
    
    membersArea = new JTextArea();
    membersArea.setEditable(false);
    JScrollPane membersScroll = new JScrollPane(membersArea);
    membersPanel.add(membersScroll, BorderLayout.CENTER);
    
    splitPane.setLeftComponent(chatPanel);
    splitPane.setRightComponent(membersPanel);
    splitPane.setDividerLocation(400);
    
    add(splitPane);
    
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
        if (message.getType() == MessageType.GROUP &&
            groupName.equals(message.getRecipient())) {
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
        loadGroupHistory();
    });
}
    
    private void sendMessage() {
        String content = inputField.getText().trim();
        if (!content.isEmpty()) {
            try {
                client.sendGroup(groupName, content);
                inputField.setText("");
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to send message: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadGroupMembers() {
        try {
            java.util.List<String> members = client.getGroupMembers(groupName);
            StringBuilder sb = new StringBuilder();
            for (String member : members) {
                sb.append("• ").append(member).append("\n");
            }
            membersArea.setText(sb.toString());
        } catch (RemoteException | GroupNotFoundException e) {
            membersArea.setText("Failed to load members: " + e.getMessage());
        }
    }

private void loadGroupHistory() {
    try {
        List<Message> history = client.getGroupHistory(client.getUsername(), groupName, HISTORY_LIMIT);
        
        for (Message msg : history) {
            if (!displayedMessageIds.contains(msg.getId())) {
                displayedMessageIds.add(msg.getId());
                addMessageToUI(msg);
            }
        }
        
        updatePlaceholder();
        
    } catch (RemoteException e) {
        System.err.println("Failed to load group history: " + e.getMessage());
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