package com.habeshagram.client.util;

import javax.swing.*;
import java.awt.*;

public class SwingUtils {
    
    public static void centerOnScreen(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation(
            (screenSize.width - window.getWidth()) / 2,
            (screenSize.height - window.getHeight()) / 2
        );
    }
    
    public static void showError(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static boolean showConfirm(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, 
                                                  JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }
    
    public static String showInput(Component parent, String title, String message) {
        return JOptionPane.showInputDialog(parent, message, title, 
                                          JOptionPane.QUESTION_MESSAGE);
    }
}