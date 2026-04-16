package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;

import javax.swing.*;
import java.awt.*;

public class ReplyBubble extends JPanel {
    
    public ReplyBubble(String sender, String content) {
        setupUI(sender, content);
    }
    
    private void setupUI(String sender, String content) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        
        JPanel quotePanel = new JPanel();
        quotePanel.setLayout(new BoxLayout(quotePanel, BoxLayout.Y_AXIS));
        quotePanel.setBackground(new Color(0, 0, 0, 30));
        quotePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, ModernTheme.PRIMARY),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        JLabel senderLabel = new JLabel("↩️ " + sender);
        senderLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
        senderLabel.setForeground(ModernTheme.PRIMARY);
        quotePanel.add(senderLabel);
        
        String preview = content;
        if (preview.length() > 80) {
            preview = preview.substring(0, 77) + "...";
        }
        JLabel contentLabel = new JLabel("<html>" + preview.replaceAll("\n", "<br>") + "</html>");
        contentLabel.setFont(ModernTheme.FONT_SMALL);
        contentLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        quotePanel.add(contentLabel);
        
        add(quotePanel, BorderLayout.CENTER);
    }
}