package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.ContextMenuFactory;
import com.habeshagram.client.ui.components.EmojiPicker;
import com.habeshagram.client.ui.components.MessageBubble;
import com.habeshagram.client.ui.components.OnlineUserPanel;
import com.habeshagram.client.util.SwingUtils;
import com.habeshagram.common.model.*;
import com.habeshagram.common.exception.GroupNotFoundException;
import com.habeshagram.client.ui.theme.ModernTheme;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.RoundRectangle2D;
import com.habeshagram.client.ui.components.ModernButton;
import com.habeshagram.client.util.ClipboardHelper;
import com.habeshagram.client.util.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class MainChatFrame extends JFrame {
    private ChatClient client;
    private JTextField inputField;
    private JButton sendButton;
    private OnlineUserPanel onlineUserPanel;
    private JPanel chatMessagesPanel;
    private JScrollPane chatScrollPane;
    private Map<String, PrivateChatFrame> privateChats;
    private Set<String> displayedMessageIds = new HashSet<>();
    private static final int HISTORY_LIMIT = 50;
    private boolean hasFocus = false;
    private JLabel typingLabel;
    private Timer typingTimer;
    private boolean isTyping = false;
    private String currentTypingUser = null;
    private Timer refreshTimer;
    private JLabel onlineCountLabel;

    public MainChatFrame(ChatClient client) {
        this.client = client;
        this.privateChats = new HashMap<>();

        initializeUI();
        setupCallbacks();
        startRefreshTimer();
    }

    private void initializeUI() {
        ModernTheme.applyTheme();
        setTitle("Habeshagram - " + client.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 1000);
        getContentPane().setBackground(ModernTheme.BACKGROUND_DARK);

        // Main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(1);

        // Left panel - Chat area
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(ModernTheme.BACKGROUND_DARK);

        // Chat header
        // Chat header - MODERN CENTERED STYLE
        JPanel chatHeader = new JPanel();
        chatHeader.setLayout(new BoxLayout(chatHeader, BoxLayout.Y_AXIS));
        chatHeader.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        chatHeader.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        // Home icon and title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        titlePanel.setOpaque(false);

        JLabel homeIcon = new JLabel("🏠");
        homeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        titlePanel.add(homeIcon);

        JLabel chatTitle = new JLabel("Home");
        chatTitle.setFont(ModernTheme.FONT_TITLE);
        chatTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        titlePanel.add(chatTitle);

        // Welcome message
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        welcomePanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + client.getUsername() + "!");
        welcomeLabel.setFont(ModernTheme.FONT_BODY);
        welcomeLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        welcomePanel.add(welcomeLabel);

        // Online count
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setOpaque(false);

        JLabel onlineIcon = new JLabel("🟢");
        onlineIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        statusPanel.add(onlineIcon);

        onlineCountLabel = new JLabel("0 online now");
        onlineCountLabel.setFont(ModernTheme.FONT_SMALL);
        onlineCountLabel.setForeground(ModernTheme.ONLINE);
        onlineCountLabel.setName("onlineCountLabel"); // For updating later
        statusPanel.add(onlineCountLabel);

        chatHeader.add(titlePanel);
        chatHeader.add(Box.createVerticalStrut(4));
        chatHeader.add(welcomePanel);
        chatHeader.add(Box.createVerticalStrut(4));
        chatHeader.add(statusPanel);

        // Add a subtle separator line
        JSeparator separator = new JSeparator();
        separator.setForeground(ModernTheme.BACKGROUND_LIGHT);
        chatHeader.add(separator);

        chatPanel.add(chatHeader, BorderLayout.NORTH);

        // Chat messages area
        chatMessagesPanel = new JPanel();
        chatMessagesPanel.setLayout(new BoxLayout(chatMessagesPanel, BoxLayout.Y_AXIS));
        chatMessagesPanel.setBackground(ModernTheme.BACKGROUND_CHAT);
        chatMessagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatScrollPane = new JScrollPane(chatMessagesPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatScrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_CHAT);

        // Typing label
        typingLabel = new JLabel(" ");
        typingLabel.setFont(ModernTheme.FONT_SMALL);
        typingLabel.setForeground(ModernTheme.TEXT_MUTED);
        typingLabel.setBorder(BorderFactory.createEmptyBorder(4, 16, 2, 16));
        typingLabel.setBackground(ModernTheme.BACKGROUND_DARK);
        typingLabel.setOpaque(true);

        // Input panel
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

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isTyping) {
                    isTyping = true;
                    try {
                        client.sendBroadcastTypingIndicator(client.getUsername());
                    } catch (RemoteException ex) {
                        // Ignore
                    }
                    new Timer(1000, evt -> isTyping = false).start();
                }
            }
        });

        JPanel leftInputPanel = new JPanel(new BorderLayout(5, 0));
        leftInputPanel.setOpaque(false);

        ModernButton emojiButton = new ModernButton("😊");
        emojiButton.setPreferredSize(new Dimension(45, 40));
        emojiButton.addActionListener(e -> {
            EmojiPicker picker = new EmojiPicker(emoji -> {
                inputField.setText(inputField.getText() + emoji);
            });
            picker.show(emojiButton, 0, -picker.getPreferredSize().height);
        });

        leftInputPanel.add(emojiButton, BorderLayout.WEST);
        leftInputPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new ModernButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(leftInputPanel, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Bottom panel with typing indicator
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ModernTheme.BACKGROUND_DARK);
        bottomPanel.add(typingLabel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        chatPanel.add(chatHeader, BorderLayout.NORTH);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Right panel - Online users and groups
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(ModernTheme.BACKGROUND_DARK);

        onlineUserPanel = new OnlineUserPanel();
        onlineUserPanel.addUserSelectionListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openPrivateChat();
                }
            }
        });

        onlineUserPanel.setOnMessageUser(user -> {
            if (!user.getUsername().equals(client.getUsername())) {
                PrivateChatFrame chatFrame = privateChats.get(user.getUsername());
                if (chatFrame == null) {
                    chatFrame = new PrivateChatFrame(client, user.getUsername());
                    privateChats.put(user.getUsername(), chatFrame);
                }
                chatFrame.setVisible(true);
            }
        });

        onlineUserPanel.setOnViewProfile(user -> {
            showProfileDialog(user);
        });

        JPanel groupPanel = createGroupPanel();

        rightPanel.add(onlineUserPanel, BorderLayout.CENTER);
        rightPanel.add(groupPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(chatPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(800);

        add(splitPane);

        // Menu bar
        setJMenuBar(createMenuBar());

        setLocationRelativeTo(null);

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
        menuBar.setBackground(ModernTheme.BACKGROUND_DARK);
        menuBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // File Menu
        JMenu fileMenu = createModernMenu("📁 File");
        JMenuItem logoutItem = createModernMenuItem("🚪 Logout");
        logoutItem.addActionListener(e -> handleLogout());
        JMenuItem exitItem = createModernMenuItem("❌ Exit");
        exitItem.addActionListener(e -> System.exit(0));

        JMenuItem setStatusItem = new JMenuItem("Set Status");
        setStatusItem.addActionListener(e -> showSetStatusDialog());

        JMenuItem clearStatusItem = new JMenuItem("Clear Status");
        clearStatusItem.addActionListener(e -> clearStatus());

        fileMenu.add(setStatusItem);
        fileMenu.add(clearStatusItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Groups Menu
        JMenu groupMenu = createModernMenu("👥 Groups");
        JMenuItem createGroupItem = createModernMenuItem("➕ Create Group");
        createGroupItem.addActionListener(e -> createGroup());
        JMenuItem joinGroupItem = createModernMenuItem("🔗 Join Group");
        joinGroupItem.addActionListener(e -> joinGroup());
        JMenuItem leaveGroupItem = createModernMenuItem("🚪 Leave Group");
        leaveGroupItem.addActionListener(e -> leaveGroup());

        groupMenu.add(createGroupItem);
        groupMenu.add(joinGroupItem);
        groupMenu.addSeparator();
        groupMenu.add(leaveGroupItem);

        // Settings Menu
        JMenu settingsMenu = createModernMenu("⚙️ Settings");
        JCheckBoxMenuItem soundItem = new JCheckBoxMenuItem("🔊 Sound Enabled", true);
        soundItem.setFont(ModernTheme.FONT_BODY);
        soundItem.addActionListener(e -> SoundManager.setEnabled(soundItem.isSelected()));
        settingsMenu.add(soundItem);

        // Help Menu
        JMenu helpMenu = createModernMenu("❓ Help");
        JMenuItem aboutItem = createModernMenuItem("ℹ️ About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(groupMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void showSetStatusDialog() {
        // Predefined statuses
        String[] suggestions = {
                "Available",
                "Busy",
                "In a meeting",
                "At school",
                "At work",
                "Eating",
                "Sleeping",
                "Gaming",
                "Studying",
                "Listening to music"
        };

        JComboBox<String> suggestionBox = new JComboBox<>(suggestions);
        suggestionBox.setEditable(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Enter your status:"), BorderLayout.NORTH);
        panel.add(suggestionBox, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Set Status",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String status = (String) suggestionBox.getSelectedItem();
            if (status != null && !status.trim().isEmpty()) {
                try {
                    client.setUserStatus(client.getUsername(), status.trim());
                    SwingUtils.showInfo(this, "Status Updated", "Your status has been set to: " + status);
                } catch (RemoteException e) {
                    SwingUtils.showError(this, "Error", "Failed to set status: " + e.getMessage());
                }
            }
        }
    }

    private void clearStatus() {
        try {
            client.setUserStatus(client.getUsername(), "");
            SwingUtils.showInfo(this, "Status Cleared", "Your status has been cleared.");
        } catch (RemoteException e) {
            SwingUtils.showError(this, "Error", "Failed to clear status: " + e.getMessage());
        }
    }

    private JMenu createModernMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setFont(ModernTheme.FONT_BODY);
        menu.setForeground(ModernTheme.TEXT_PRIMARY);
        menu.setOpaque(false);

        // Hover effect
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menu.setForeground(ModernTheme.PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menu.setForeground(ModernTheme.TEXT_PRIMARY);
            }
        });

        return menu;
    }

    private JMenuItem createModernMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(ModernTheme.FONT_BODY);

        // Hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(ModernTheme.PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(ModernTheme.BACKGROUND_MEDIUM);
            }
        });

        return item;
    }

    // Add leave group method
    private void leaveGroup() {
        try {
            // Get list of groups the user is a member of
            List<Group> allGroups = client.getAvailableGroups();
            List<String> userGroups = new ArrayList<>();

            for (Group group : allGroups) {
                if (group.hasMember(client.getUsername())) {
                    userGroups.add(group.getName());
                }
            }

            if (userGroups.isEmpty()) {
                SwingUtils.showInfo(this, "No Groups", "You are not a member of any groups.");
                return;
            }

            // Show dialog to select group to leave
            String groupName = (String) JOptionPane.showInputDialog(
                    this,
                    "Select a group to leave:",
                    "Leave Group",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    userGroups.toArray(),
                    userGroups.get(0));

            if (groupName != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to leave group '" + groupName + "'?",
                        "Confirm Leave",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    client.leaveGroup(client.getUsername(), groupName);
                    SwingUtils.showInfo(this, "Success", "You have left the group: " + groupName);
                }
            }

        } catch (RemoteException e) {
            SwingUtils.showError(this, "Connection Error", "Failed to leave group: " + e.getMessage());
        } catch (GroupNotFoundException e) {
            SwingUtils.showError(this, "Error", "Group not found: " + e.getMessage());
        } catch (Exception e) {
            SwingUtils.showError(this, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

private JPanel createGroupPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(ModernTheme.BACKGROUND_LIGHT),
        "Groups",
        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        ModernTheme.FONT_HEADER,
        ModernTheme.TEXT_PRIMARY
    ));
    panel.setPreferredSize(new Dimension(200, 150));
    
    DefaultListModel<String> groupListModel = new DefaultListModel<>();
    JList<String> groupList = new JList<>(groupListModel);
    groupList.setBackground(ModernTheme.BACKGROUND_MEDIUM);
    groupList.setForeground(ModernTheme.TEXT_PRIMARY);
    groupList.setFont(ModernTheme.FONT_BODY);
    groupList.setSelectionBackground(ModernTheme.PRIMARY);
    
    // Double-click to open group
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
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showGroupContextMenu(e, groupList);
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showGroupContextMenu(e, groupList);
            }
        }
    });
    
    JScrollPane scrollPane = new JScrollPane(groupList);
    scrollPane.setBorder(null);
    scrollPane.setBackground(ModernTheme.BACKGROUND_DARK);
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

