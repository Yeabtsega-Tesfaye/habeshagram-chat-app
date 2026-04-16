package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.client.util.ClipboardHelper;
import com.habeshagram.common.model.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class ContextMenuFactory {
    
    public enum MessageAction {
        COPY, DELETE, REPLY
    }
    
    public enum UserAction {
        MESSAGE, PROFILE, COPY_USERNAME
    }
    
    public enum ChatAction {
        PASTE
    }
    
    // Create menu for message bubble
    public static JPopupMenu createMessageMenu(Message message, boolean isOwnMessage, 
                                               Consumer<MessageAction> actionHandler) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        menu.setBorder(BorderFactory.createLineBorder(ModernTheme.BACKGROUND_LIGHT));
        
        // Copy option
        JMenuItem copyItem = createMenuItem("📋 Copy Text", () -> {
            ClipboardHelper.copyToClipboard(message.getContent());
            actionHandler.accept(MessageAction.COPY);
        });
        menu.add(copyItem);
        
        // Reply option - MOVED ABOVE DELETE
        if (!message.getSender().equals("System")) {
            JMenuItem replyItem = createMenuItem("↩️ Reply", () -> {
                actionHandler.accept(MessageAction.REPLY);
            });
            menu.add(replyItem);
        }
        
        if (isOwnMessage) {
            menu.addSeparator();
            
            // Delete option (only for own messages)
            JMenuItem deleteItem = createMenuItem("🗑️ Delete Message", () -> {
                actionHandler.accept(MessageAction.DELETE);
            });
            menu.add(deleteItem);
        }
        
        return menu;
    }
    
    // Create menu for user in list
    public static JPopupMenu createUserMenu(String username, boolean isOnline,
                                            Consumer<UserAction> actionHandler) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        menu.setBorder(BorderFactory.createLineBorder(ModernTheme.BACKGROUND_LIGHT));
        
        // Message option
        JMenuItem messageItem = createMenuItem("💬 Message", () -> {
            actionHandler.accept(UserAction.MESSAGE);
        });
        menu.add(messageItem);
        
        // Profile option
        JMenuItem profileItem = createMenuItem("👤 View Profile", () -> {
            actionHandler.accept(UserAction.PROFILE);
        });
        menu.add(profileItem);
        
        menu.addSeparator();
        
        // Copy username
        JMenuItem copyUserItem = createMenuItem("📋 Copy Username", () -> {
            ClipboardHelper.copyToClipboard(username);
            actionHandler.accept(UserAction.COPY_USERNAME);
        });
        menu.add(copyUserItem);
        
        return menu;
    }
    
    // Create menu for empty chat area
    public static JPopupMenu createChatMenu(Consumer<ChatAction> actionHandler) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        menu.setBorder(BorderFactory.createLineBorder(ModernTheme.BACKGROUND_LIGHT));
        
        // Paste option
        JMenuItem pasteItem = createMenuItem("📋 Paste", () -> {
            actionHandler.accept(ChatAction.PASTE);
        });
        menu.add(pasteItem);
        
        return menu;
    }
    
    // Helper to create styled menu item
    private static JMenuItem createMenuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(ModernTheme.FONT_BODY);
        item.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        item.setForeground(ModernTheme.TEXT_PRIMARY);
        item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
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
        
        item.addActionListener(e -> action.run());
        
        return item;
    }
    
    // Show temporary tooltip
    public static void showToast(Component parent, String message) {
        JWindow toast = new JWindow();
        toast.setLayout(new BorderLayout());
        
        JLabel label = new JLabel(message);
        label.setFont(ModernTheme.FONT_SMALL);
        label.setForeground(ModernTheme.TEXT_PRIMARY);
        label.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        toast.add(label);
        toast.pack();
        
        Point location = parent.getLocationOnScreen();
        toast.setLocation(
            location.x + (parent.getWidth() - toast.getWidth()) / 2,
            location.y + parent.getHeight() - toast.getHeight() - 50
        );
        
        toast.setVisible(true);
        
        // Auto-hide after 1.5 seconds
        new Timer(1500, e -> toast.dispose()).start();
    }
}