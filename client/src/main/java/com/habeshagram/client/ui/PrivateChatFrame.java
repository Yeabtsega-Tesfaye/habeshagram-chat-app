package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.common.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.HashSet;
import java.util.List;

import com.habeshagram.client.ui.components.*;
import com.habeshagram.client.ui.theme.ModernTheme;
import java.awt.geom.RoundRectangle2D;

import com.habeshagram.client.util.*;

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
    private TypingDots typingDots;
    private Timer typingTimeoutTimer;
    private Timer typingTimer;
    private Boolean isTyping = false;
    private Message replyingTo = null;
    private ReplyIndicator replyIndicator;
    private JPanel inputContainer;
    private Consumer<Message> onReplyCallback;
    private boolean hasUnreadMessages = false;
    private NewMessageDivider unreadDivider;
    private boolean isFirstShow = true;

    public PrivateChatFrame(ChatClient client, String recipient) {
        this.client = client;
        this.recipient = recipient;
        this.hasFocus = true;

        initializeUI();
        setupCallback();

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

    /**
     * Show window with slide-in animation
     */
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
        setTitle("Private Chat - " + recipient);
        setAppIcon();
        setSize(600, 800);
        getContentPane().setBackground(ModernTheme.BACKGROUND_DARK);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND_DARK);

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Messages area
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(ModernTheme.BACKGROUND_CHAT);
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messagesPanel.add(Box.createVerticalGlue());

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
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        typingDots = new TypingDots(recipient);
        typingDots.setVisible(false);
        typingDots.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

        // Create input container (holds reply indicator + typing label + input panel)
        inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBackground(ModernTheme.BACKGROUND_DARK);

        // Create input panel
        JPanel inputPanel = createInputPanel();

        // Add components to input container in correct order
        // Reply indicator will be added here dynamically when replying
        inputContainer.add(typingDots, BorderLayout.NORTH);
        inputContainer.add(inputPanel, BorderLayout.SOUTH);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputContainer, BorderLayout.SOUTH); // FIXED: Add inputContainer, not inputPanel

        add(mainPanel);
        setLocationRelativeTo(null);

        // Add Escape key listener to cancel reply
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && replyingTo != null) {
                    cancelReply();
                }
            }
        });

        // Add typing indicator key listener
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isTyping) {
                    isTyping = true;
                    try {
                        client.sendTypingIndicator(client.getUsername(), recipient);
                    } catch (RemoteException ex) {
                        // Ignore
                    }

                    // Reset typing flag after 1 second
                    new Timer(1000, evt -> isTyping = false).start();
                }
            }
        });

        // Add focus listener to mark messages as read
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                hasFocus = true;
                if (hasUnreadMessages) {
                    hasUnreadMessages = false;
                    removeUnreadDivider();
                }

                // Mark messages from this recipient as READ
                try {
                    client.markPrivateMessagesAsRead(client.getUsername(), recipient);
                } catch (RemoteException ex) {
                    // Ignore
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
        // NOTE: Uses messagesPanel (not chatMessagesPanel)
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

        return inputPanel; // Return just the input panel, not a wrapper
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
                    String status = user.getStatus() == com.habeshagram.common.model.UserStatus.ONLINE ? "● Online"
                            : "○ Offline";
                    JLabel statusLabel = new JLabel(status);
                    statusLabel.setFont(ModernTheme.FONT_SMALL);
                    statusLabel.setForeground(
                            user.getStatus() == com.habeshagram.common.model.UserStatus.ONLINE ? ModernTheme.ONLINE
                                    : ModernTheme.OFFLINE);
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

                    if (!hasFocus && !message.getSender().equals(client.getUsername())) {
                        hasUnreadMessages = true;
                        String title = message.getSender();
                        String content = message.getContent();
                        if (content.length() > 50) {
                            content = content.substring(0, 47) + "...";
                        }
                        ToastNotification.show(PrivateChatFrame.this, title, content, message.getType());
                    }
                    addMessageToUI(message);

                    // bounce & sound
                    Component lastAdded = messagesPanel.getComponent(messagesPanel.getComponentCount() - 2);
                    if (lastAdded instanceof MessageBubble) {
                        AnimationUtils.bounce((MessageBubble) lastAdded);
                    }
                    if (!hasFocus && message.getSender().equals(recipient)) {
                        unreadCount++;
                        updateTitle();
                        SoundManager.playMessageSound();
                    }
                }
            });
        }
    });

    // Typing listener
    client.getCallbackImpl().addTypingListener(username -> {
        if (username.equals(recipient)) {
            SwingUtilities.invokeLater(() -> {
                typingDots.setVisible(true);
                typingDots.startAnimation();
                if (typingTimeoutTimer != null) typingTimeoutTimer.stop();
                typingTimeoutTimer = new Timer(2500, e -> {
                    typingDots.stopAnimation();
                    typingDots.setVisible(false);
                });
                typingTimeoutTimer.setRepeats(false);
                typingTimeoutTimer.start();
            });
        }
    });

    // Delete listener
    client.getCallbackImpl().addDeleteListener(messageId -> {
        SwingUtilities.invokeLater(() -> {
            if (displayedMessageIds.remove(messageId)) {
                refreshChatMessages();
            }
        });
    });

    // READ LISTENER: do NOT call refreshMessagesStatus or loadConversationHistory.
    // Instead, just mark the messages as read on the spot.
    client.getCallbackImpl().addReadListener(reader -> {
        if (reader.equals(recipient)) {
            SwingUtilities.invokeLater(() -> {
                for (Component comp : messagesPanel.getComponents()) {
                    if (comp instanceof MessageBubble bubble) {
                        Message msg = bubble.getMessage();
                        if (msg != null
                                && msg.getType() == MessageType.PRIVATE
                                && msg.getSender().equals(client.getUsername())
                                && msg.getStatus() != Message.MessageStatus.READ) {
                            bubble.updateStatus(Message.MessageStatus.READ);
                        }
                    }
                }
                messagesPanel.repaint();
            });
        }
    });

    // Load history once, after a tiny delay to ensure UI is ready
    new Timer(300, e -> {
        loadConversationHistory();
        ((Timer) e.getSource()).stop();
    }).start();
}

