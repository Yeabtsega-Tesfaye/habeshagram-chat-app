package com.habeshagram.client.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OnlineUserPanel extends JPanel {
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    
    public OnlineUserPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Online Users"));
        
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane, BorderLayout.CENTER);
        
        setPreferredSize(new Dimension(200, 0));
    }
    
    public void updateUsers(List<String> users) {
        userListModel.clear();
        for (String user : users) {
            userListModel.addElement(user);
        }
    }
    
    public String getSelectedUser() {
        return userList.getSelectedValue();
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
            
            label.setIcon(UIManager.getIcon("FileView.computerIcon"));
            label.setIconTextGap(8);
            
            return label;
        }
    }
}