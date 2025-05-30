package com.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class RegisterGUI extends JFrame {
    private DBHelper dbHelper;
    private JTextField usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton registerButton, backButton;
    private JLabel messageLabel;

    public RegisterGUI() {
        dbHelper = new DBHelper();
        setTitle("Register Account");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Create a New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(81, 43, 129));

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        confirmPasswordField.setBorder(BorderFactory.createTitledBorder("Confirm Password"));

        messageLabel = new JLabel("");
        messageLabel.setForeground(Color.RED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        registerButton = createStyledButton("Register");
        backButton = createStyledButton("Back");

        registerButton.addActionListener(e -> handleRegister());
        backButton.addActionListener(e -> {
            new AuthGUI().setVisible(true);
            dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(250, 250, 250));
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(confirmPasswordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(buttonPanel);

        add(mainPanel);
        setVisible(true);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        if (dbHelper.registerUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            new AuthGUI().setVisible(true);
            dispose();
        } else {
            messageLabel.setText("Username already taken.");
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(81, 43, 129));
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 30));

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
