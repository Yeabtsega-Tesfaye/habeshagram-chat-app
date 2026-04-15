package com.habeshagram.client.ui.components;

import com.habeshagram.client.ui.theme.ModernTheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class EmojiPicker extends JPopupMenu {
    private static final String[] EMOJIS = {
        "😊", "😂", "❤️", "👍", "🔥", "🎉", 
        "😍", "🙏", "😭", "✨", "😎", "👀",
        "💯", "🤔", "😅", "💪", "👏", "🙌"
    };
    
    private Consumer<String> onEmojiSelected;
    
    public EmojiPicker(Consumer<String> onEmojiSelected) {
        this.onEmojiSelected = onEmojiSelected;
        setupUI();
    }
    
    private void setupUI() {
        setBackground(ModernTheme.BACKGROUND_MEDIUM);
        setBorder(BorderFactory.createLineBorder(ModernTheme.BACKGROUND_LIGHT));
        
        JPanel panel = new JPanel(new GridLayout(3, 6, 5, 5));
        panel.setBackground(ModernTheme.BACKGROUND_MEDIUM);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        for (String emoji : EMOJIS) {
            JLabel emojiLabel = new JLabel(emoji);
            emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            emojiLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emojiLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            emojiLabel.setToolTipText(emoji);
            
            emojiLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onEmojiSelected.accept(emoji);
                    setVisible(false);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    emojiLabel.setBackground(ModernTheme.PRIMARY);
                    emojiLabel.setOpaque(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    emojiLabel.setOpaque(false);
                }
            });
            
            panel.add(emojiLabel);
        }
        
        add(panel);
    }
}