package com.example;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Display the splash screen first
            SplashScreen splash = new SplashScreen();
            splash.setVisible(true);

            Timer splashTimer = new Timer(3000, e -> {
                splash.dispose();
                new AuthGUI().setVisible(true); // Open the auth GUI after splash
            });
            splashTimer.setRepeats(false);
            splashTimer.start();
        });
    }
}
