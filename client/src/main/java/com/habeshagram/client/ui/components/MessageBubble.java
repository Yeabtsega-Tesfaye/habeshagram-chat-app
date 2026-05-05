package com.habeshagram.client.ui.components;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import com.habeshagram.client.ui.theme.ModernTheme;
import com.habeshagram.common.model.Message;
import com.habeshagram.common.model.MessageType;

public class MessageBubble extends JPanel {
    private static final int MAX_BUBBLE_WIDTH = 400;
    private static final int BUBBLE_PADDING = 12;
    private Message message;
    private boolean isOwnMessage;
    private Consumer<Message> onDelete;
    private Consumer<Message> onReply;
    private JLabel statusLabel;
    
    public MessageBubble(Message message, boolean isOwnMessage, Consumer<Message> onDelete) {
        setLayout(new BorderLayout());
        setOpaque(false);

        this.message = message;
        this.isOwnMessage = isOwnMessage;
        this.onReply = null; // Will set later if needed
        this.onDelete = onDelete;
        
        JPanel bubblePanel = createBubblePanel(message, isOwnMessage);
        
        // Add tooltip with full timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        bubblePanel.setToolTipText(message.getTimestamp().format(formatter));
        
        // Align based on ownership
        if (isOwnMessage && message.getType() != MessageType.SYSTEM) {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(bubblePanel);
            add(wrapper, BorderLayout.CENTER);
        } else if (message.getType() == MessageType.SYSTEM) {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(bubblePanel);
            add(wrapper, BorderLayout.CENTER);
        } else {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(bubblePanel);
            add(wrapper, BorderLayout.CENTER);
        }

        addRightClickListener(bubblePanel);
    }

private void addRightClickListener(JPanel bubblePanel) {
    bubblePanel.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showContextMenu(e);
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showContextMenu(e);
            }
        }
    });

    for (Component comp : bubblePanel.getComponents()) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
    }
}

private void showContextMenu(MouseEvent e) {
    JPopupMenu menu = ContextMenuFactory.createMessageMenu(
        message,
        isOwnMessage,
        action -> {
            switch (action) {
                case COPY:
                    ContextMenuFactory.showToast(this, "Copied to clipboard!");
                    break;
                case DELETE:
                    int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Delete this message?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (onDelete != null) {
                            onDelete.accept(message);
                        }
                    }
                    break;
                case REPLY:
                    // This should now be called!
                    if (onReply != null) {
                        onReply.accept(message);
                    }
                    break;
            }
        }
    );
    
    menu.show(e.getComponent(), e.getX(), e.getY());
}

private JPanel createBubblePanel(Message message, boolean isOwnMessage) {
    JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Float fadeOpacity = (Float) getClientProperty("fadeOpacity");
            if (fadeOpacity != null && fadeOpacity < 1.0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeOpacity));
            }

            // Draw shadow
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fill(new RoundRectangle2D.Double(2, 2, getWidth() - 1, getHeight() - 1, 12, 12));

            // Draw bubble
            Color bgColor = getBubbleColor(message, isOwnMessage);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 3, getHeight() - 3, 12, 12));
            
            g2.dispose();
        }
    };
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(BUBBLE_PADDING, BUBBLE_PADDING, BUBBLE_PADDING, BUBBLE_PADDING));

    // Reply preview
    if (message.isReply()) {
        ReplyBubble replyPreview = new ReplyBubble(
            message.getReplyToSender(), 
            message.getReplyToContent()
        );
        replyPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(replyPreview);
    }
    
    // Header for non-system messages
    if (message.getType() != MessageType.SYSTEM) {
        if (isOwnMessage) {
            // Simplified header for own messages
            JPanel ownHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            ownHeader.setOpaque(false);
            
            if (message.getType() == MessageType.PRIVATE) {
                JLabel icon = new JLabel("🔒");
                icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
                ownHeader.add(icon);
            } else if (message.getType() == MessageType.GROUP) {
                JLabel icon = new JLabel("👥");
                icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
                ownHeader.add(icon);
            } else if (message.getType() == MessageType.BROADCAST) {
                JLabel icon = new JLabel("📢");
                icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
                ownHeader.add(icon);
            }
            
            JLabel timeLabel = new JLabel(message.getFormattedTime());
            timeLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(10f));
            timeLabel.setForeground(ModernTheme.TEXT_MUTED);
            ownHeader.add(timeLabel);
            
            panel.add(ownHeader);
            panel.add(Box.createVerticalStrut(4));
        } else {
            JPanel headerPanel = createHeader(message, isOwnMessage);
            panel.add(headerPanel);
            panel.add(Box.createVerticalStrut(4));
        }
    }
    
    // Message content
    JLabel contentLabel = new JLabel("<html><div style='width:" + (MAX_BUBBLE_WIDTH - 40) + "px;'>" 
                                    + message.getContent().replaceAll("\n", "<br>") + "</div></html>");
    contentLabel.setFont(ModernTheme.FONT_MESSAGE);
    contentLabel.setForeground(ModernTheme.TEXT_PRIMARY);
    contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(contentLabel);
    
    // ✅ Footer for EVERY message, not only system
    panel.add(Box.createVerticalStrut(4));
    JPanel footerPanel = createFooter(message, isOwnMessage);
    if (message.getType() == MessageType.SYSTEM) {
        footerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
    panel.add(footerPanel);
    
    // Calculate preferred size
    int width = Math.min(MAX_BUBBLE_WIDTH, panel.getPreferredSize().width);
    panel.setPreferredSize(new Dimension(width, panel.getPreferredSize().height));
    
    return panel;
}

  private Color getBubbleColor(Message message, boolean isOwnMessage) {
        if (message.getType() == MessageType.SYSTEM) {
            return ModernTheme.BUBBLE_SYSTEM;
        } else if (isOwnMessage) {
            return ModernTheme.BUBBLE_SENT;
        } else {
            return ModernTheme.BUBBLE_RECEIVED;
        }
    }
    
