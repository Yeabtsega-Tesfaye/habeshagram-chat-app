package com.habeshagram.client.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.EmojiPicker;
import com.habeshagram.client.ui.components.MessageBubble;
import com.habeshagram.client.ui.components.ModernButton;
import com.habeshagram.client.ui.components.NewMessageDivider;
import com.habeshagram.client.ui.components.ReplyIndicator;
import com.habeshagram.client.ui.components.ToastNotification;
import com.habeshagram.client.ui.components.TypingDots;
import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.client.util.AnimationUtils;
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
    private TypingDots typingDots;
    private Timer typingTimeoutTimer;
    private Timer typingTimer;
    private boolean isTyping = false;
    private Message replyingTo = null;
    private ReplyIndicator replyIndicator;
    private JPanel inputContainer;
private boolean hasUnreadMessages = false;
private NewMessageDivider unreadDivider;
private boolean isFirstShow = true;

    public GroupChatFrame(ChatClient client, String groupName) {
        this.client = client;
        this.groupName = groupName;
        this.hasFocus = true;

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

    public void showWithAnimation() {
        if (isFirstShow || !isVisible()) {
            AnimationUtils.slideInFromRight(this);
            isFirstShow = false;
        } else {
            setVisible(true);
            toFront();
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }

private void initializeUI() {
    ModernTheme.applyTheme();
    setTitle("Group Chat - " + groupName);
    setAppIcon();
    setSize(900, 900);
    getContentPane().setBackground(ModernTheme.BACKGROUND_DARK);
    
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setBackground(ModernTheme.BACKGROUND_DARK);
    
    // Chat panel (left side)
    JPanel chatPanel = new JPanel(new BorderLayout());
    chatPanel.setBackground(ModernTheme.BACKGROUND_DARK);
    
    // Header
    JPanel headerPanel = createHeaderPanel();
    chatPanel.add(headerPanel, BorderLayout.NORTH);
    
    // Messages area
    messagesPanel = new JPanel();
    messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
    messagesPanel.setBackground(ModernTheme.BACKGROUND_CHAT);
    messagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    messagesPanel.add(Box.createVerticalGlue());
    
    placeholderLabel = new JLabel("No messages in this group yet. Start the conversation!");
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
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
   
    typingDots = new TypingDots("");
    typingDots.setVisible(false);
    typingDots.setBorder(BorderFactory.createEmptyBorder(4, 16, 4,16));


    // Input container
    inputContainer = new JPanel(new BorderLayout());
    inputContainer.setBackground(ModernTheme.BACKGROUND_DARK);
    
    // Input panel
    JPanel inputPanel = createInputPanel();
    
    inputContainer.add(typingDots, BorderLayout.NORTH);
    inputContainer.add(inputPanel, BorderLayout.SOUTH);
    
    chatPanel.add(scrollPane, BorderLayout.CENTER);
    chatPanel.add(inputContainer, BorderLayout.SOUTH);
    
    // Members panel (right side)
    JPanel membersPanel = new JPanel(new BorderLayout());
    membersPanel.setBackground(ModernTheme.BACKGROUND_DARK);
    membersPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(ModernTheme.BACKGROUND_LIGHT),
        "Members",
        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
        javax.swing.border.TitledBorder.DEFAULT_POSITION,
        ModernTheme.FONT_HEADER,
        ModernTheme.TEXT_PRIMARY
    ));
    
    membersArea = new JTextArea();
    membersArea.setEditable(false);
    membersArea.setBackground(ModernTheme.BACKGROUND_MEDIUM);
    membersArea.setForeground(ModernTheme.TEXT_PRIMARY);
    membersArea.setFont(ModernTheme.FONT_BODY);
    JScrollPane membersScroll = new JScrollPane(membersArea);
    membersScroll.setBorder(null);
    membersPanel.add(membersScroll, BorderLayout.CENTER);
    
    splitPane.setLeftComponent(chatPanel);
    splitPane.setRightComponent(membersPanel);
    splitPane.setDividerLocation(500);
    
    add(splitPane);
    setLocationRelativeTo(null);
    
    // Escape key to cancel reply
    inputField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && replyingTo != null) {
                cancelReply();
            }
        }
    });
    
    // Typing indicator key listener
