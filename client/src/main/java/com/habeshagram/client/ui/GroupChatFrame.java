package com.habeshagram.client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.EmojiPicker;
import com.habeshagram.client.ui.components.MessageBubble;
import com.habeshagram.client.ui.components.ModernButton;
import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.client.util.SoundManager;
import com.habeshagram.common.exception.GroupNotFoundException;
import com.habeshagram.common.model.Message;
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
    private int unreadCount = 0;
    private boolean hasFocus = false;
    private JLabel typingLabel;
    private Timer typingTimer;
    private boolean isTyping = false;
    
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
    splitPane.setDividerSize(1);
    
    // Messages area
    JPanel chatPanel = new JPanel(new BorderLayout());
    chatPanel.setBackground(ModernTheme.BACKGROUND_DARK);

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
    scrollPane.setBorder(null);
    scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND_CHAT);


        typingLabel = new JLabel(" ");
        typingLabel.setFont(ModernTheme.FONT_SMALL);
        typingLabel.setForeground(ModernTheme.TEXT_MUTED);
        typingLabel.setBorder(BorderFactory.createEmptyBorder(4, 16, 2, 16));
        typingLabel.setBackground(ModernTheme.BACKGROUND_DARK);
        typingLabel.setOpaque(true);
    
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

    inputField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isTyping) {
                isTyping = true;
                try {
                    client.sendGroupTypingIndicator(client.getUsername(), groupName);
                } catch (RemoteException ex) {
                    // Ignore
                }
                
                // Reset typing flag after 1 second
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
    
    ModernButton sendButton = new ModernButton("Send");
    sendButton.setPreferredSize(new Dimension(80, 40));
    sendButton.addActionListener(e -> sendMessage());
    
    inputPanel.add(leftInputPanel, BorderLayout.CENTER);
    inputPanel.add(sendButton, BorderLayout.EAST);
    
    // Bottom panel
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setBackground(ModernTheme.BACKGROUND_DARK);
    bottomPanel.add(typingLabel, BorderLayout.NORTH);
    bottomPanel.add(inputPanel, BorderLayout.CENTER);
    
    chatPanel.add(scrollPane, BorderLayout.CENTER);
    chatPanel.add(bottomPanel, BorderLayout.SOUTH);
    
    // Members area
   JPanel membersPanel = new JPanel(new BorderLayout());
    membersPanel.setBackground(ModernTheme.BACKGROUND_DARK);
    membersPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(ModernTheme.BACKGROUND_LIGHT),
        "Members",
        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        ModernTheme.FONT_SMALL,
        ModernTheme.TEXT_PRIMARY
    ));
    
    membersArea = new JTextArea();
    membersArea.setEditable(false);
    membersArea.setBackground(ModernTheme.BACKGROUND_MEDIUM);
    membersArea.setForeground(ModernTheme.TEXT_PRIMARY);
    membersArea.setFont(ModernTheme.FONT_SMALL);
    JScrollPane membersScroll = new JScrollPane(membersArea);
    membersScroll.setBorder(null);
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
        if (message.getType() == MessageType.GROUP && groupName.equals(message.getRecipient())) {
            SwingUtilities.invokeLater(() -> {
                if (!displayedMessageIds.contains(message.getId())) {
                    displayedMessageIds.add(message.getId());
                    addMessageToUI(message);
                    
                    if (!message.getSender().equals(client.getUsername())) {
                        SoundManager.playMessageSound();
                    }
                }
            });
        }
    });
    
    // Typing listener for group
    client.getCallbackImpl().addTypingListener((username) -> {
        // Check if typing is for this group (you may need to track which group)
        SwingUtilities.invokeLater(() -> {
            if (!username.equals(client.getUsername())) {
                typingLabel.setText(username + " is typing...");
                
                if (typingTimer != null) {
                    typingTimer.stop();
                }
                typingTimer = new Timer(2000, e -> typingLabel.setText(" "));
                typingTimer.setRepeats(false);
                typingTimer.start();
            }
        });
    });
    
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

}