package com.habeshagram.client.util;

public class SoundManager {
    private static boolean enabled = true;
    
    public static void playMessageSound() {
        if (!enabled) return;
        
        new Thread(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                
                if (os.contains("linux")) {
                    // Use the exact path that worked in terminal
                    String[] cmd = {
                        "/usr/bin/paplay",
                        "/usr/share/sounds/freedesktop/stereo/complete.oga"
                    };
                    
                    try {
                        new ProcessBuilder(cmd).start();
                    } catch (Exception e) {
                        // Try with just "paplay" as fallback
                        try {
                            Runtime.getRuntime().exec("paplay /usr/share/sounds/freedesktop/stereo/complete.oga");
                        } catch (Exception ignored) {}
                    }
                } else if (os.contains("win")) {
                    Runtime.getRuntime().exec("powershell -c (New-Object Media.SoundPlayer).Play()");
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec("afplay /System/Library/Sounds/Glass.aiff");
                }
            } catch (Exception e) {
                // Silent fail
            }
        }).start();
    }
    
    public static void setEnabled(boolean enabled) {
        SoundManager.enabled = enabled;
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
}