private void showGroupContextMenu(MouseEvent e, JList<String> groupList) {
    int index = groupList.locationToIndex(e.getPoint());
    if (index >= 0) {
        groupList.setSelectedIndex(index);
        String selectedGroup = groupList.getSelectedValue();
        
        if (selectedGroup != null) {
            JPopupMenu menu = createGroupContextMenu(selectedGroup);
            menu.show(groupList, e.getX(), e.getY());
        }
    }
}

private JPopupMenu createGroupContextMenu(String groupName) {
    JPopupMenu menu = new JPopupMenu();
    menu.setBackground(ModernTheme.BACKGROUND_MEDIUM);
    menu.setBorder(BorderFactory.createLineBorder(ModernTheme.BACKGROUND_LIGHT));
    
    // Open group
    JMenuItem openItem = createMenuItem("💬 Open Group", () -> {
        openGroupChat(groupName);
    });
    menu.add(openItem);
    
    // View members
    JMenuItem membersItem = createMenuItem("👥 View Members", () -> {
        showGroupMembers(groupName);
    });
    menu.add(membersItem);
    
    menu.addSeparator();
    
    // Check if user is a member
    try {
        java.util.List<String> members = client.getGroupMembers(groupName);
        boolean isMember = members.contains(client.getUsername());
        
        if (!isMember) {
            // Join group option
            JMenuItem joinItem = createMenuItem("➕ Join Group", () -> {
                joinGroup(groupName);
            });
            menu.add(joinItem);
        } else {
            // Leave group option (only if not creator, or always allow)
            JMenuItem leaveItem = createMenuItem("🚪 Leave Group", () -> {
                leaveGroup(groupName);
            });
            menu.add(leaveItem);
        }
    } catch (Exception ex) {
        // Ignore
    }
    
    menu.addSeparator();
    
    // Copy group name
    JMenuItem copyItem = createMenuItem("📋 Copy Group Name", () -> {
        ClipboardHelper.copyToClipboard(groupName);
        ContextMenuFactory.showToast(this, "Group name copied!");
    });
    menu.add(copyItem);
    
    return menu;
}

