package com.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class AuthGUI extends JFrame {
    private DBHelper dbHelper;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private JLabel messageLabel;

    public AuthGUI() {
        dbHelper = new DBHelper();
        setTitle("Chatbot Login");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Welcome to Chatbot");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(81, 43, 129));

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(300, 30));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 30));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton = createStyledButton("Login");
        registerButton = createStyledButton("Register");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (dbHelper.authenticateUser(username, password)) {
                messageLabel.setText("Login successful!");
                messageLabel.setForeground(new Color(0, 128, 0));
                new ChatbotGUI(username).setVisible(true); // Passing username to ChatbotGUI
                dispose();
            } else {
                messageLabel.setText("Invalid credentials.");
                messageLabel.setForeground(Color.RED);
            }
        });

        registerButton.addActionListener(e -> {
            new RegisterGUI().setVisible(true);
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(250, 250, 250));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(buttonPanel);

        add(mainPanel);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(81, 43, 129));
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(120, 70, 180));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(81, 43, 129));
            }
        });

        return button;
    }
}
