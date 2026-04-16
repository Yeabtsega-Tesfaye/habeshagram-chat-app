package com.habeshagram.client.ui.components;

import com.habeshagram.common.model.User;
import com.habeshagram.common.model.UserStatus;
import com.habeshagram.client.ui.theme.ModernTheme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.ArrayList;

public class OnlineUserPanel extends JPanel {
    private DefaultListModel<User> userListModel;
    private JList<User> userList;
    private JTextField searchField;
    private List<User> allUsers = new ArrayList<>();
    private Consumer<String> onStatusChanged;
    
    public OnlineUserPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("All Users"));
        
        // Create search panel with label
        JPanel searchPanel = new JPanel(new BorderLayout());
        JLabel searchLabel = new JLabel("🔍");
        searchLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        
        searchField = new JTextField();
        
        // Add document listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterUsers(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterUsers(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterUsers(); }
        });
        
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());
        userList.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        userList.setSelectionBackground(ModernTheme.PRIMARY);
        userList.setSelectionForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(userList);
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(200, 0));
    }
    
    public void updateUsers(List<User> users) {
        this.allUsers = users;
        filterUsers();
    }
    
    private void filterUsers() {
        String searchText = searchField.getText().toLowerCase().trim();
        
        List<User> filteredUsers;
        if (searchText.isEmpty()) {
            filteredUsers = allUsers;
        } else {
            filteredUsers = new ArrayList<>();
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().contains(searchText)) {
                    filteredUsers.add(user);
                }
            }
        }
        
        userListModel.clear();
        for (User user : filteredUsers) {
            userListModel.addElement(user);
        }
    }
    
    public String getSelectedUser() {
        User selected = userList.getSelectedValue();
        return selected != null ? selected.getUsername() : null;
    }
    
    public void addUserSelectionListener(java.awt.event.MouseListener listener) {
        userList.addMouseListener(listener);
    }

    public void setOnStatusChanged(Consumer<String> callback) {
        this.onStatusChanged = callback;
    }
    
    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                     int index, boolean isSelected, 
                                                     boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof User) {
                User user = (User) value;

                StringBuilder displayText = new StringBuilder(user.getUsername());

                if (user.getCustomStatus() != null && !user.getCustomStatus().isEmpty()) {
                    displayText.append(" - ").append(user.getCustomStatus());
                }

                label.setText(displayText.toString());
                
                // Simple tooltip - just text, let Swing handle styling
                if (user.getStatus() == UserStatus.ONLINE) {
                    label.setIcon(createOnlineIcon());
                    label.setToolTipText("Online  " + user.getStatusText());
                } else {
                    if (user.getLastSeen() != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a");
                        label.setToolTipText("Last seen: " + user.getLastSeen().format(formatter));
                    } else {
                        label.setIcon(createOfflineIcon());
                        label.setToolTipText("Offline  " + user.getStatusText());
                    }
                }
                
                // Colors
                if (isSelected) {
                    label.setBackground(ModernTheme.PRIMARY);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(ModernTheme.BACKGROUND_MEDIUM);
                    if (user.getStatus() == UserStatus.ONLINE) {
                        label.setForeground(new Color(100, 255, 100));
                    } else {
                        label.setForeground(new Color(160, 160, 160));
                    }
                }
                
                // Icon
                if (user.getStatus() == UserStatus.ONLINE) {
                    label.setIcon(createOnlineIcon());
                } else {
                    label.setIcon(createOfflineIcon());
                }
                
                label.setOpaque(true);
                label.setIconTextGap(8);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            }
            
            return label;
        }
        
        private Icon createOnlineIcon() {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(59, 165, 93));
                    g2.fillOval(x, y + 2, 10, 10);
                    g2.dispose();
                }
                @Override
                public int getIconWidth() { return 10; }
                @Override
                public int getIconHeight() { return 14; }
            };
        }
        
        private Icon createOfflineIcon() {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(116, 127, 141));
                    g2.fillOval(x, y + 2, 10, 10);
                    g2.dispose();
                }
                @Override
                public int getIconWidth() { return 10; }
                @Override
                public int getIconHeight() { return 14; }
            };
        }
    }
}