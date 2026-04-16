package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.common.model.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ToastNotification extends JWindow {
    private static final int TOAST_DURATION = 3000; // 3 seconds
    private static ToastNotification currentToast = null;
    
    public static void show(Component parent, String title, String message, MessageType type) {
        // Close existing toast
        if (currentToast != null) {
            currentToast.dispose();
        }
        
        SwingUtilities.invokeLater(() -> {
            currentToast = new ToastNotification(title, message, type);
            currentToast.showToast();
        });
    }
    
    private ToastNotification(String title, String message, MessageType type) {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));
        
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fill(new RoundRectangle2D.Double(3, 3, getWidth() - 1, getHeight() - 1, 12, 12));
                
                // Background
                g2.setColor(ModernTheme.BACKGROUND_MEDIUM);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 3, getHeight() - 3, 12, 12));
                
                // Border
                g2.setColor(ModernTheme.PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 5, getHeight() - 5, 10, 10));
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
        // Icon based on message type
        String icon = "💬";
        if (type == MessageType.PRIVATE) icon = "🔒 New Private Message";
        else if (type == MessageType.GROUP) icon = "👥 New Group Message";
        else icon = "📢 New Message";
        
        JLabel titleLabel = new JLabel(icon);
        titleLabel.setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD));
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        
        JLabel msgLabel = new JLabel("<html><b>" + title + "</b><br>" + message + "</html>");
        msgLabel.setFont(ModernTheme.FONT_SMALL);
        msgLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(msgLabel, BorderLayout.CENTER);
        
        add(panel);
        pack();
    }
    
    private void showToast() {
        // Position at bottom-right
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screen.width - getWidth() - 20, screen.height - getHeight() - 50);
        
        setVisible(true);
        
        // Auto-close timer
        Timer timer = new Timer(TOAST_DURATION, e -> {
            fadeOut();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void fadeOut() {
        Timer fadeTimer = new Timer(20, null);
        fadeTimer.addActionListener(e -> {
            float opacity = getOpacity();
            if (opacity > 0.1f) {
                setOpacity(opacity - 0.1f);
            } else {
                ((Timer) e.getSource()).stop();
                dispose();
                currentToast = null;
            }
        });
        fadeTimer.start();
    }
}