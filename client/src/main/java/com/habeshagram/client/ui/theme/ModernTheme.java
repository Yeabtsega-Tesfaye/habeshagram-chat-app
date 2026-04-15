package com.habeshagram.client.ui.theme;

import java.awt.*;
import javax.swing.UIManager;

public class ModernTheme {
    // Primary Colors - Modern Purple/Blue theme (like Discord)
    public static final Color PRIMARY = new Color(88, 101, 242);      // Discord blurple
    public static final Color PRIMARY_DARK = new Color(78, 93, 218);
    public static final Color PRIMARY_LIGHT = new Color(114, 137, 255);
    
    // Background Colors
    public static final Color BACKGROUND_DARK = new Color(32, 34, 37);    // Dark theme bg
    public static final Color BACKGROUND_MEDIUM = new Color(47, 49, 54);
    public static final Color BACKGROUND_LIGHT = new Color(54, 57, 63);
    public static final Color BACKGROUND_CHAT = new Color(49, 51, 56);
    
    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    public static final Color TEXT_SECONDARY = new Color(185, 187, 190);
    public static final Color TEXT_MUTED = new Color(142, 146, 151);
    
    // Message Bubbles
    public static final Color BUBBLE_SENT = new Color(88, 101, 242);
    public static final Color BUBBLE_RECEIVED = new Color(64, 68, 75);
    public static final Color BUBBLE_SYSTEM = new Color(47, 49, 54);
    
    // Status Colors
    public static final Color ONLINE = new Color(59, 165, 93);
    public static final Color OFFLINE = new Color(116, 127, 141);
    public static final Color AWAY = new Color(250, 166, 26);
    public static final Color ERROR = new Color(237, 66, 69);
    public static final Color SUCCESS = new Color(59, 165, 93);
    
    // Accent Colors
    public static final Color ACCENT_PURPLE = new Color(152, 85, 236);
    public static final Color ACCENT_BLUE = new Color(0, 175, 244);
    public static final Color ACCENT_GREEN = new Color(59, 165, 93);
    public static final Color ACCENT_RED = new Color(237, 66, 69);
    public static final Color ACCENT_ORANGE = new Color(250, 166, 26);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MESSAGE = new Font("Segoe UI", Font.PLAIN, 13);
    
    // Spacing
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 12;
    public static final int SPACING_LG = 16;
    public static final int SPACING_XL = 24;
    
    // Border Radius
    public static final int RADIUS_SM = 4;
    public static final int RADIUS_MD = 8;
    public static final int RADIUS_LG = 12;
    public static final int RADIUS_XL = 20;
    
    // Shadows
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 50);
    
    // Apply modern look and feel
    public static void applyTheme() {
        try {
            // Use Nimbus look and feel as base
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    
                    // Customize Nimbus colors
                    UIManager.put("control", BACKGROUND_MEDIUM);
                    UIManager.put("nimbusBase", PRIMARY);
                    UIManager.put("nimbusBlueGrey", BACKGROUND_LIGHT);
                    UIManager.put("nimbusFocus", PRIMARY_LIGHT);
                    UIManager.put("nimbusLightBackground", BACKGROUND_DARK);
                    UIManager.put("nimbusSelectionBackground", PRIMARY);
                    UIManager.put("text", TEXT_PRIMARY);
                    UIManager.put("nimbusSelectedText", TEXT_PRIMARY);
                    
                    break;
                }
            }
        } catch (Exception e) {
            // Fallback to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}