package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ModernButton extends JButton {
    private Color backgroundColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private int radius;
    private boolean isHovered = false;
    private boolean isPressed = false;
    
    public ModernButton(String text) {
        this(text, ModernTheme.PRIMARY, ModernTheme.PRIMARY_DARK, 
             ModernTheme.PRIMARY_LIGHT, ModernTheme.TEXT_PRIMARY, ModernTheme.RADIUS_MD);
    }
    
    public ModernButton(String text, Color bgColor, Color hoverColor, 
                       Color pressedColor, Color textColor, int radius) {
        super(text);
        this.backgroundColor = bgColor;
        this.hoverColor = hoverColor;
        this.pressedColor = pressedColor;
        this.textColor = textColor;
        this.radius = radius;
        
        setupButton();
    }
    
    private void setupButton() {
        setFont(ModernTheme.FONT_BODY.deriveFont(Font.BOLD));
        setForeground(textColor);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color currentBg = backgroundColor;
        if (isPressed) {
            currentBg = pressedColor;
        } else if (isHovered) {
            currentBg = hoverColor;
        }
        
        g2.setColor(currentBg);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
        
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);
        
        g2.dispose();
    }
}