package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;

import javax.swing.*;
import java.awt.*;

public class TypingDots extends JPanel {
    private Timer animationTimer;
    private int dotCount = 0;
    private String username;
    private JLabel nameLabel;
    private JPanel dotsPanel;

    public TypingDots(String username) {
        this.username = username;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        // Username label
        JLabel nameLabel = new JLabel(username);
        nameLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
        nameLabel.setForeground(ModernTheme.PRIMARY);
        add(nameLabel);

        // "is typing" label
        JLabel typingLabel = new JLabel("is typing");
        typingLabel.setFont(ModernTheme.FONT_SMALL);
        typingLabel.setForeground(ModernTheme.TEXT_MUTED);
        add(typingLabel);

        // Dots panel (custom painting)
        dotsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(ModernTheme.TEXT_MUTED);
                int x = 0;
                for (int i = 0; i < dotCount; i++) {
                    g2.fillOval(x, 8, 5, 5);
                    x += 10;
                }

                g2.dispose();
            }
        };
        dotsPanel.setOpaque(false);
        dotsPanel.setPreferredSize(new Dimension(35, 20));
        add(dotsPanel);

        // Animation timer
        animationTimer = new Timer(350, e -> {
            dotCount = (dotCount + 1) % 4; // 0, 1, 2, 3 dots
            dotsPanel.repaint();
        });
    }

    public void updateUsername(String newUsername) {
        this.username = newUsername;
        this.nameLabel.setText(newUsername);
    }

    public void startAnimation() {
        dotCount = 1;
        animationTimer.start();
        setVisible(true);
    }

    public void stopAnimation() {
        animationTimer.stop();
        setVisible(false);
    }

    public String getUsername() {
        return username;
    }
}