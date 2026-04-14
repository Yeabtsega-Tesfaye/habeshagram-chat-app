package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.MessageBubble;
import com.habeshagram.client.ui.components.OnlineUserPanel;
import com.habeshagram.client.util.SwingUtils;
import com.habeshagram.common.model.*;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class MainChatFrame extends JFrame {
    private ChatClient client;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;
    private OnlineUserPanel onlineUserPanel;
    private JPanel chatMessagesPanel;
    private JScrollPane chatScrollPane;
    private Map<String, PrivateChatFrame> privateChats;
    private Set<String> displayedMessageIds = new HashSet<>();
private static final int HISTORY_LIMIT = 50;
    
    private Timer refreshTimer;
    
    public MainChatFrame(ChatClient client) {
        this.client = client;
        this.privateChats = new HashMap<>();
        
        initializeUI();
        setupCallbacks();
        startRefreshTimer();
    }
    
    private void initializeUI() {
        setTitle("Habeshagram - " + client.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        
        // Main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Left panel - Chat area
        JPanel chatPanel = new JPanel(new BorderLayout());
        
        // Chat messages area
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(Color.WHITE);
        
        chatScrollPane = new JScrollPane(chatMessagesPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Right panel - Online users and groups
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        onlineUserPanel = new OnlineUserPanel();
        onlineUserPanel.addUserSelectionListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openPrivateChat();
                }
            }
        });
        
        JPanel groupPanel = createGroupPanel();
        
        rightPanel.add(onlineUserPanel, BorderLayout.CENTER);
        rightPanel.add(groupPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(chatPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(550);
        
        add(splitPane);
        
        // Menu bar
        setJMenuBar(createMenuBar());
        
        SwingUtils.centerOnScreen(this);
        
        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLogout();
            }
        });
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> handleLogout());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        JMenu groupMenu = new JMenu("Groups");
        JMenuItem createGroupItem = new JMenuItem("Create Group");
        createGroupItem.addActionListener(e -> createGroup());
        JMenuItem joinGroupItem = new JMenuItem("Join Group");
        joinGroupItem.addActionListener(e -> joinGroup());
        
        groupMenu.add(createGroupItem);
        groupMenu.add(joinGroupItem);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(groupMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private JPanel createGroupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Groups"));
        panel.setPreferredSize(new Dimension(200, 150));
        
        DefaultListModel<String> groupListModel = new DefaultListModel<>();
        JList<String> groupList = new JList<>(groupListModel);
        
        groupList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedGroup = groupList.getSelectedValue();
                    if (selectedGroup != null) {
                        openGroupChat(selectedGroup);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(groupList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh groups periodically
        new Timer(5000, e -> {
            try {
                groupListModel.clear();
                client.getAvailableGroups().forEach(g -> groupListModel.addElement(g.getName()));
            } catch (RemoteException ex) {
                // Ignore
            }
        }).start();
        
        return panel;
    }
    
    private void addMessageToChat(Message message) {
        boolean isOwnMessage = message.getSender().equals(client.getUsername());
        MessageBubble bubble = new MessageBubble(message, isOwnMessage);
        
        chatMessagesPanel.add(bubble);
        chatMessagesPanel.add(Box.createVerticalStrut(5));
        chatMessagesPanel.revalidate();
        
        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    private void sendMessage() {
        String content = inputField.getText().trim();
        if (!content.isEmpty()) {
            try {
                client.sendBroadcast(content);
                inputField.setText("");
            } catch (RemoteException e) {
                SwingUtils.showError(this, "Error", "Failed to send message: " + e.getMessage());
            }
        }
    }
    
private void openPrivateChat() {
    String selectedUser = onlineUserPanel.getSelectedUser();
    if (selectedUser != null && !selectedUser.equals(client.getUsername())) {
        PrivateChatFrame chatFrame = privateChats.get(selectedUser);
        if (chatFrame == null) {
            chatFrame = new PrivateChatFrame(client, selectedUser);
            privateChats.put(selectedUser, chatFrame);
        }
        chatFrame.setVisible(true);
    }
}
    
private void openGroupChat(String groupName) {
    try {
        // Check if user is a member
        List<String> members = client.getGroupMembers(groupName);
        if (members.contains(client.getUsername())) {
            GroupChatFrame chatFrame = new GroupChatFrame(client, groupName);
            chatFrame.setVisible(true);
        } else {
            SwingUtils.showError(this, "Access Denied", 
                "You are not a member of this group. Please join the group first.");
        }
    } catch (Exception e) {
        SwingUtils.showError(this, "Error", "Failed to open group: " + e.getMessage());
    }
}
    
    private void createGroup() {
        String groupName = SwingUtils.showInput(this, "Create Group", "Enter group name:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            try {
                client.createGroup(groupName.trim());
                SwingUtils.showInfo(this, "Success", "Group created successfully!");
            } catch (RemoteException e) {
                SwingUtils.showError(this, "Error", "Failed to create group: " + e.getMessage());
            }
        }
    }
    
    private void joinGroup() {
        String groupName = SwingUtils.showInput(this, "Join Group", "Enter group name:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            try {
                client.joinGroup(groupName.trim());
                SwingUtils.showInfo(this, "Success", "Joined group successfully!");
            } catch (Exception e) {
                SwingUtils.showError(this, "Error", "Failed to join group: " + e.getMessage());
            }
        }
    }
    
private void startRefreshTimer() {
    refreshTimer = new Timer(3000, e -> {
        try {
            // Get all users (both online and offline)
            List<User> allUsers = client.getAllUsers();
            SwingUtilities.invokeLater(() -> onlineUserPanel.updateUsers(allUsers));
        } catch (RemoteException ex) {
            // Connection might be lost
            refreshTimer.stop();
        }
    });
    refreshTimer.start();
}
    
    private void handleLogout() {
        refreshTimer.stop();
        try {
            client.logout();
        } catch (RemoteException e) {
            // Ignore
        }
        
        // Close all private chats
        privateChats.values().forEach(PrivateChatFrame::dispose);
        
        new LoginFrame().setVisible(true);
        dispose();
    }
    
    private void showAbout() {
        SwingUtils.showInfo(this, "About Habeshagram",
            "Habeshagram Chat Application\n" +
            "Version 1.0\n\n" +
            "A distributed chat system built with Java RMI\n" +
            "Advanced Programming and Distributed Systems Project");
    }

// Modify setupCallbacks() method
private void setupCallbacks() {
    client.getCallbackImpl().addMessageListener(message -> {
        SwingUtilities.invokeLater(() -> {
            // Prevent duplicates
            if (!displayedMessageIds.contains(message.getId())) {
                displayedMessageIds.add(message.getId());
                addMessageToChat(message);
            }
        });
    });
    
    // Load message history after window is visible
    SwingUtilities.invokeLater(() -> {
        loadMessageHistory();
    });
}

// Add new method to load history
private void loadMessageHistory() {
    try {
        List<Message> history = client.getRecentMessages(client.getUsername(), HISTORY_LIMIT);
        for (Message msg : history) {
            if (!displayedMessageIds.contains(msg.getId())) {
                displayedMessageIds.add(msg.getId());
                addMessageToChat(msg);
            }
        }
        
        // Show placeholder if no messages
        if (displayedMessageIds.isEmpty()) {
            showPlaceholder("No messages yet. Start chatting!");
        }
    } catch (RemoteException e) {
        System.err.println("Failed to load message history: " + e.getMessage());
    }
}

// Add placeholder method
private void showPlaceholder(String text) {
    JLabel placeholder = new JLabel(text);
    placeholder.setForeground(Color.GRAY);
    placeholder.setHorizontalAlignment(SwingConstants.CENTER);
    chatMessagesPanel.add(placeholder);
    chatMessagesPanel.revalidate();
}
}