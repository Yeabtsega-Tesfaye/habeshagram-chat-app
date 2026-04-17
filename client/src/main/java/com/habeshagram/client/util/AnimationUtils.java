package com.habeshagram.client.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AnimationUtils {

    /**
     * Fade in a component smoothly
     * 
     * @param component  The component to fade in
     * @param durationMs Duration in milliseconds (e.g., 300)
     */
    public static void fadeIn(JComponent component, int durationMs) {

        component.setVisible(false); // Start hidden
        component.setOpaque(false);

        Timer timer = new Timer(durationMs / 30, null);
        final float[] opacity = { 0.0f };
        final float step = 1.0f / 30;

        timer.addActionListener(e -> {
            opacity[0] += step;

            if (opacity[0] >= 1.0f) {
                opacity[0] = 1.0f;
                ((Timer) e.getSource()).stop();
                component.setOpaque(true);
            }

            component.putClientProperty("fadeOpacity", opacity[0]);
            component.repaint();
        });

        timer.start();
        component.setVisible(true); // Make visible AFTER timer starts
    }

    /**
     * Stops the pulse animation on a component
     */
    public static void stopPulseAnimation(JComponent component) {
        Timer timer = (Timer) component.getClientProperty("pulseTimer");
        if (timer != null) {
            timer.stop();
        }

        Color originalBg = (Color) component.getClientProperty("originalBg");
        if (originalBg != null) {
            component.setBackground(originalBg);
        }

        component.putClientProperty("pulseTimer", null);
        component.putClientProperty("originalBg", null);
        component.repaint();
    }

    // Slide in from right
    public static void slideInFromRight(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int targetX = (screen.width - frame.getWidth()) / 2;
        int targetY = (screen.height - frame.getHeight()) / 2;

        frame.setLocation(screen.width, targetY);
        frame.setVisible(true);

        Timer timer = new Timer(5, null);
        final int[] currentX = { screen.width };
        final int step = 30;

        timer.addActionListener(e -> {
            currentX[0] -= step;
            if (currentX[0] <= targetX) {
                frame.setLocation(targetX, targetY);
                ((Timer) e.getSource()).stop();
            } else {
                frame.setLocation(currentX[0], targetY);
            }
        });

        timer.start();
    }

/**
 * Fades out a window before closing
 * @param window The window to fade out
 * @param onComplete Callback when animation finishes
 */
public static void fadeOut(Window window, Runnable onComplete) {
    // Set initial opacity if not already set
    if (window.getOpacity() == 0.0f) {
        window.setOpacity(1.0f);
    }
    
    Timer timer = new Timer(20, null); // ~50 FPS - smooth but fast
    final float[] opacity = {1.0f};
    final float step = 0.12f; // Faster fade (about 8-9 frames = ~180ms)
    
    timer.addActionListener(e -> {
        opacity[0] -= step;
        if (opacity[0] <= 0.0f) {
            ((Timer) e.getSource()).stop();
            window.setOpacity(0.0f);
            onComplete.run();
        } else {
            window.setOpacity(opacity[0]);
        }
    });
    
    timer.start();
}

    // Bounce animation
    public static void bounce(JComponent component) {
        Point original = component.getLocation();
        Timer timer = new Timer(16, null);
        final int[] offset = { 0 };
        final boolean[] goingDown = { true };
        final int[] count = { 0 };

        timer.addActionListener(e -> {
            if (goingDown[0]) {
                offset[0] += 2;
                if (offset[0] >= 8) {
                    goingDown[0] = false;
                }
            } else {
                offset[0] -= 2;
                if (offset[0] <= -4) {
                    offset[0] = 0;
                    count[0]++;
                    if (count[0] >= 2) {
                        ((Timer) e.getSource()).stop();
                        component.setLocation(original);
                        return;
                    }
                    goingDown[0] = true;
                }
            }
            component.setLocation(original.x, original.y + offset[0]);
        });

        timer.start();
    }
}