inputField.addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
        // Skip for special keys
        if (e.getKeyCode() == KeyEvent.VK_ENTER || 
            e.getKeyCode() == KeyEvent.VK_ESCAPE ||
            e.getKeyCode() == KeyEvent.VK_SHIFT ||
            e.getKeyCode() == KeyEvent.VK_CONTROL ||
            e.getKeyCode() == KeyEvent.VK_ALT) {
            return;
        }
        
        if (!isTyping) {
            isTyping = true;
            try {
                client.sendGroupTypingIndicator(client.getUsername(), groupName);
            } catch (RemoteException ex) {
                // Ignore
            }
            
            // Reset after 1 second
            Timer timer = new Timer(1000, evt -> isTyping = false);
            timer.setRepeats(false);
            timer.start();
        }
    }
});

addWindowFocusListener(new WindowAdapter() {
    @Override
    public void windowGainedFocus(WindowEvent e) {
        hasFocus = true;
        if (hasUnreadMessages) {
            hasUnreadMessages = false;
            removeUnreadDivider();
        }
    }
    
    @Override
    public void windowLostFocus(WindowEvent e) {
        hasFocus = false;
    }
});
    
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            dispose();
        }
    });
}

private void addUnreadDivider() {
    if (unreadDivider == null) {
        unreadDivider = new NewMessageDivider();
    }
    // NOTE: Uses messagesPanel
    messagesPanel.add(unreadDivider, messagesPanel.getComponentCount() - 1);
    messagesPanel.revalidate();
    messagesPanel.repaint();
}

private void removeUnreadDivider() {
    if (unreadDivider != null && unreadDivider.getParent() != null) {
        messagesPanel.remove(unreadDivider);
        messagesPanel.revalidate();
        messagesPanel.repaint();
        unreadDivider = null;
    }
}


private JPanel createInputPanel() {
    JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
    inputPanel.setBackground(ModernTheme.BACKGROUND_DARK);
    inputPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    
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
    
    leftInputPanel.add(emojiButton, BorderLayout.WEST);
    leftInputPanel.add(inputField, BorderLayout.CENTER);
    
    ModernButton sendButton = new ModernButton("Send");
    sendButton.setPreferredSize(new Dimension(80, 40));
    sendButton.addActionListener(e -> sendMessage());

    inputPanel.add(leftInputPanel, BorderLayout.CENTER);
    inputPanel.add(sendButton, BorderLayout.EAST);
    
    return inputPanel;
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

                                        if (!hasFocus && !message.getSender().equals(client.getUsername())) {
                    hasUnreadMessages = true;
                    
                    // Show toast notification
                    String title = message.getSender();
                    String content = message.getContent();
                    if (content.length() > 50) {
                        content = content.substring(0, 47) + "...";
                    }
                    ToastNotification.show(GroupChatFrame.this, title, content, message.getType());
                }
                
                        addMessageToUI(message);

                                      Component lastAdded = messagesPanel.getComponent(messagesPanel.getComponentCount() - 2);
            if (lastAdded instanceof MessageBubble) {
                AnimationUtils.bounce((MessageBubble) lastAdded);
            }

                        if (!message.getSender().equals(client.getUsername())) {
                            SoundManager.playMessageSound();
                        }
                    }
                });
            }
        });
    
