package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LogoPanel extends JPanel {
    
    public LogoPanel() {
        setOpaque(false);
        setLayout(new GridBagLayout()); // Better centering than BoxLayout
        
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // Try to load custom logo
        java.net.URL logoURL = getClass().getClassLoader().getResource("images/logo-128.png");
        
        if (logoURL != null) {
            ImageIcon originalIcon = new ImageIcon(logoURL);
            Image originalImage = originalIcon.getImage();
            
            JLabel logoLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    // Create rounded clip
                    RoundRectangle2D roundedRect = new RoundRectangle2D.Double(0, 0, 100, 100, 14, 14);
                    g2.setClip(roundedRect);
                    
                    // Draw the image
                    g2.drawImage(originalImage, 0, 0, 100, 100, null);
                    
                    g2.dispose();
                }
                
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(100, 100);
                }
                
                @Override
                public Dimension getMinimumSize() {
                    return new Dimension(100, 100);
                }
                
                @Override
                public Dimension getMaximumSize() {
                    return new Dimension(100, 100);
                }
            };
            
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(logoLabel);
        } else {
            // Fallback: Text logo with emoji
            JPanel fallbackPanel = new JPanel();
            fallbackPanel.setOpaque(false);
            fallbackPanel.setLayout(new BoxLayout(fallbackPanel, BoxLayout.Y_AXIS));
            
            JLabel emojiLabel = new JLabel("💬");
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
            emojiLabel.setForeground(ModernTheme.PRIMARY);
            emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            fallbackPanel.add(emojiLabel);
            
            fallbackPanel.add(Box.createVerticalStrut(5));
            
            JLabel appName = new JLabel("HABESHAGRAM");
            appName.setFont(new Font("Segoe UI", Font.BOLD, 28));
            appName.setForeground(ModernTheme.PRIMARY);
            appName.setAlignmentX(Component.CENTER_ALIGNMENT);
            fallbackPanel.add(appName);
            
            contentPanel.add(fallbackPanel);
        }
        
        contentPanel.add(Box.createVerticalStrut(15));
        
        JLabel subtitle = new JLabel("Stay connected the habesha way!");
        subtitle.setFont(ModernTheme.FONT_SMALL);
        subtitle.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitle);
        
        // Add slight left padding to shift everything right
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 0));
        
        add(contentPanel);
    }
}