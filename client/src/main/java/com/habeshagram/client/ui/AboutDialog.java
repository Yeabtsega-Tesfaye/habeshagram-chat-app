package com.habeshagram.client.ui;

import com.habeshagram.client.ui.components.ModernButton;
import com.habeshagram.client.ui.theme.ModernTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class AboutDialog extends JDialog {
    
    public AboutDialog(JFrame parent) {
        super(parent, "About Habeshagram", true);
        setUndecorated(true);
        setupUI();
    }
    
    private void setupUI() {
        // Main panel with shadow effect
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                for (int i = 0; i < 5; i++) {
                    g2.setColor(new Color(0, 0, 0, 10 - i * 2));
                    g2.fill(new RoundRectangle2D.Double(i, i, getWidth() - i * 2, getHeight() - i * 2, 20, 20));
                }
                
                // Background
                g2.setColor(ModernTheme.BACKGROUND_DARK);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                
                // Border
                g2.setColor(ModernTheme.PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));
                
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // Logo
        JPanel logoPanel = createLogoPanel();
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(logoPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // App name
        JLabel nameLabel = new JLabel("Habeshagram");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nameLabel.setForeground(ModernTheme.PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        
        // Version
        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(ModernTheme.FONT_SMALL);
        versionLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(versionLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Separator
        JSeparator separator = new JSeparator();
        separator.setForeground(ModernTheme.BACKGROUND_LIGHT);
        separator.setMaximumSize(new Dimension(300, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(separator);
        contentPanel.add(Box.createVerticalStrut(20));
        
// Description with all features
JTextPane descArea = new JTextPane();
descArea.setEditable(false);
descArea.setOpaque(false);
descArea.setForeground(ModernTheme.TEXT_PRIMARY);
descArea.setFont(ModernTheme.FONT_SMALL);
descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
descArea.setContentType("text/plain");

String description = 
    "═══════════════════════════════════════════════\n" +
    "Habeshagram is a feature-rich distributed chat application\n" +
    "developed for Advanced Programming and Distributed Systems.\n" +
    "═══════════════════════════════════════════════\n\n" +
    
    "🎯 Core Features:\n" +
    "• Real-time messaging with Java RMI\n" +
    "• User authentication with BCrypt password hashing\n" +
    "• Persistent message storage using SQLite\n" +
    "• Multi-client support with concurrent handling\n\n" +
    
    "💬 Messaging Features:\n" +
    "• Broadcast messages to all online users\n" +
    "• Private one-to-one conversations\n" +
    "• Group chat with member management\n" +
    "• Reply to messages with quoted preview\n" +
    "• Delete your own messages\n" +
    "• Copy message text to clipboard\n" +
    "• Offline message delivery when users return\n" +
    "• 50 message history per conversation\n\n" +
    
    "👤 User Experience:\n" +
    "• Online/offline status indicators\n" +
    "• Custom status messages\n" +
    "• User search and filtering\n" +
    "• Typing indicators with animated dots\n" +
    "• Unread message divider\n" +
    "• Toast notifications for new messages\n" +
    "• Sound notifications with mute option\n\n" +
    
    "🎨 Modern UI:\n" +
    "• Dark theme with Discord-inspired colors\n" +
    "• Smooth fade-in animations for messages\n" +
    "• Bounce animation on new messages\n" +
    "• Slide-in window transitions\n" +
    "• Shimmer loading effects\n" +
    "• Emoji picker with 18 common emojis\n" +
    "• Right-click context menus\n" +
    "• Message bubbles with hover timestamps\n\n" +
    
    "🔧 Technical Architecture:\n" +
    "• Client-Server RMI architecture\n" +
    "• RMI callbacks for real-time push\n" +
    "• Thread-safe ClientRegistry (ConcurrentHashMap)\n" +
    "• DAO pattern for database operations\n" +
    "• Multi-module Maven project structure\n" +
    "• Custom serializable message protocol\n\n" +
    
    "🛡️ Security & Group Management:\n" +
    "• Password hashing with BCrypt\n" +
    "• Group access control (members only)\n" +
    "• Create, join, and leave groups\n" +
    "• Group member list viewing\n" +
    "• Creator can delete groups\n\n" +
    
    "📦 Advanced Features:\n" +
    "• Right-click context menus on messages/users/groups\n" +
    "• View user profiles with last seen\n" +
    "• Copy usernames and group names\n" +
    "• Auto-scroll to new messages\n" +
    "• Smooth scrolling with unit increment\n" +
    "• Escape key to cancel replies\n" +
    "• Enter key to send messages\n\n" +
    
    "═══════════════════════════════════════\n" +
    "Built with ☕ Java 25 • 🔗 RMI • 🎨 Swing • 💾 SQLite\n" +
    "Version 1.0.0 | © 2026 Habeshagram\n" +
    "═══════════════════════════════════════\n\n" +
    
    "Developed with passion and dedication by\n" +
    "Yeabtsega Tesfaye\n" +
    "Advanced Programming and Distributed Systems Project";

descArea.setText(description);

descArea.setAlignmentX(Component.LEFT_ALIGNMENT);

descArea.setCaretPosition(0);

// Put in a scroll pane to handle long content
JScrollPane descScroll = new JScrollPane(descArea);
descScroll.setOpaque(false);
descScroll.getViewport().setOpaque(false);
descScroll.setBorder(null);
descScroll.setPreferredSize(new Dimension(450, 300));
descScroll.setAlignmentX(Component.CENTER_ALIGNMENT);
descScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
descScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
descScroll.getVerticalScrollBar().setUnitIncrement(16);

// Style the scrollbar
descScroll.getVerticalScrollBar().setBackground(ModernTheme.BACKGROUND_DARK);
descScroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
    @Override
    protected void configureScrollBarColors() {
        this.thumbColor = ModernTheme.BACKGROUND_LIGHT;
        this.trackColor = ModernTheme.BACKGROUND_DARK;
    }
    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }
    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }
    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        return button;
    }
});

contentPanel.add(descScroll);
        contentPanel.add(Box.createVerticalStrut(25));
        
        // Tech stack
        JPanel techPanel = createTechPanel();
        techPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(techPanel);
        contentPanel.add(Box.createVerticalStrut(25));
        
        // Close button
        ModernButton closeButton = new ModernButton("Close");
        closeButton.setPreferredSize(new Dimension(120, 40));
        closeButton.addActionListener(e -> dispose());
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(closeButton);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
        
        setSize(550, 650);
        setLocationRelativeTo(getParent());
        
        // Make dialog draggable
        makeDraggable(mainPanel);
    }
    
    private JPanel createLogoPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        // Try to load logo
        java.net.URL logoURL = getClass().getClassLoader().getResource("images/logo-64.png");
        if (logoURL == null) {
            logoURL = getClass().getResource("/images/logo-64.png");
        }
        
        JLabel logoLabel;
        if (logoURL != null) {
            ImageIcon originalIcon = new ImageIcon(logoURL);
            Image roundedImage = createRoundedImage(originalIcon.getImage(), 70, 70, 16);
            logoLabel = new JLabel(new ImageIcon(roundedImage));
        } else {
            logoLabel = new JLabel("💬");
            logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
            logoLabel.setForeground(ModernTheme.PRIMARY);
        }
        
        panel.add(logoLabel);
        return panel;
    }
    
    private JPanel createTechPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.setOpaque(false);
        
        String[] tech = {"☕ Java", "🔗 RMI", "🎨 Swing", "💾 SQLite"};
        for (String t : tech) {
            JLabel label = new JLabel(t);
            label.setFont(ModernTheme.FONT_SMALL);
            label.setForeground(ModernTheme.ACCENT_BLUE);
            panel.add(label);
        }
        
        return panel;
    }
    
    private Image createRoundedImage(Image original, int width, int height, int cornerRadius) {
        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = roundedImage.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Double(0, 0, width, height, cornerRadius, cornerRadius));
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        
        return roundedImage;
    }
    
    private void makeDraggable(JPanel panel) {
        final Point[] clickPoint = {null};
        
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                clickPoint[0] = e.getPoint();
            }
        });
        
        panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (clickPoint[0] != null) {
                    Point current = e.getLocationOnScreen();
                    setLocation(current.x - clickPoint[0].x, current.y - clickPoint[0].y);
                }
            }
        });
    }
}