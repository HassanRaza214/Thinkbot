package com.example;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SplashScreen extends JFrame {
    public SplashScreen() {
        setUndecorated(true);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top logos panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(245, 245, 245));

        try {
            URL leftLogoURL = getClass().getResource("/logo1.png");
            URL rightLogoURL = getClass().getResource("/logo2.png");

            if (leftLogoURL != null && rightLogoURL != null) {
                ImageIcon leftLogo = new ImageIcon(leftLogoURL);
                ImageIcon rightLogo = new ImageIcon(rightLogoURL);

                JLabel leftLabel = new JLabel(scaleImage(leftLogo, 90, 90));
                JLabel rightLabel = new JLabel(scaleImage(rightLogo, 90, 90));

                logoPanel.add(leftLabel, BorderLayout.WEST);
                logoPanel.add(rightLabel, BorderLayout.EAST);
            }
        } catch (Exception e) {
            System.out.println("Logo loading failed.");
        }

        // Center content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(8, 0, 8, 0);

        JLabel universityLabel = new JLabel("Sir Syed University of Engineering & Technology");
        universityLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        universityLabel.setForeground(new Color(50, 50, 50));

        JLabel deptLabel = new JLabel("Software Engineering Department");
        deptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        deptLabel.setForeground(new Color(70, 70, 70));

        JLabel courseLabel = new JLabel("Data Structures & Algorithms");
        courseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        courseLabel.setForeground(new Color(70, 70, 70));

        JLabel projectLabel = new JLabel("Self-Learning Chatbot");
        projectLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        projectLabel.setForeground(new Color(81, 43, 129));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 18));
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setForeground(new Color(81, 43, 129));
        progressBar.setBorderPainted(false);

        contentPanel.add(universityLabel, gbc);
        contentPanel.add(deptLabel, gbc);
        contentPanel.add(courseLabel, gbc);
        contentPanel.add(Box.createVerticalStrut(15), gbc);
        contentPanel.add(projectLabel, gbc);
        contentPanel.add(Box.createVerticalStrut(25), gbc);
        contentPanel.add(progressBar, gbc);

        mainPanel.add(logoPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Add subtle border for depth
        getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createLineBorder(new Color(245, 245, 245), 5)
        ));
    }

    private ImageIcon scaleImage(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }
}
