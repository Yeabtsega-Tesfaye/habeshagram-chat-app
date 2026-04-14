package com.habeshagram.client.ui.components;

import com.habeshagram.common.model.User;
import com.habeshagram.common.model.UserStatus;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OnlineUserPanel extends JPanel {
    private DefaultListModel<User> userListModel;
    private JList<User> userList;
    
    public OnlineUserPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("All Users"));
        
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane, BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(200, 0));
    }
    
    public void updateUsers(List<User> users) {
        userListModel.clear();
        for (User user : users) {
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
    
    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                     int index, boolean isSelected, 
                                                     boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof User) {
                User user = (User) value;
                
                // Set text with username
                label.setText(user.getUsername());
                
                // Set icon based on status
                if (user.getStatus() == UserStatus.ONLINE) {
                    label.setIcon(createOnlineIcon());
                    label.setToolTipText("Online");
                } else {
                    label.setIcon(createOfflineIcon());
                    label.setToolTipText(user.getStatusText());
                }
                
                // Style for offline users
                if (user.getStatus() != UserStatus.ONLINE) {
                    label.setForeground(Color.GRAY);
                } else {
                    label.setForeground(Color.BLACK);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                }
                
                label.setIconTextGap(8);
            }
            
            return label;
        }
        
        private Icon createOnlineIcon() {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 200, 0)); // Green
                    g2.fillOval(x, y, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x + 2, y + 2, 6, 6);
                    g2.dispose();
                }
                
                @Override
                public int getIconWidth() { return 10; }
                
                @Override
                public int getIconHeight() { return 10; }
            };
        }
        
        private Icon createOfflineIcon() {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.GRAY);
                    g2.fillOval(x, y, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x + 3, y + 3, 4, 4);
                    g2.dispose();
                }
                
                @Override
                public int getIconWidth() { return 10; }
                
                @Override
                public int getIconHeight() { return 10; }
            };
        }
    }
}