package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;

import javax.swing.*;
import java.awt.*;

public class NewMessageDivider extends JPanel {
    
    public NewMessageDivider() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JPanel linePanel = new JPanel();
        linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.X_AXIS));
        linePanel.setOpaque(false);
        linePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Left line
        JPanel leftLine = new JPanel();
        leftLine.setBackground(ModernTheme.ERROR);
        leftLine.setPreferredSize(new Dimension(40, 2));
        leftLine.setMaximumSize(new Dimension(40, 2));
        
        // Label
        JLabel label = new JLabel(" NEW MESSAGES ");
        label.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
        label.setForeground(ModernTheme.ERROR);
        label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        
        // Right line
        JPanel rightLine = new JPanel();
        rightLine.setBackground(ModernTheme.ERROR);
        rightLine.setPreferredSize(new Dimension(40, 2));
        rightLine.setMaximumSize(new Dimension(40, 2));
        
        linePanel.add(Box.createHorizontalGlue());
        linePanel.add(leftLine);
        linePanel.add(label);
        linePanel.add(rightLine);
        linePanel.add(Box.createHorizontalGlue());
        
        add(Box.createVerticalStrut(5));
        add(linePanel);
        add(Box.createVerticalStrut(5));
    }
}