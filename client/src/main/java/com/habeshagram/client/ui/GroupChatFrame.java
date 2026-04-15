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

import com.habeshagram.client.ui.components.ModernButton;
import com.habeshagram.client.ui.theme.ModernTheme;
import java.awt.geom.RoundRectangle2D;

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
    private int unreadCount = 0;
    private boolean hasFocus = false;
    
    public GroupChatFrame(ChatClient client, String groupName) {
        this.client = client;
        this.groupName = groupName;
        
        initializeUI();
        setupCallback();
        loadGroupMembers();

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                hasFocus = true;
                unreadCount = 0;
                updateTitle();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                hasFocus = false;
            }
        });
    }
    
private void initializeUI() {
    ModernTheme.applyTheme();
    setTitle("Group Chat - " + groupName);
    setSize(900, 900);
    
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    
    // Messages area
    JPanel chatPanel = new JPanel(new BorderLayout());

    JPanel headerPanel = createHeaderPanel();
    chatPanel.add(headerPanel, BorderLayout.NORTH);
    
    messagesPanel = new JPanel();
    messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
    messagesPanel.setBackground(ModernTheme.BACKGROUND_DARK);
    messagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    messagesPanel.add(Box.createVerticalGlue()); // Push messages to the top
    
    // Add placeholder
    placeholderLabel = new JLabel("No messages in this group yet. Start the conversation!");
    placeholderLabel.setForeground(ModernTheme.TEXT_MUTED);
    placeholderLabel.setFont(ModernTheme.FONT_BODY);
    placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
    placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    placeholderLabel.setVisible(false);
    messagesPanel.add(placeholderLabel);
    
    scrollPane = new JScrollPane(messagesPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    
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
    splitPane.setDividerLocation(600);
    
    add(splitPane);
    
    setLocationRelativeTo(null);
    
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            dispose();
        }
    });
}

// Header with member count
private JPanel createHeaderPanel() {
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(ModernTheme.BACKGROUND_MEDIUM);
    header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
    
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    leftPanel.setOpaque(false);
    
    JLabel nameLabel = new JLabel("# " + groupName);
    nameLabel.setFont(ModernTheme.FONT_HEADER);
    nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
    leftPanel.add(nameLabel);
    
    try {
        java.util.List<String> members = client.getGroupMembers(groupName);
        JLabel countLabel = new JLabel(members.size() + " members");
        countLabel.setFont(ModernTheme.FONT_SMALL);
        countLabel.setForeground(ModernTheme.TEXT_MUTED);
        leftPanel.add(countLabel);
    } catch (Exception e) {
        // Ignore
    }
    
    header.add(leftPanel, BorderLayout.WEST);
    
    return header;
}
    
    private void setupCallback() {
    client.getCallbackImpl().addMessageListener(message -> {
        if (message.getType() == MessageType.GROUP &&
            groupName.equals(message.getRecipient())) {
            SwingUtilities.invokeLater(() -> {
                if (!displayedMessageIds.contains(message.getId())) {
                    displayedMessageIds.add(message.getId());
                    addMessageToUI(message);

                    if (!hasFocus && !message.getSender().equals(client.getUsername())) {
                        unreadCount++;
                        updateTitle();
                    }
                }
            });

            if(!hasFocus && !message.getSender().equals(client.getUsername())) {
                playNotificationSound();
            }
        }
    });
    
    // Load history after window is visible
    SwingUtilities.invokeLater(() -> {
        loadGroupHistory();
    });
}

private void updateTitle() {
    if (unreadCount > 0) {
        setTitle("Group Chat - " + groupName + " (" + unreadCount + ")");
    } else {
        setTitle("Group Chat - " + groupName);
    }
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

    int insertPosition = messagesPanel.getComponentCount() - 1; // Before the vertical glue
    messagesPanel.add(bubble, insertPosition);
    messagesPanel.add(Box.createVerticalStrut(5), insertPosition + 1); // Add spacing after the message
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

private void playNotificationSound() {
    Toolkit.getDefaultToolkit().beep();
}


}