// Add typing listener in setupCallback():
client.getCallbackImpl().addTypingListener(username -> {
    SwingUtilities.invokeLater(() -> {
        typingDots.updateUsername(username);
        typingDots.setVisible(true);
        typingDots.startAnimation();
        
        if (typingTimeoutTimer != null) {
            typingTimeoutTimer.stop();
        }
        
        typingTimeoutTimer = new Timer(2500, e -> {
            typingDots.stopAnimation();
            typingDots.setVisible(false);
        });
        typingTimeoutTimer.setRepeats(false);
        typingTimeoutTimer.start();
    });
});



        // In setupCallback() method, add:
        client.getCallbackImpl().addDeleteListener(messageId -> {
            SwingUtilities.invokeLater(() -> {
                if (displayedMessageIds.remove(messageId)) {
                    refreshChatMessages();
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
            if (replyingTo != null) {
                client.sendGroupReply(groupName, content,
                    replyingTo.getId(),
                    replyingTo.getSender(),
                    replyingTo.getContent());
                cancelReply();
            } else {
                client.sendGroup(groupName, content);
            }
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

                SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });

        } catch (RemoteException e) {
            System.err.println("Failed to load group history: " + e.getMessage());
        }
    }

private void addMessageToUI(Message message) {
    placeholderLabel.setVisible(false);
    
    boolean isOwnMessage = message.getSender().equals(client.getUsername());
    MessageBubble bubble = new MessageBubble(message, isOwnMessage, msg -> {
        deleteMessage(msg);
    });
    
    // Add reply handler
    bubble.addReplyHandler(msg -> {
        startReply(msg);
    });

        if (!hasFocus && hasUnreadMessages && unreadDivider == null && 
        !message.getSender().equals(client.getUsername())) {
        addUnreadDivider();
    }

    bubble.setVisible(false);
    
    int insertPosition = messagesPanel.getComponentCount() - 1;
    messagesPanel.add(bubble, insertPosition);
    messagesPanel.add(Box.createVerticalStrut(5), insertPosition + 1);
    
    messagesPanel.revalidate();
    messagesPanel.repaint();
    
    SwingUtilities.invokeLater(() -> {
        bubble.setVisible(true);
        fadeInMessage(bubble);

    if (!isOwnMessage) {
        Timer bounceTimer = new Timer(250, e -> {
            AnimationUtils.bounce(bubble);
            ((Timer) e.getSource()).stop();
        });
        bounceTimer.setRepeats(false);
        bounceTimer.start();
    }

        SwingUtilities.invokeLater(() -> {

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    });

});
  }

  private void fadeInMessage(JComponent component) {
    component.setOpaque(false);
    
    Timer timer = new Timer(16, null);
    final float[] alpha = {0.0f};
    
    timer.addActionListener(e -> {
        alpha[0] += 0.08f;
        if (alpha[0] >= 1.0f) {
            alpha[0] = 1.0f;
            ((Timer) e.getSource()).stop();
            component.setOpaque(true);
        }
        
        applyAlpha(component, alpha[0]);
        component.repaint();
    });
    
    timer.start();
}

private void applyAlpha(Component comp, float alpha) {
    if (comp instanceof JComponent) {
        ((JComponent) comp).putClientProperty("fadeOpacity", alpha);
    }
    if (comp instanceof Container) {
        for (Component child : ((Container) comp).getComponents()) {
            applyAlpha(child, alpha);
        }
    }
}

    private void deleteMessage(Message message) {
        try {
            client.deleteMessage(message.getId(), client.getUsername());
            displayedMessageIds.remove(message.getId());
            refreshChatMessages();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to delete message: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshChatMessages() {
        messagesPanel.removeAll();
        messagesPanel.add(Box.createVerticalGlue());
        messagesPanel.add(placeholderLabel);
        displayedMessageIds.clear();
        loadGroupHistory();
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    // Update placeholder visibility
    private void updatePlaceholder() {
        placeholderLabel.setVisible(displayedMessageIds.isEmpty());
    }

    public void startReply(Message message) {
    this.replyingTo = message;
    
    if (replyIndicator != null) {
        inputContainer.remove(replyIndicator);
    }
    
    replyIndicator = new ReplyIndicator(message, this::cancelReply);
    inputContainer.add(replyIndicator, BorderLayout.CENTER);
    inputContainer.revalidate();
    inputContainer.repaint();
    
    inputField.putClientProperty("JTextField.placeholderText", "Type your reply...");
    inputField.requestFocus();
}

private void cancelReply() {
    this.replyingTo = null;
    
    if (replyIndicator != null) {
        inputContainer.remove(replyIndicator);
        replyIndicator = null;
        inputContainer.revalidate();
        inputContainer.repaint();
    }
    
    inputField.putClientProperty("JTextField.placeholderText", "Type a message...");
    inputField.repaint();
}

private void setAppIcon() {
    try {
        java.net.URL iconURL = getClass().getClassLoader().getResource("logo-32.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        } else {
            // Fallback: Use emoji/text icon
            System.out.println("Logo not found, using default");
        }
    } catch (Exception e) {
        System.err.println("Could not load app icon: " + e.getMessage());
    }
}

}