package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.common.model.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ReplyIndicator extends JPanel {
    private Message replyingTo;
    private Runnable onCancel;
    
    public ReplyIndicator(Message message, Runnable onCancel) {
        this.replyingTo = message;
        this.onCancel = onCancel;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BACKGROUND_MEDIUM);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, ModernTheme.PRIMARY),
            BorderFactory.createEmptyBorder(6, 12, 6, 8)
        ));
        
        // Left side - Reply info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel replyLabel = new JLabel("↩️ Replying to " + replyingTo.getSender());
        replyLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
        replyLabel.setForeground(ModernTheme.PRIMARY);
        infoPanel.add(replyLabel);
        
        String preview = replyingTo.getContent();
        if (preview.length() > 50) {
            preview = preview.substring(0, 47) + "...";
        }
        JLabel previewLabel = new JLabel(preview);
        previewLabel.setFont(ModernTheme.FONT_SMALL);
        previewLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        infoPanel.add(previewLabel);
        
        add(infoPanel, BorderLayout.CENTER);
        
        // Right side - Cancel button
        JLabel cancelButton = new JLabel("✕");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setForeground(ModernTheme.TEXT_MUTED);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));
        cancelButton.setToolTipText("Cancel reply (Esc)");
        
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onCancel.run();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setForeground(ModernTheme.ERROR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setForeground(ModernTheme.TEXT_MUTED);
            }
        });
        
        add(cancelButton, BorderLayout.EAST);
    }
    
    public Message getReplyingTo() {
        return replyingTo;
    }
}