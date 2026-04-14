package com.habeshagram.client.ui;

import com.habeshagram.client.core.ChatClient;
import com.habeshagram.client.util.SwingUtils;
import com.habeshagram.common.exception.AuthenticationException;
import com.habeshagram.common.exception.UserAlreadyExistsException;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class LoginFrame extends JFrame {
    private ChatClient client;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    
    public LoginFrame() {
        client = new ChatClient();
        initializeUI();
        
        // Try to connect to server
        if (!client.connect()) {
            SwingUtils.showError(this, "Connection Error", 
                "Cannot connect to server. Please ensure the server is running.");
        }
    }
    
    private void initializeUI() {
        setTitle("Habeshagram - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        JLabel titleLabel = new JLabel("Habeshagram");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Username:"), gbc);
        
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Password:"), gbc);
        
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passwordField, gbc);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);
        
        registerButton = new JButton("Register");
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
        pack();
        SwingUtils.centerOnScreen(this);
        
        // Enter key triggers login
        getRootPane().setDefaultButton(loginButton);
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
        } catch (RemoteException e) {
            SwingUtils.showError(this, "Connection Error", 
                "Server error: " + e.getMessage());
        } catch (AuthenticationException e) {
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
                SwingUtils.showInfo(this, "Success", 
                    "Registration successful! You can now login.");
            }
        } catch (RemoteException e) {
            SwingUtils.showError(this, "Connection Error", 
                "Server error: " + e.getMessage());
        } catch (UserAlreadyExistsException e) {
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
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginFrame().setVisible(true);
        });
    }
}