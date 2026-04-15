package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.ui.components.ModernButton;
import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.client.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(ModernTheme.BACKGROUND_DARK);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Logo/Title
        JLabel titleLabel = new JLabel("💬 Habeshagram");
        titleLabel.setFont(ModernTheme.FONT_TITLE);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Stay connected the Habesha way!");
        subtitleLabel.setFont(ModernTheme.FONT_SMALL);
        subtitleLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 8, 30, 8);
        mainPanel.add(subtitleLabel, gbc);
        
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 1;
        
        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ModernTheme.FONT_SMALL);
        userLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(userLabel, gbc);
        
        usernameField = createModernTextField();
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);
        
        // Password
        JLabel passLabel = new JLabel("Password");
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
        
        getRootPane().setDefaultButton(loginButton);
    }
    
    private JTextField createModernTextField() {
        JTextField field = new JTextField(15) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(ModernTheme.BACKGROUND_MEDIUM);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                
                g2.setColor(ModernTheme.BACKGROUND_LIGHT);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setCaretColor(ModernTheme.TEXT_PRIMARY);
        field.setFont(ModernTheme.FONT_BODY);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return field;
    }
    
    private JPasswordField createModernPasswordField() {
        JPasswordField field = new JPasswordField(15) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(ModernTheme.BACKGROUND_MEDIUM);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                
                g2.setColor(ModernTheme.BACKGROUND_LIGHT);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setForeground(ModernTheme.TEXT_PRIMARY);
        field.setCaretColor(ModernTheme.TEXT_PRIMARY);
        field.setFont(ModernTheme.FONT_BODY);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
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
}