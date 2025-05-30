package com.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatbotGUI extends JFrame {
    private JPanel sidebarPanel, chatPanel, inputPanel;
    private JScrollPane chatScrollPane;
    private JTextField userInputField;
    private JButton sendButton, clearButton, historyButton, settingsButton;
    private JLabel typingIndicator, statusLabel;
    private DBHelper dbHelper;
    private JComboBox<String> topicComboBox;
    private int userId; // tracks logged-in user

    public ChatbotGUI(String username) {
        dbHelper = new DBHelper();
        this.userId = dbHelper.getUserIdByUsername(username);

        setTitle("Chatbot - Welcome " + username);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(40, 40, 60));
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        sidebarPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel profileLabel = new JLabel("<html><center><font color='white'><h3>" + username + "</h3></font></center></html>");
        profileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileLabel.setForeground(new Color(255, 255, 255));
        sidebarPanel.add(profileLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        List<String> topics = dbHelper.getAllTopics();
        topicComboBox = new JComboBox<>(topics.toArray(new String[0]));
        topicComboBox.setMaximumSize(new Dimension(180, 30));
        sidebarPanel.add(topicComboBox);

        historyButton = createSidebarButton("Chat History");
        clearButton = createSidebarButton("Clear Chat");
        settingsButton = createSidebarButton("Settings");

        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebarPanel.add(historyButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        sidebarPanel.add(clearButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        sidebarPanel.add(settingsButton);

        // Chat display
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        customizeScrollbar(chatScrollPane);

        // Input area
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        userInputField = new JTextField();
        userInputField.setPreferredSize(new Dimension(400, 30));

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(81, 43, 129));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        typingIndicator = new JLabel(" ");
        typingIndicator.setFont(new Font("Arial", Font.ITALIC, 12));
        typingIndicator.setForeground(new Color(100, 100, 100));

        statusLabel = new JLabel("Online");
        statusLabel.setHorizontalAlignment(JLabel.RIGHT);
        statusLabel.setForeground(new Color(0, 153, 0));

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.add(typingIndicator, BorderLayout.WEST);
        bottomBar.add(statusLabel, BorderLayout.EAST);

        inputPanel.add(userInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(bottomBar, BorderLayout.SOUTH);

        add(sidebarPanel, BorderLayout.WEST);
        add(chatScrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Action listeners
        sendButton.addActionListener(e -> handleUserMessage());
        clearButton.addActionListener(e -> clearChat());
        settingsButton.addActionListener(e -> showSettingsDialog());

        // Updated history button listener
        historyButton.addActionListener(e -> {
            List<String[]> entries = dbHelper.getUserHistoryEntries(userId);
            new HistoryGUI(this, entries).setVisible(true);
        });

        setVisible(true);
    }

    private void handleUserMessage() {
        String userMessage = userInputField.getText().trim();
        String selectedTopic = (String) topicComboBox.getSelectedItem();

        if (userMessage.isEmpty()) return;

        userInputField.setText("");
        typingIndicator.setText("Bot is typing...");
        statusLabel.setText("Processing...");

        new SwingWorker<String, Void>() {
            boolean isFromAPI = false;

            @Override
            protected String doInBackground() {
                String answer = dbHelper.getAnswer(userMessage);
                if (answer == null) {
                    answer = dbHelper.getSimilarAnswer(userMessage);
                }
                if (answer == null) {
                    isFromAPI = true;
                    answer = dbHelper.callLLMAPI(userMessage);
                }
                return answer;
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    addQAThread(userMessage, response, isFromAPI, selectedTopic);
                } catch (Exception ex) {
                    addSystemMessage("[Error getting response]");
                } finally {
                    typingIndicator.setText(" ");
                    statusLabel.setText("Online");
                    scrollToBottom();
                }
            }
        }.execute();
    }

    private void addQAThread(String question, String answer, boolean isFromAPI, String topic) {
        JPanel qaPanel = new JPanel();
        qaPanel.setLayout(new BoxLayout(qaPanel, BoxLayout.Y_AXIS));
        qaPanel.setBackground(Color.WHITE);
        qaPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Question bubble
        JPanel questionBubble = new JPanel(new BorderLayout());
        questionBubble.setBackground(new Color(220, 235, 255));
        questionBubble.setBorder(new EmptyBorder(10, 10, 10, 10));
        questionBubble.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        questionBubble.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel questionLabel = new JLabel("<html><body style='width: 400px'>" + question + "</body></html>");
        JLabel questionTime = new JLabel(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        questionTime.setFont(new Font("Arial", Font.PLAIN, 10));
        questionTime.setForeground(Color.GRAY);

        questionBubble.add(questionLabel, BorderLayout.CENTER);
        questionBubble.add(questionTime, BorderLayout.SOUTH);

        // Answer bubble
        JPanel answerBubble = new JPanel();
        answerBubble.setLayout(new BoxLayout(answerBubble, BoxLayout.Y_AXIS));
        answerBubble.setBackground(new Color(230, 230, 230));
        answerBubble.setBorder(new EmptyBorder(10, 10, 10, 10));
        answerBubble.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        answerBubble.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel answerLabel = new JLabel("<html><body style='width: 400px'>" + answer + "</body></html>");
        JLabel answerTime = new JLabel(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        answerTime.setFont(new Font("Arial", Font.PLAIN, 10));
        answerTime.setForeground(Color.GRAY);

        answerBubble.add(answerLabel);
        answerBubble.add(Box.createVerticalStrut(5));
        answerBubble.add(answerTime);

        // Feedback panel if from API
        if (isFromAPI) {
            JPanel feedbackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            feedbackPanel.setBackground(new Color(230, 230, 230));

            JLabel feedbackPrompt = new JLabel("Was this helpful?");
            JButton yesButton = new JButton("Yes");
            JButton noButton = new JButton("No");

            styleFeedbackButton(yesButton, new Color(76, 175, 80));
            styleFeedbackButton(noButton, new Color(244, 67, 54));

            feedbackPanel.add(feedbackPrompt);
            feedbackPanel.add(yesButton);
            feedbackPanel.add(noButton);

            yesButton.addActionListener(e -> {
                dbHelper.addQuestionToTopic(topic, question, answer, userId);
                showFeedbackAcknowledgment(answerBubble, feedbackPanel);
                addSystemMessage("[Saved to database]");
            });

            noButton.addActionListener(e -> {
                showFeedbackAcknowledgment(answerBubble, feedbackPanel);
                addSystemMessage("[Answer not saved]");
            });

            answerBubble.add(Box.createVerticalStrut(10));
            answerBubble.add(feedbackPanel);
        }

        qaPanel.add(questionBubble);
        qaPanel.add(Box.createVerticalStrut(5));
        qaPanel.add(answerBubble);
        qaPanel.add(Box.createVerticalStrut(10));

        chatPanel.add(qaPanel);
        chatPanel.revalidate();
        chatPanel.repaint();
        scrollToBottom();
    }

    private void showFeedbackAcknowledgment(JPanel answerBubble, JPanel feedbackPanel) {
        answerBubble.remove(feedbackPanel);
        JLabel receivedLabel = new JLabel("✓ Feedback received");
        receivedLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        receivedLabel.setForeground(new Color(0, 128, 0));
        receivedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        receivedLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
        answerBubble.add(receivedLabel);
        answerBubble.revalidate();
        answerBubble.repaint();
    }

    private void styleFeedbackButton(JButton button, Color bgColor) {
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(60, 25));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private void addSystemMessage(String msg) {
        JLabel label = new JLabel(msg);
        label.setFont(new Font("Arial", Font.ITALIC, 12));
        label.setForeground(Color.GRAY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        chatPanel.add(label);
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(160, 30));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(81, 43, 129));
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(120, 70, 180));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(81, 43, 129));
            }
        });

        return btn;
    }

    private void customizeScrollbar(JScrollPane scrollPane) {
        JScrollBar vScroll = scrollPane.getVerticalScrollBar();
        vScroll.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(120, 70, 180);
                this.trackColor = new Color(240, 240, 240);
            }
        });
    }

    private void clearChat() {
        chatPanel.removeAll();
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private void showSettingsDialog() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("General", new JPanel().add(new JLabel("General Settings")));
        tabs.addTab("API", new JPanel().add(new JLabel("API Configuration")));
        tabs.addTab("Account", new JPanel().add(new JLabel("Account Settings")));
        JOptionPane.showMessageDialog(this, tabs, "Settings", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Load a past Q&A into the chat view
     */
    public void loadConversation(String question, String answer) {
        clearChat();
        // Safely get current topic, if none selected use empty string
        String currentTopic = "";
        Object sel = topicComboBox.getSelectedItem();
        if (sel != null) {
            currentTopic = sel.toString();
        }
        addQAThread(question, answer, false, currentTopic);
    }
}