private JPanel createHeader(Message message, boolean isOwnMessage) {
    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    header.setOpaque(false);
    
    // Sender name
    JLabel nameLabel = new JLabel(message.getSender());
    nameLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
    nameLabel.setForeground(ModernTheme.ACCENT_BLUE);
    header.add(nameLabel);
    
    // Icon indicators
    if (message.getType() == MessageType.PRIVATE) {
        JLabel privateIcon = new JLabel("🔒");
        privateIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        privateIcon.setToolTipText("Private Message");
        header.add(privateIcon);
    } else if (message.getType() == MessageType.GROUP) {
        JLabel groupIcon = new JLabel("👥");
        groupIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        groupIcon.setToolTipText("Group: " + message.getRecipient());
        header.add(groupIcon);
    }   else if (message.getType() == MessageType.BROADCAST) {
        JLabel broadcastIcon = new JLabel("📢");
        broadcastIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        broadcastIcon.setToolTipText("Broadcast Message");
        header.add(broadcastIcon);
    }
    
    
    // Separator dot
    JLabel dotLabel = new JLabel("•");
    dotLabel.setFont(ModernTheme.FONT_SMALL);
    dotLabel.setForeground(ModernTheme.TEXT_MUTED);
    header.add(dotLabel);
    
    // Time
    JLabel timeLabel = new JLabel(message.getFormattedTime());
    timeLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(10f));
    timeLabel.setForeground(ModernTheme.TEXT_MUTED);
    header.add(timeLabel);
    
    return header;
}
    
private JPanel createFooter(Message message, boolean isOwnMessage) {
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    footer.setOpaque(false);
    
    // Status icon for own messages
    if (isOwnMessage && message.getType() != MessageType.SYSTEM) {
         this.statusLabel = new JLabel(getStatusText(message));
        statusLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 11));
        
if (message.isRead()) {
    statusLabel.setForeground(new Color(255, 255, 255, 255));  // solid white
} else if (message.isDelivered()) {
    statusLabel.setForeground(new Color(255, 255, 255, 180));
} else {
    statusLabel.setForeground(new Color(255, 255, 255, 120));
}

        footer.add(statusLabel);
        footer.add(Box.createHorizontalStrut(4));
    }
    
    JLabel timeLabel = new JLabel(message.getFormattedTime());
    timeLabel.setFont(ModernTheme.FONT_SMALL.deriveFont(10f));
    timeLabel.setForeground(isOwnMessage ? new Color(255, 255, 255, 180) : ModernTheme.TEXT_MUTED);
    footer.add(timeLabel);
    
    return footer;
}

private String getStatusText(Message message) {
    switch (message.getStatus()) {
        case SENT:     return "Sent";
        case DELIVERED: return "Delivered";
        case READ:     return "Read";
        default:       return "Sent";
    }
}

    public void addReplyHandler(Consumer<Message> handler) {
        this.onReply = handler;
    }

    public Message getMessage() {
    return message;
}

public void updateStatus(Message.MessageStatus newStatus) {
    this.message.setStatus(newStatus);
    if (statusLabel != null) {
        String text = switch (newStatus) {
            case SENT -> "Sent";
            case DELIVERED -> "Delivered";
            case READ -> "Read";
        };
        statusLabel.setText(text);

        Color color = switch (newStatus) {
            case SENT     -> new Color(255, 255, 255, 120);
            case DELIVERED -> new Color(255, 255, 255, 180);
            case READ     -> new Color(255, 255, 255, 255);
        };
        statusLabel.setForeground(color);
        repaint();
    }
}
}