private JMenuItem createMenuItem(String text, Runnable action) {
    JMenuItem item = new JMenuItem(text);
    item.setFont(ModernTheme.FONT_BODY);
    item.setBackground(ModernTheme.BACKGROUND_MEDIUM);
    item.setForeground(ModernTheme.TEXT_PRIMARY);
    item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    
    item.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            item.setBackground(ModernTheme.PRIMARY);
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
            item.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        }
    });
    
    item.addActionListener(e -> action.run());
    
    return item;
}

private void showGroupMembers(String groupName) {
    try {
        java.util.List<String> members = client.getGroupMembers(groupName);
        StringBuilder sb = new StringBuilder();
        sb.append("Members of ").append(groupName).append(":\n\n");
        for (String member : members) {
            sb.append("• ").append(member).append("\n");
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(ModernTheme.FONT_BODY);
        textArea.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        textArea.setForeground(ModernTheme.TEXT_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Group Members", JOptionPane.INFORMATION_MESSAGE);
            
    } catch (RemoteException | GroupNotFoundException e) {
        SwingUtils.showError(this, "Error", "Failed to load members: " + e.getMessage());
    }
}

private void joinGroup(String groupName) {
    try {
        client.joinGroup(groupName);
        SwingUtils.showInfo(this, "Success", "Joined group: " + groupName);
    } catch (Exception e) {
        SwingUtils.showError(this, "Error", "Failed to join group: " + e.getMessage());
    }
}

private void leaveGroup(String groupName) {
    int confirm = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to leave '" + groupName + "'?",
        "Confirm Leave",
        JOptionPane.YES_NO_OPTION);
        
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            client.leaveGroup(client.getUsername(), groupName);
            SwingUtils.showInfo(this, "Success", "Left group: " + groupName);
        } catch (Exception e) {
            SwingUtils.showError(this, "Error", "Failed to leave group: " + e.getMessage());
        }
    }
}

    private void addMessageToChat(Message message) {
        boolean isOwnMessage = message.getSender().equals(client.getUsername());
        MessageBubble bubble = new MessageBubble(message, isOwnMessage, msg -> {
            deleteMessage(msg);
        });

        // Add reply handler
        if (!message.getSender().equals("System") && !message.getSender().equals(client.getUsername())) {
            bubble.addReplyHandler(msg -> {
                if (message.getType() == MessageType.GROUP) {
                    openGroupChatWithReply(msg);
                } else {
                    openPrivateChatWithReply(msg);
                }
            });
        }

        chatMessagesPanel.add(bubble);
        chatMessagesPanel.add(Box.createVerticalStrut(5));
        chatMessagesPanel.revalidate();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void openGroupChatWithReply(Message message) {
        String groupName = message.getRecipient();

        // Open group chat window
        GroupChatFrame chatFrame = new GroupChatFrame(client, groupName);
        chatFrame.setVisible(true);

        // Start the reply
        chatFrame.startReply(message);
    }

    private void openPrivateChatWithReply(Message message) {
        String sender = message.getSender();

        // Open or get existing private chat
        PrivateChatFrame chatFrame = privateChats.get(sender);
        if (chatFrame == null) {
            chatFrame = new PrivateChatFrame(client, sender);
            privateChats.put(sender, chatFrame);
        }

        // Show the window
        chatFrame.setVisible(true);
        chatFrame.toFront();

        // Start the reply
        chatFrame.startReply(message);
    }

    private void deleteMessage(Message message) {
        try {
            client.deleteMessage(message.getId(), client.getUsername());

            // Remove from UI
            displayedMessageIds.remove(message.getId());
            refreshChatMessages();

        } catch (RemoteException e) {
            SwingUtils.showError(this, "Error", "Failed to delete message: " + e.getMessage());
        }
    }

    // Add refresh method:
    private void refreshChatMessages() {
        chatMessagesPanel.removeAll();
        loadMessageHistory();
    }

    // Add delete listener in setupCallbacks:

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
                List<User> allUsers = client.getAllUsers();
                SwingUtilities.invokeLater(() -> {
                    onlineUserPanel.updateUsers(allUsers);

                    // Count online users
                    long onlineCount = allUsers.stream()
                            .filter(u -> u.getStatus() == UserStatus.ONLINE)
                            .count();
                    updateOnlineCount((int) onlineCount);
                });
            } catch (RemoteException ex) {
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

                        "Habeshagram is a distributed chat application designed and developed " +
                        "as part of an Advanced Programming and Distributed Systems project.\n\n" +

                        "This application allows users to communicate in real-time through " +
                        "a client-server architecture powered by Java RMI (Remote Method Invocation). " +
                        "It demonstrates core concepts such as remote communication, object serialization, " +
                        "and concurrent client handling.\n\n" +

                        "Key Features:\n" +
                        "- Real-time messaging\n" +
                        "- Private and group chat support\n" +
                        "- User status management\n" +
                        "- Modular and scalable design\n\n" +

                        "This project reflects a strong focus on clean architecture, " +
                        "code organization using Maven, and practical implementation of " +
                        "distributed system concepts.\n\n" +

                        "Developed with dedication as part of my journey in software engineering.\n" +
                        "— Yeabtsega Tesfaye");
    }

    private void setupCallbacks() {
        // Message listener
        client.getCallbackImpl().addMessageListener(message -> {
            SwingUtilities.invokeLater(() -> {
                if (!displayedMessageIds.contains(message.getId())) {
                    displayedMessageIds.add(message.getId());
                    addMessageToChat(message);

                    if (!message.getSender().equals(client.getUsername())) {
                        SoundManager.playMessageSound();
                    }
                }
            });
        });

        // Typing listener
        client.getCallbackImpl().addTypingListener(username -> {
            if (username != null && !username.equals(client.getUsername())) {
                SwingUtilities.invokeLater(() -> {
                    currentTypingUser = username;
                    typingLabel.setText(username + " is typing...");

                    if (typingTimer != null) {
                        typingTimer.stop();
                    }
                    typingTimer = new Timer(2000, e -> {
                        typingLabel.setText(" ");
                        currentTypingUser = null;
                    });
                    typingTimer.setRepeats(false);
                    typingTimer.start();
                });
            }
        });

        client.getCallbackImpl().addStatusChangeListener(event -> {
            SwingUtilities.invokeLater(() -> {
                // Refresh user list to show updated status
                try {
                    List<User> allUsers = client.getAllUsers();
                    onlineUserPanel.updateUsers(allUsers);
                } catch (RemoteException e) {
                    // Ignore
                }

                // Show notification
                if (!event.getUsername().equals(client.getUsername())) {
                    String status = event.getNewStatus();
                    if (status != null && !status.isEmpty()) {
                        // Could show a small toast notification here
                        System.out.println(event.getUsername() + " set status to: " + status);
                    }
                }
            });
        });

        client.getCallbackImpl().addDeleteListener(messageId -> {
            SwingUtilities.invokeLater(() -> {
                if (displayedMessageIds.remove(messageId)) {
                    refreshChatMessages();
                }
            });
        });

        SwingUtilities.invokeLater(() -> {
            loadMessageHistory();
        });
    }

