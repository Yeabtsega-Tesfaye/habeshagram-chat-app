package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.LogoPanel;
import com.habeshagram.client.ui.components.ModernButton;
import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.client.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private ChatClient client;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ModernButton loginButton;
    private ModernButton registerButton;
    
    public LoginFrame() {
        client = new ChatClient();
        ModernTheme.applyTheme();
        initializeUI();
        
        if (!client.connect()) {
            SwingUtils.showError(this, "Connection Error", 
                "Cannot connect to server. Please ensure the server is running.");
        }
    }
    
    private void initializeUI() {
        setTitle("Habeshagram");
        setAppIcon();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(ModernTheme.BACKGROUND_DARK);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        LogoPanel logoPanel = new LogoPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 30, 8);
        mainPanel.add(logoPanel, gbc);
        
        // Username
        JLabel userLabel = new JLabel("");
        userLabel.setFont(ModernTheme.FONT_SMALL);
        userLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 8, 8, 8);
        mainPanel.add(userLabel, gbc);
        
        usernameField = createModernTextField();
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);
        
        // Password
        JLabel passLabel = new JLabel("");
        passLabel.setFont(ModernTheme.FONT_SMALL);
        passLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(passLabel, gbc);
        
        passwordField = createModernPasswordField();
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(ModernTheme.BACKGROUND_DARK);
        
        loginButton = new ModernButton("Login", ModernTheme.PRIMARY, 
                                      ModernTheme.PRIMARY_DARK, ModernTheme.PRIMARY_LIGHT,
                                      ModernTheme.TEXT_PRIMARY, ModernTheme.RADIUS_MD);
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.addActionListener(e -> handleLogin());
        
        registerButton = new ModernButton("Register", ModernTheme.BACKGROUND_LIGHT,
                                         ModernTheme.BACKGROUND_MEDIUM, ModernTheme.BACKGROUND_DARK,
                                         ModernTheme.TEXT_PRIMARY, ModernTheme.RADIUS_MD);
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.addActionListener(e -> handleRegister());
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 8, 8, 8);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);

        mainPanel.requestFocusInWindow();
        
    }
    
    private JTextField createModernTextField() {
        JTextField field = new JTextField(15) {
            private boolean focused = false;
            private final String placeholder = "Enter username";
            
            @Override
            protected void paintComponent(Graphics g) {
                // CRITICAL FIX: Call super FIRST, then paint over it
                super.paintComponent(g);
                
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2.setColor(ModernTheme.BACKGROUND_MEDIUM);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                
                // Border - highlighted when focused
                if (focused) {
                    g2.setColor(ModernTheme.PRIMARY);
                    g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setColor(ModernTheme.BACKGROUND_LIGHT);
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                
                // Draw text manually to ensure it's on top
                g2.setColor(ModernTheme.TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                
                if (!text.isEmpty()) {
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(text, 12, y);
                } else if (!focused) {
                    // Draw placeholder
                    g2.setColor(ModernTheme.TEXT_SECONDARY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(placeholder, 12, y);
                }
                
                g2.dispose();
            }
            
            @Override
            protected void processFocusEvent(FocusEvent e) {
                focused = (e.getID() == FocusEvent.FOCUS_GAINED);
                repaint();
                super.processFocusEvent(e);
            }
        };
        
        field.setOpaque(false);
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setCaretColor(ModernTheme.PRIMARY);
        field.setFont(ModernTheme.FONT_BODY);
        field.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        field.setPreferredSize(new Dimension(200, 40));
        
        // Repaint on text changes
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
        });
        
        return field;
    }
    
    private JPasswordField createModernPasswordField() {
        JPasswordField field = new JPasswordField(15) {
            private boolean focused = false;
            private final String placeholder = "Enter password";
            
            @Override
            protected void paintComponent(Graphics g) {
                // CRITICAL FIX: Call super FIRST
                super.paintComponent(g);
                
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2.setColor(ModernTheme.BACKGROUND_MEDIUM);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                
                // Border
                if (focused) {
                    g2.setColor(ModernTheme.PRIMARY);
                    g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setColor(ModernTheme.BACKGROUND_LIGHT);
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                
                // Draw password text or placeholder
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                char[] password = getPassword();
                
                if (password.length > 0) {
                    g2.setColor(ModernTheme.TEXT_PRIMARY);
                    String dots = "•".repeat(password.length);
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(dots, 12, y);
                } else if (!focused) {
                    g2.setColor(ModernTheme.TEXT_SECONDARY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(placeholder, 12, y);
                }
                
                g2.dispose();
            }
            
            @Override
            protected void processFocusEvent(FocusEvent e) {
                focused = (e.getID() == FocusEvent.FOCUS_GAINED);
                repaint();
                super.processFocusEvent(e);
            }
        };
        
        field.setOpaque(false);
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setCaretColor(ModernTheme.PRIMARY);
        field.setFont(ModernTheme.FONT_BODY);
        field.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        field.setPreferredSize(new Dimension(200, 40));
        field.setEchoChar('\0'); // Disable default echo char since we're painting manually
        
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { field.repaint(); }
        });
        
        return field;
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            SwingUtils.showError(this, "Error", "Username and password are required.");
            return;
        }
        
        try {
            if (client.login(username, password)) {
                openMainChat();
            }
        } catch (Exception e) {
            SwingUtils.showError(this, "Login Failed", e.getMessage());
        }
    }
    
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            SwingUtils.showError(this, "Error", "Username and password are required.");
            return;
        }
        
        try {
            if (client.register(username, password)) {
                SwingUtils.showInfo(this, "Success", "Registration successful! You can now login.");
            }
        } catch (Exception e) {
            SwingUtils.showError(this, "Registration Failed", e.getMessage());
        }
    }
    
    private void openMainChat() {
        MainChatFrame mainFrame = new MainChatFrame(client);
        mainFrame.setVisible(true);
        dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }

    private void setAppIcon() {
        try {
            java.net.URL iconURL = getClass().getClassLoader().getResource("images/logo-32.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.err.println("Could not load app icon: " + e.getMessage());
        }
    }
}