private void loadConversationHistory() {
    try {
        List<Message> history = client.getPrivateHistory(client.getUsername(), recipient, HISTORY_LIMIT);
        for (Message msg : history) {
            if (!displayedMessageIds.contains(msg.getId())) {
                displayedMessageIds.add(msg.getId());
                addMessageToUI(msg);
            }
        }
        updatePlaceholder();

        // After adding all messages, force-update the status icons from the loaded status
        SwingUtilities.invokeLater(() -> {
            for (Component comp : messagesPanel.getComponents()) {
                if (comp instanceof MessageBubble bubble) {
                    Message msg = bubble.getMessage();
                    if (msg != null && msg.getType() == MessageType.PRIVATE) {
                        bubble.updateStatus(msg.getStatus());
                    }
                }
            }
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    } catch (RemoteException e) {
        System.err.println("Failed to load conversation history: " + e.getMessage());
    }
}


    private void refreshMessagesStatus() {
    // Reload conversation to get updated status
    displayedMessageIds.clear();
    messagesPanel.removeAll();
    messagesPanel.add(Box.createVerticalGlue());
    messagesPanel.add(placeholderLabel);
    loadConversationHistory();
}

    private void updateTitle() {
        if (unreadCount > 0) {
            setTitle("Private Chat - " + recipient + " (" + unreadCount + ")");
        } else {
            setTitle("Private Chat - " + recipient);
        }
    }
    
    private void addMessageToUI(Message message) {
        placeholderLabel.setVisible(false);

        boolean isOwnMessage = message.getSender().equals(client.getUsername());
        MessageBubble bubble = new MessageBubble(message, isOwnMessage, msg -> {
            deleteMessage(msg);
        });

        bubble.addReplyHandler(msg -> {
            startReply(msg);
        });

        if (!hasFocus && hasUnreadMessages && unreadDivider == null &&
                message.getSender().equals(recipient)) {
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

            if (!isOwnMessage && message.getSender().equals(recipient)) {
                Timer bounceTimer = new Timer(250, e -> {
                    AnimationUtils.bounce(bubble);
                    ((Timer) e.getSource()).stop();
                });
                bounceTimer.setRepeats(false);
                bounceTimer.start();
            }

            // Auto-scroll
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            });
        });
    }

    private void fadeInMessage(JComponent component) {
        component.setOpaque(false);

        Timer timer = new Timer(20, null);
        final float[] alpha = { 0.0f };

        timer.addActionListener(e -> {
            alpha[0] += 0.05f;
            if (alpha[0] >= 1.0f) {
                alpha[0] = 1.0f;
                ((Timer) e.getSource()).stop();
                component.setOpaque(true);
            }

            // Apply alpha to all child components
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

    private void updatePlaceholder() {
        placeholderLabel.setVisible(displayedMessageIds.isEmpty());
    }

    private void sendMessage() {
        String content = inputField.getText().trim();
        if (!content.isEmpty()) {
            try {
                if (replyingTo != null) {
                    client.sendPrivateReply(recipient, content,
                            replyingTo.getId(),
                            replyingTo.getSender(),
                            replyingTo.getContent());
                    cancelReply();
                } else {
                    client.sendPrivate(recipient, content);
                }
                inputField.setText("");
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to send message: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
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
        loadConversationHistory();
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    public void startReply(Message message) {
        this.replyingTo = message;

        // Remove old indicator if exists
        if (replyIndicator != null) {
            inputContainer.remove(replyIndicator);
        }

        // Create new indicator
        replyIndicator = new ReplyIndicator(message, this::cancelReply);
        inputContainer.add(replyIndicator, BorderLayout.NORTH);
        inputContainer.revalidate();
        inputContainer.repaint();

        // Change placeholder
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

    private void playNotificationSound() {
        Toolkit.getDefaultToolkit().beep();
    }

    private void setAppIcon() {
        try {
            java.net.URL iconURL = getClass().getClassLoader().getResource("images/logo-32.png");
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