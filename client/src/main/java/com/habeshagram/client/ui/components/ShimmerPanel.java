package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ShimmerPanel extends JPanel {
    private Timer shimmerTimer;
    private float shimmerPosition = -1.0f;
    
    public ShimmerPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add placeholder message bubbles
        add(createPlaceholderBubble(true, 200));
        add(Box.createVerticalStrut(8));
        add(createPlaceholderBubble(false, 250));
        add(Box.createVerticalStrut(8));
        add(createPlaceholderBubble(true, 180));
        add(Box.createVerticalStrut(8));
        add(createPlaceholderBubble(false, 220));
        
        // Start shimmer animation
        shimmerTimer = new Timer(40, e -> {
            shimmerPosition += 0.04f;
            if (shimmerPosition > 2.0f) {
                shimmerPosition = -1.0f;
            }
            repaint();
        });
    }
    
    private JPanel createPlaceholderBubble(boolean isRight, int width) {
        JPanel wrapper = new JPanel(new FlowLayout(isRight ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2.setColor(isRight ? ModernTheme.BUBBLE_SENT : ModernTheme.BUBBLE_RECEIVED);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                
                // Shimmer overlay
                if (shimmerPosition >= -1.0f) {
                    float shimmerX = shimmerPosition * (getWidth() + 200) - 100;
                    GradientPaint shimmer = new GradientPaint(
                        shimmerX, 0,
                        new Color(255, 255, 255, 0),
                        shimmerX + 100, 0,
                        new Color(255, 255, 255, 40)
                    );
                    g2.setPaint(shimmer);
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                }
                
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setPreferredSize(new Dimension(width, 40));
        
        wrapper.add(bubble);
        return wrapper;
    }
    
    public void startShimmer() {
        shimmerTimer.start();
    }
    
    public void stopShimmer() {
        shimmerTimer.stop();
    }
}