private void loadMessageHistory() {
    try {
        List<Message> allMessages = new ArrayList<>();
        
        // Get broadcast/system messages
        List<Message> broadcastHistory = client.getRecentMessages(client.getUsername(), HISTORY_LIMIT);
        allMessages.addAll(broadcastHistory);
        
        // Get recent private messages involving this user
        List<Message> privateHistory = client.getRecentPrivateMessages(client.getUsername(), HISTORY_LIMIT);
        allMessages.addAll(privateHistory);
        
        // Get recent group messages for groups this user is a member of
        List<Message> groupHistory = client.getRecentGroupMessages(client.getUsername(), HISTORY_LIMIT);
        allMessages.addAll(groupHistory);
        
        // Sort all messages by timestamp (oldest first)
        allMessages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        
        // Display messages in chronological order
        for (Message msg : allMessages) {
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

    private void updateOnlineCount(int count) {
        if (onlineCountLabel != null) {
            onlineCountLabel.setText(count + " online now");
        }
    }

    private void showProfileDialog(User user) {
        String status = user.getStatus() == UserStatus.ONLINE ? "🟢 Online" : "⚪ Offline";
        String lastSeen = user.getLastSeen() != null
                ? "Last seen: "
                        + user.getLastSeen().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "";

        String message = String.format(
                "Username: %s\nStatus: %s\n%s\nJoined: %s",
                user.getUsername(),
                status,
                lastSeen,
                user.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        JOptionPane.showMessageDialog(this, message, "User Profile", JOptionPane.INFORMATION_MESSAGE);
    }

}